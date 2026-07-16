package com.matox.nexcore.data.device

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.matox.nexcore.domain.model.SensorLiveMotion
import com.matox.nexcore.domain.model.SensorReading
import com.matox.nexcore.domain.model.SensorSnapshot
import com.matox.nexcore.domain.model.SensorType
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Streaming reader that produces a [SensorSnapshot] from on-device
 * `SensorManager` callbacks.
 *
 * Lifecycle & flow behavior:
 *  - On construction: enumerates every sensor the OS reports
 *    and pre-populates a `MutableStateFlow<SensorSnapshot>` with
 *    zero-value rows so the UI can render the list immediately.
 *  - A single [SensorEventListener] is registered on the
 *    *streamable* subset of sensors (motion, light, pressure, …)
 *    using [SensorManager.SENSOR_DELAY_GAME] (~50Hz). Events are
 *    coalesced inside [onSensorChanged] via a throttle so the
 *    StateFlow only emits at ~10 Hz even when the OS delivers at
 *    the full rate. This avoids Compose recompositions at full
 *    sensor cadence while still giving a smooth UI.
 *  - [register] and [unregister] are idempotent and safe to
 *    call from any thread. The host (a ViewModel-bound
 *    `Lifecycle.Event` observer) calls them on resume / pause
 *    to avoid draining battery when the screen isn't visible.
 *  - All public methods swallow exceptions: a single failed
 *    sensor (very rare in practice) degrades to a zero-value row
 *    rather than crashing the screen.
 *
 * Threading:
 *  - `SensorManager` callbacks come in on the main thread. We
 *    mutate the reading cache under [writeLock] and publish the
 *    resulting immutable snapshot via the StateFlow on the same
 *    thread (StateFlow.setValue is documented as thread-safe for
 *    the same value). The repo / ViewModel can subscribe on
 *    whatever dispatcher they like.
 */
