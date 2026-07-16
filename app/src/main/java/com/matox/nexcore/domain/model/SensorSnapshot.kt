package com.matox.nexcore.domain.model

/**
 * Snapshot of all device sensors exposed by
 * [com.matox.nexcore.data.device.SensorProvider].
 *
 * The Sensor Monitor screen renders this in three places:
 *  - the hero card (accel/gyro magnitudes + status pill),
 *  - the dynamic list (every sensor the OS reports),
 *  - the "details" deep-link button (open privacy or app details).
 *
 * - [readings]: full enumeration of every sensor the OS reports,
 *   including ones we don't actively stream live. Each entry
 *   carries its most recent reading (or zeros if the OS hasn't
 *   delivered any events for it).
 * - [activeCount]: count of sensors that have produced at least
 *   one event since the provider started — cheap "how many
 *   sensors are live" badge.
 * - [accelerometer] / [gyroscope]: convenience pointers into
 *   [readings] so the hero card doesn't have to scan the full
 *   list to find them.
 * - [hasAnyMotion]: true when at least one motion-related sensor
 *   (accel, gyro, gravity, lin accel, rotation vector, step
 *   counter / detector) is present — drives the "motion support"
 *   affordance on the details button.
 *
 * All fields have sensible defaults so the UI can render an empty
 * device (no sensors at all) without null-checks.
 */
data class SensorSnapshot(
    val readings: List<SensorReading> = emptyList(),
    val activeCount: Int = 0,
    val accelerometer: SensorReading? = null,
    val gyroscope: SensorReading? = null,
    val hasAnyMotion: Boolean = false,
) {
    /**
     * Convenience — total sensors reported by the OS. The list card
     * uses this to render an "N sensors" subtitle.
     */
    val totalSensorCount: Int get() = readings.size
}

/**
 * A single sensor row.
 *
 * - [sensorType]: canonical [SensorType] key — both the provider
 *   and the UI rely on this enum to render the right icon and
 *   format the values.
 * - [name]: human-readable sensor name from the framework
 *   (`Sensor.getName()`) — shown verbatim in the list.
 * - [vendor]: manufacturer string from the OS — shown verbatim
 *   as a tiny secondary line in the details row.
 * - [values]: most recent event values. Length matches the
 *   sensor's channel count (3 for accel, 1 for light, etc).
 * - [unit]: a short unit label (`m/s²`, `µT`, `lx`, …) suitable
 *   for direct UI use.
 * - [active]: true once the provider has received at least one
 *   event from the OS — drives the "live" badge in the list.
 */
data class SensorReading(
    val sensorType: SensorType,
    val name: String,
    val vendor: String,
    val values: FloatArray,
    val unit: String,
    val active: Boolean = false,
) {
    /**
     * Equality on [FloatArray] requires contentEquals — Kotlin
     * synthesised `equals` on data classes would otherwise fall
     * back to reference identity for the array, which breaks
     * snapshot diffing.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SensorReading) return false
        if (sensorType != other.sensorType) return false
        if (name != other.name) return false
        if (vendor != other.vendor) return false
        if (unit != other.unit) return false
        if (active != other.active) return false
        if (!values.contentEquals(other.values)) return false
        return true
    }

    /**
     * Same rationale as [equals] — FloatArray needs a manual
     * hashCode so the snapshot's [List] comparisons don't fall
     * back to identity.
     */
    override fun hashCode(): Int {
        var result = sensorType.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + vendor.hashCode()
        result = 31 * result + values.contentHashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + active.hashCode()
        return result
    }

    companion object {
        /**
         * Build an empty ("never produced") row for a given sensor
         * type. The list view falls back to this when the device
         * reports a sensor the provider hasn't received any events
         * for yet (or whose listening failed).
         */
        fun empty(
            sensorType: SensorType,
            name: String,
            vendor: String,
            unit: String,
        ): SensorReading = SensorReading(
            sensorType = sensorType,
            name = name,
            vendor = vendor,
            values = FloatArray(sensorType.valueCount) { 0f },
            unit = unit,
            active = false,
        )
    }
}

/**
 * Canonical sensor-type identifiers — a strict subset of
 * `android.hardware.Sensor` constants. We avoid importing the
 * SDK type into the domain model so the presentation layer
 * doesn't depend on `android.hardware.*` symbols (which make
 * pure Kotlin tests harder).
 *
 * Each entry carries:
 *  - [label]: short UI label ("Accel", "Gyro", …).
 *  - [valueCount]: number of channels (accel = 3, light = 1, …).
 *  - [unit]: display unit string ("m/s²", "µT", …).
 *  - [iconKey]: semantic icon key — the presentation layer
 *    resolves these to [androidx.compose.material.icons] vecs.
 *  - [streamable]: true for sensors that should be actively
 *    listened to on the screen (we don't stream proximity to
 *    save battery, etc.).
 *  - [motionRelated]: subset used for the "this device has
 *    motion sensors" affordance.
 */
