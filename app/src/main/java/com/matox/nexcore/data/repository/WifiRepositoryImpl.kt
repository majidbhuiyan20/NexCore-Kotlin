package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.WifiProvider
import com.matox.nexcore.domain.model.WifiSnapshot
import com.matox.nexcore.domain.repository.WifiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext

/**
 * Polls [WifiProvider.snapshot] every [POLL_INTERVAL_MS] and emits
 * the result. The WiFi & Network screen doesn't need to be more
 * frequent than every 3 s — RSSI / link speed change slowly.
 *
 * [refreshNow] unblocks the polling loop immediately for manual
 * refreshes. [refreshPublicIp] fires the one-shot HTTPS public-IP
 * lookup on the IO dispatcher and re-emits the next snapshot with
 * the new public IP filled in.
 *
 * **Off-main-thread.** Every snapshot / public-IP read is wrapped in
 * `withContext(Dispatchers.IO)` and the whole flow is `flowOn(IO)`
 * so the UI thread never blocks on WifiManager / ConnectivityManager
 * / HTTPS round-trips.
 */
class WifiRepositoryImpl(
    private val provider: WifiProvider,
) : WifiRepository {

    private val refreshRequests = Channel<Unit>(capacity = Channel.CONFLATED)
    private val publicIpRefreshes = Channel<Unit>(capacity = Channel.CONFLATED)

    /**
     * Latest public IP — kept in memory so the periodic snapshot loop
     * doesn't lose it when it refreshes the rest of the fields.
     */
    @Volatile private var lastPublicIp: String? = null

    override fun observeSnapshot(): Flow<WifiSnapshot> = channelFlow {
        // First emit immediately.
        runCatching { send(safeSnapshot().copy(publicIp = lastPublicIp)) }

        val ticker = flow {
            while (true) {
                delay(POLL_INTERVAL_MS)
                emit(Unit)
            }
        }
        val refresher = refreshRequests.consumeAsFlow()
        val ipRefresher = publicIpRefreshes.consumeAsFlow()

        // Refresh public IP — runs on IO. The completion triggers a
        // separate `send` of the updated snapshot.
        val ipWorker = ipRefresher.collect {
            val ip = withContext(Dispatchers.IO) { provider.fetchPublicIp() }
            if (ip != null) {
                lastPublicIp = ip
                runCatching { send(safeSnapshot().copy(publicIp = ip)) }
            }
        }

        merge(ticker, refresher).collect {
            runCatching { send(safeSnapshot().copy(publicIp = lastPublicIp)) }
        }

        ipWorker // keep the lambda referenced
    }.flowOn(Dispatchers.IO)

    override suspend fun refreshNow() {
        refreshRequests.send(Unit)
    }

    override suspend fun refreshPublicIp() {
        publicIpRefreshes.send(Unit)
    }

    private suspend fun safeSnapshot(): WifiSnapshot =
        withContext(Dispatchers.IO) { provider.snapshot() }

    companion object {
        /** 3 s cadence — RSSI / link speed change slowly. */
        private const val POLL_INTERVAL_MS: Long = 3_000L
    }
}