class SensorProvider(
    private val appContext: Context,
) : SensorEventListener {

    private val sensorManager: SensorManager? = ContextCompat.getSystemService(
        appContext, SensorManager::class.java,
    )

    /** Backing flow — replaced atomically on every onSensorChanged
     *  batch. Initialised in [seedReadings] right below. */
    private val _snapshot = MutableStateFlow(emptySnapshot())
    val snapshot: StateFlow<SensorSnapshot> = _snapshot.asStateFlow()

    /**
     * Hot side-channel for "live motion" updates. The presentation
     * layer's hero card composes from this flow because we want it
     * to pulse independently of the full snapshot (which also
     * carries the static sensor list).
     */
    private val _motion = MutableSharedFlow<SensorLiveMotion>(
        replay = 1,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val motion: Flow<SensorLiveMotion> = _motion.asSharedFlow()

    /**
     * Per-type latest reading — mutated from `onSensorChanged`
     * (main thread) and copied into a new immutable snapshot on
     * each emission. Guarded by [writeLock] because
     * `onSensorChanged` is documented as called from the main
     * thread but some OEM ROMs deliver callbacks on a Handler
     * thread, and `buildSnapshot()` may be invoked from a
     * background dispatcher.
     */
    private val writeLock = Any()
    private val latestByType = HashMap<SensorType, FloatArray>()

    /** Map from domain [SensorType] to the framework [Sensor]. */
    private val sensorsByType = HashMap<SensorType, Sensor>()

    /** Map from framework [Sensor] back to our domain [SensorType]. */
    private val typeByFrameworkSensor = HashMap<Sensor, SensorType>()

    /** Throttle — last wall-clock ms a snapshot was emitted. */
    @Volatile private var lastSnapshotMs: Long = 0L

    // -------------------------------------------------------------------------
    // Construction / enumeration
    // -------------------------------------------------------------------------

    init {
        seedReadings()
    }

    /**
     * Populate the initial snapshot from `SensorManager.getSensorList`.
     * Sensors the OS doesn't report are simply omitted from the list —
     * the screen renders "N sensors" so a device with 12 sensors still
     * looks complete even when accelerometer is missing.
     *
     * Safe to call from any thread; runs once.
     */
    private fun seedReadings() {
        runCatching {
            val sm = sensorManager ?: return@runCatching
            val all = sm.getSensorList(Sensor.TYPE_ALL).orEmpty()
            val readings = ArrayList<SensorReading>(all.size)
            for (frameworkSensor in all) {
                val type = SensorType.fromAndroidType(frameworkSensor.type)
                    ?: continue
                sensorsByType[type] = frameworkSensor
                typeByFrameworkSensor[frameworkSensor] = type
                readings += SensorReading.empty(
                    sensorType = type,
                    name = frameworkSensor.name ?: type.label,
                    vendor = frameworkSensor.vendor ?: "—",
                    unit = type.unit,
                )
            }
            _snapshot.value = buildSnapshotFrom(readings, activeCount = 0)
        }
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Register [SensorEventListener] callbacks on every streamable
     * sensor reported by the OS. Idempotent — calling twice is a
     * no-op (we unregister first to keep semantics predictable
     * across screen rotations and process restarts).
     */
    fun register() {
        runCatching {
            val sm = sensorManager ?: return@runCatching
            unregister() // make idempotent
            for ((type, sensor) in sensorsByType) {
                if (type.streamable) {
                    sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
                }
            }
        }
    }

    /**
     * Tear down every registered listener. Safe to call from any
     * thread, even if no listener was registered.
     */
    fun unregister() {
        runCatching {
            sensorManager?.unregisterListener(this)
        }
    }

    // -------------------------------------------------------------------------
    // SensorEventListener
    // -------------------------------------------------------------------------

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val type = typeByFrameworkSensor[event.sensor] ?: return
        val values = event.values
        if (values == null || values.isEmpty()) return

        // Throttle snapshot publication so we don't push to the
        // StateFlow on every event (SENSOR_DELAY_GAME ≈ 50 Hz).
        // Hero card animations look smooth at 10 Hz and we keep
        // recomposition cost proportional to the screen refresh
        // rate.
        val now = SystemClock.elapsedRealtime()
        synchronized(writeLock) {
            latestByType[type] = values.copyOf()
            if (now - lastSnapshotMs < MIN_EMISSION_INTERVAL_MS) {
                // Still publish motion since the hero pulses each
                // tick — but skip the heavy full-snapshot copy.
                emitMotionOnly(type, values)
                return
            }
            lastSnapshotMs = now
        }

        publishSnapshot()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for now — accuracy contributes to a future
        // "trust" badge on each row but isn't surfaced today.
    }

    // -------------------------------------------------------------------------
    // Snapshot helpers
    // -------------------------------------------------------------------------

    /**
     * Push a [SensorLiveMotion] update without rebuilding the full
     * snapshot — used when throttled so the hero card can still
     * pulse at sensor rate.
     */
    private fun emitMotionOnly(type: SensorType, values: FloatArray) {
        val motion = computeMotion(type, values)
        if (motion.active) {
            _motion.tryEmit(motion)
        }
    }

    /**
     * Build a [SensorSnapshot] from the latest reading cache.
     * Marks the corresponding row as `active = true` for every
     * sensor that's produced at least one event.
     */
    private fun publishSnapshot() {
        val updated = currentUpdatedReadings()
        val active = updated.count { it.active }
        val accel = updated.firstOrNull { it.sensorType == SensorType.ACCELEROMETER }
        val gyro = updated.firstOrNull { it.sensorType == SensorType.GYROSCOPE }
        val snap = _snapshot.value.copy(
            readings = updated,
            activeCount = active,
            accelerometer = accel,
            gyroscope = gyro,
            hasAnyMotion = updated.any { it.active && it.sensorType.motionRelated },
        )
        _snapshot.value = snap
        val motion = computeMotionFrom(snap)
        if (motion.active) _motion.tryEmit(motion)
    }

    /**
     * One-shot snapshot built from the latest readings. Public
     * so the repository's `refreshNow()` can re-publish an
     * up-to-date snapshot even if no new event has arrived
     * (e.g. after the user toggles a privacy setting).
     */
    fun refresh(): SensorSnapshot {
        val updated = currentUpdatedReadings()
        val snap = buildSnapshotFrom(
            readings = updated,
            activeCount = updated.count { it.active },
        )
        _snapshot.value = snap
        val motion = computeMotionFrom(snap)
        if (motion.active) _motion.tryEmit(motion)
        return snap
    }

    /**
     * Snapshot helper — apply the latest reading cache to every
     * row in the current StateFlow value. Used by both
     * [publishSnapshot] and [refresh] so the two paths stay in
     * lock-step.
     */
    private fun currentUpdatedReadings(): List<SensorReading> {
        val current = _snapshot.value.readings
        return current.map { row ->
            val latest = synchronized(writeLock) { latestByType[row.sensorType] }
            if (latest != null && latest.size >= row.values.size) {
                row.copy(
                    values = FloatArray(row.values.size) { latest[it] },
                    active = true,
                )
            } else row
        }
    }

    private fun buildSnapshotFrom(
        readings: List<SensorReading>,
        activeCount: Int,
    ): SensorSnapshot {
        val accel = readings.firstOrNull { it.sensorType == SensorType.ACCELEROMETER }
        val gyro = readings.firstOrNull { it.sensorType == SensorType.GYROSCOPE }
        return SensorSnapshot(
            readings = readings,
            activeCount = activeCount,
            accelerometer = accel,
            gyroscope = gyro,
            hasAnyMotion = readings.any { it.active && it.sensorType.motionRelated },
        )
    }

    private fun emptySnapshot(): SensorSnapshot = SensorSnapshot()

    // -------------------------------------------------------------------------
    // Motion helpers
    // -------------------------------------------------------------------------

    /**
     * Magnitude helper used by the throttled motion-only path.
     * Returns a [SensorLiveMotion] whose `active` flag is true
     * when the firing sensor is accel/gyro (the two that drive
     * the hero card).
     */
    private fun computeMotion(type: SensorType, values: FloatArray): SensorLiveMotion {
        val magnitude = magnitudeFor(type, values)
        // For accel events we update the accel magnitude; for gyro
        // events we update the gyro magnitude. Other sensors are
        // skipped so the hero card keeps showing the last known
        // accel/gyro values until a fresh accel/gyro event lands.
        val replayed = _motion.replayCache.lastOrNull() ?: SensorLiveMotion.Empty
        return when (type) {
            SensorType.ACCELEROMETER,
            SensorType.GRAVITY,
            SensorType.LINEAR_ACCELERATION -> SensorLiveMotion(
                accelerometerMagnitude = magnitude,
                gyroscopeMagnitude = replayed.gyroscopeMagnitude,
                active = true,
            )
            SensorType.GYROSCOPE -> SensorLiveMotion(
                accelerometerMagnitude = replayed.accelerometerMagnitude,
                gyroscopeMagnitude = magnitude,
                active = true,
            )
            else -> SensorLiveMotion(
                accelerometerMagnitude = replayed.accelerometerMagnitude,
                gyroscopeMagnitude = replayed.gyroscopeMagnitude,
                active = false,
            )
        }
    }

    private fun computeMotionFrom(snap: SensorSnapshot): SensorLiveMotion {
        val a = magnitudeForValues(snap.accelerometer?.values)
        val g = magnitudeForValues(snap.gyroscope?.values)
        return SensorLiveMotion(
            accelerometerMagnitude = a,
            gyroscopeMagnitude = g,
            active = (a + g) > 0f,
        )
    }

    private fun magnitudeFor(type: SensorType, values: FloatArray): Float {
        val supports3Axis = type == SensorType.ACCELEROMETER ||
            type == SensorType.GYROSCOPE ||
            type == SensorType.GRAVITY ||
            type == SensorType.LINEAR_ACCELERATION ||
            type == SensorType.MAGNETOMETER
        return if (supports3Axis && values.size >= 3) {
            sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
        } else {
            0f
        }
    }

    private fun magnitudeForValues(values: FloatArray?): Float {
        if (values == null || values.size < 3) return 0f
        return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    }

    companion object {
        /**
         * 100 ms = 10 Hz. Below this cadence Compose anims still
         * look smooth but we drop recomposition pressure by 5×
         * compared to the raw ~50 Hz sensor rate.
         */
        private const val MIN_EMISSION_INTERVAL_MS: Long = 100L
    }
}