enum class SensorType(
    val label: String,
    val valueCount: Int,
    val unit: String,
    val iconKey: SensorIcon,
    val streamable: Boolean,
    val motionRelated: Boolean,
) {
    ACCELEROMETER("Accelerometer", 3, "m/s²", SensorIcon.ACCEL, streamable = true, motionRelated = true),
    GYROSCOPE("Gyroscope", 3, "rad/s", SensorIcon.GYRO, streamable = true, motionRelated = true),
    MAGNETOMETER("Magnetometer", 3, "µT", SensorIcon.MAGNETO, streamable = true, motionRelated = false),
    GRAVITY("Gravity", 3, "m/s²", SensorIcon.GRAVITY, streamable = true, motionRelated = true),
    LINEAR_ACCELERATION("Linear Accel", 3, "m/s²", SensorIcon.LIN_ACCEL, streamable = true, motionRelated = true),
    ROTATION_VECTOR("Rotation Vector", 4, "", SensorIcon.ROTATION, streamable = true, motionRelated = true),
    GAME_ROTATION_VECTOR("Game Rotation", 3, "", SensorIcon.ROTATION, streamable = true, motionRelated = true),
    STEP_COUNTER("Step Counter", 1, "steps", SensorIcon.STEPS, streamable = false, motionRelated = true),
    STEP_DETECTOR("Step Detector", 1, "", SensorIcon.STEPS, streamable = false, motionRelated = true),
    SIGNIFICANT_MOTION("Significant Motion", 1, "", SensorIcon.MOTION, streamable = false, motionRelated = true),
    PROXIMITY("Proximity", 1, "cm", SensorIcon.PROXIMITY, streamable = false, motionRelated = false),
    LIGHT("Light", 1, "lx", SensorIcon.LIGHT, streamable = true, motionRelated = false),
    PRESSURE("Pressure", 1, "hPa", SensorIcon.PRESSURE, streamable = true, motionRelated = false),
    AMBIENT_TEMPERATURE("Ambient Temp", 1, "°C", SensorIcon.THERMO, streamable = false, motionRelated = false),
    RELATIVE_HUMIDITY("Humidity", 1, "%", SensorIcon.HUMIDITY, streamable = false, motionRelated = false),
    HEART_RATE("Heart Rate", 1, "bpm", SensorIcon.HEART, streamable = false, motionRelated = false),
    ;

    companion object {
        /**
         * Lookup a [SensorType] by its numeric `Sensor` type —
         * the provider passes through `Sensor.getType()` int
         * values that aren't part of the public Android SDK
         * constants on every API level, so we keep the
         * authoritative mapping here.
         */
        fun fromAndroidType(androidType: Int): SensorType? = when (androidType) {
            android.hardware.Sensor.TYPE_ACCELEROMETER -> ACCELEROMETER
            android.hardware.Sensor.TYPE_GYROSCOPE -> GYROSCOPE
            android.hardware.Sensor.TYPE_MAGNETIC_FIELD -> MAGNETOMETER
            android.hardware.Sensor.TYPE_GRAVITY -> GRAVITY
            android.hardware.Sensor.TYPE_LINEAR_ACCELERATION -> LINEAR_ACCELERATION
            android.hardware.Sensor.TYPE_ROTATION_VECTOR -> ROTATION_VECTOR
            android.hardware.Sensor.TYPE_GAME_ROTATION_VECTOR -> GAME_ROTATION_VECTOR
            android.hardware.Sensor.TYPE_STEP_COUNTER -> STEP_COUNTER
            android.hardware.Sensor.TYPE_STEP_DETECTOR -> STEP_DETECTOR
            android.hardware.Sensor.TYPE_SIGNIFICANT_MOTION -> SIGNIFICANT_MOTION
            android.hardware.Sensor.TYPE_PROXIMITY -> PROXIMITY
            android.hardware.Sensor.TYPE_LIGHT -> LIGHT
            android.hardware.Sensor.TYPE_PRESSURE -> PRESSURE
            android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE -> AMBIENT_TEMPERATURE
            android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY -> RELATIVE_HUMIDITY
            android.hardware.Sensor.TYPE_HEART_RATE -> HEART_RATE
            else -> null
        }
    }
}

/**
 * Semantic icon keys for the sensor list — the presentation
 * layer maps these to `androidx.compose.material.icons` values
 * in a small helper, keeping Compose symbols out of the domain
 * package.
 */
enum class SensorIcon {
    ACCEL,
    GYRO,
    MAGNETO,
    GRAVITY,
    LIN_ACCEL,
    ROTATION,
    STEPS,
    MOTION,
    PROXIMITY,
    LIGHT,
    PRESSURE,
    THERMO,
    HUMIDITY,
    HEART,
}

/**
 * Derived live-telemetry state for the hero card. Kept as a
 * separate type so the rest of the snapshot (`SensorSnapshot`)
 * stays a pure data shape that can be safely compared.
 */
data class SensorLiveMotion(
    /** Composite accel magnitude (`sqrt(x²+y²+z²)`) in m/s². */
    val accelerometerMagnitude: Float = 0f,
    /** Composite gyro magnitude in rad/s. */
    val gyroscopeMagnitude: Float = 0f,
    /** True when at least one motion sensor has fired in this tick. */
    val active: Boolean = false,
) {
    companion object {
        val Empty = SensorLiveMotion()
    }
}
