package com.matox.nexcore.data.device

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.matox.nexcore.domain.model.AppTrafficRow
import com.matox.nexcore.domain.model.WifiConnection
import com.matox.nexcore.domain.model.WifiIpInfo
import com.matox.nexcore.domain.model.WifiSecurity
import com.matox.nexcore.domain.model.WifiSnapshot
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.URL

/**
 * Synchronous reader that produces a [WifiSnapshot] from on-device
 * network APIs.
 *
 * All reads are defensive: a restricted OEM API or missing network
 * simply contributes a placeholder instead of blanking the whole screen.
 */
class WifiProvider(
    private val appContext: Context,
) {

    @Volatile private var lastGood: WifiSnapshot? = null

    fun snapshot(): WifiSnapshot {
        val result = runCatching { buildSnapshot() }.getOrNull()
        if (result != null) {
            lastGood = result
            return result
        }
        return lastGood ?: emptySnapshot()
    }

    /** One-shot public-IP lookup. Call from a background thread. */
    fun fetchPublicIp(): String? = runCatching {
        val url = URL("https://api.ipify.org?format=json")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 4_000
            readTimeout = 4_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "NexCore/1.0")
        }
        try {
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                Regex("\"ip\"\\s*:\\s*\"([^\"]+)\"")
                    .find(body)?.groupValues?.getOrNull(1)
            } else null
        } finally {
            conn.disconnect()
        }
    }.getOrNull()

    private fun emptySnapshot(): WifiSnapshot = WifiSnapshot(
        connection = null,
        ip = null,
        publicIp = null,
        appTraffic = emptyList(),
        cellularType = "—",
    )

    @SuppressLint("MissingPermission")
    private fun buildSnapshot(): WifiSnapshot {
        val conn = readConnection()
        val ip = readIpInfo()
        val traffic = readAppTraffic()
        val cellular = readCellularType()
        return WifiSnapshot(
            connection = conn,
            ip = ip,
            publicIp = lastGood?.publicIp,
            appTraffic = traffic,
            cellularType = cellular,
        )
    }

    @SuppressLint("MissingPermission")
    private fun wifiManager(): WifiManager? = runCatching {
        appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }.getOrNull()

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun readConnection(): WifiConnection? {
        val wm = wifiManager() ?: return null
        val info: WifiInfo = runCatching { wm.connectionInfo }.getOrNull() ?: return null
        val ssid = (info.ssid ?: "").trim('"').ifBlank { "—" }
        val bssid = info.bssid ?: "—"
        val rssi = info.rssi
        val linkSpeed = info.linkSpeed
        val frequency = info.frequency

        // WifiInfo does not expose capabilities on the public API. On
        // API 30+, currentSecurityType is available; otherwise we
        // report UNKNOWN rather than relying on hidden APIs.
        val security = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                when (info.currentSecurityType) {
                    WifiInfo.SECURITY_TYPE_OPEN -> WifiSecurity.OPEN
                    WifiInfo.SECURITY_TYPE_WEP -> WifiSecurity.WEP
                    WifiInfo.SECURITY_TYPE_PSK -> WifiSecurity.WPA2_PSK
                    WifiInfo.SECURITY_TYPE_SAE -> WifiSecurity.WPA3_SAE
                    WifiInfo.SECURITY_TYPE_EAP -> WifiSecurity.EAP
                    WifiInfo.SECURITY_TYPE_OWE -> WifiSecurity.OWE
                    else -> WifiSecurity.UNKNOWN
                }
            }.getOrDefault(WifiSecurity.UNKNOWN)
        } else WifiSecurity.UNKNOWN

        val channel = freqToChannel(frequency)
        val pct = (((rssi + 100).coerceIn(0, 50)) * 2)
        return WifiConnection(
            ssid = ssid,
            bssid = bssid,
            rssiDbm = rssi,
            linkSpeedMbps = linkSpeed,
            frequencyMhz = frequency,
            channel = channel,
            security = security,
            signalPercent = pct,
        )
    }

    private fun freqToChannel(freqMhz: Int): Int = when {
        freqMhz == 0 -> 0
        freqMhz in 2412..2484 -> if (freqMhz == 2484) 14 else (freqMhz - 2412) / 5 + 1
        freqMhz in 5170..5825 -> (freqMhz - 5000) / 5
        freqMhz in 5945..7125 -> (freqMhz - 5950) / 5
        else -> 0
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun readIpInfo(): WifiIpInfo? {
        val cm = runCatching {
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        }.getOrNull() ?: return null
        val activeNetwork: Network = cm.activeNetwork ?: return null
        val lp: LinkProperties = runCatching { cm.getLinkProperties(activeNetwork) }.getOrNull() ?: return null
        val dhcp: DhcpInfo? = runCatching { wifiManager()?.dhcpInfo }.getOrNull()

        val localIp = lp.linkAddresses
            .mapNotNull { it.address }
            .filterIsInstance<Inet4Address>()
            .firstOrNull()?.hostAddress ?: intToIp(dhcp?.ipAddress ?: 0)
        val gateway = lp.routes
            .mapNotNull { it.gateway }
            .filterIsInstance<Inet4Address>()
            .firstOrNull()?.hostAddress ?: intToIp(dhcp?.gateway ?: 0)
        val dns1 = lp.dnsServers.filterIsInstance<Inet4Address>().firstOrNull()?.hostAddress
            ?: intToIp(dhcp?.dns1 ?: 0)
        val dns2 = lp.dnsServers.filterIsInstance<Inet4Address>().getOrNull(1)?.hostAddress
            ?: intToIp(dhcp?.dns2 ?: 0)

        return WifiIpInfo(
            localIp = localIp,
            gateway = gateway,
            dns1 = dns1,
            dns2 = dns2,
            dhcpServer = intToIp(dhcp?.serverAddress ?: 0),
        )
    }

    private fun intToIp(addr: Int): String = when (addr) {
        0 -> "—"
        else -> {
            val b0 = addr and 0xff
            val b1 = (addr shr 8) and 0xff
            val b2 = (addr shr 16) and 0xff
            val b3 = (addr shr 24) and 0xff
            "$b3.$b2.$b1.$b0"
        }
    }

    @SuppressLint("MissingPermission")
    private fun readCellularType(): String {
        val cm = runCatching {
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        }.getOrNull() ?: return "—"
        val activeNetwork = cm.activeNetwork ?: return "—"
        val caps = runCatching { cm.getNetworkCapabilities(activeNetwork) }.getOrNull() ?: return "—"
        return if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) "Cellular" else "—"
    }

    private fun readAppTraffic(): List<AppTrafficRow> {
        // Use the slim, icon-free list — the heavy `snapshot()` decodes
        // every app's icon bitmap and we don't need them here (the VM
        // loads icons lazily per visible row).
        val apps = runCatching { AppsProvider(appContext).simpleList() }.getOrNull().orEmpty()
        return apps.take(20).mapNotNull { info ->
            val uid = runCatching {
                appContext.packageManager.getApplicationInfo(info.packageName, 0).uid
            }.getOrNull() ?: return@mapNotNull null
            if (uid <= 0) return@mapNotNull null
            val rx = runCatching { TrafficStats.getUidRxBytes(uid) }.getOrNull() ?: 0L
            val tx = runCatching { TrafficStats.getUidTxBytes(uid) }.getOrNull() ?: 0L
            if (rx <= 0L && tx <= 0L) return@mapNotNull null
            AppTrafficRow(info.packageName, info.displayName, rx.coerceAtLeast(0L), tx.coerceAtLeast(0L))
        }.sortedByDescending { it.rxBytes + it.txBytes }.take(5)
    }
}