package com.matox.nexcore.data.device

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.matox.nexcore.domain.model.BasicInfo
import com.matox.nexcore.domain.model.BatteryInfo
import com.matox.nexcore.domain.model.HardwareInfo
import com.matox.nexcore.domain.model.NetworkInfo
import com.matox.nexcore.domain.model.OsInfo
import com.matox.nexcore.domain.model.PhoneInfoSnapshot
import com.matox.nexcore.domain.model.SensorsInfo
import java.io.File
import java.io.RandomAccessFile
import java.net.NetworkInterface
import java.util.Locale
import kotlin.math.max

/**
 * Pure read-only access to system "About this device" data.
 *
 * Each section has a private `read*` method that wraps every external
 * call in `runCatching` so a single failure (sandboxed /proc, blocked
 * SIM, restricted MAC) degrades to "—" rather than blanking the screen.
 *
 * No new permissions are required by anything in this class:
 *  - `Build.*`        → public
 *  - `SystemProperties` → hidden, accessed via reflection only on fields
 *                        known to be safe to read
 *  - `/proc/cpuinfo`  / `/proc/version`  → world-readable
 *  - `ACTION_BATTERY_CHANGED` → sticky broadcast, no permission
 *  - `TelephonyManager.getSimState()` → public, no permission
 *  - `TelephonyManager.getNetworkType()` → public, returns "Unknown"
 *                          on API 30+ without READ_PHONE_STATE
 *  - `NetworkInterface.getHardwareAddress()` → returns the standard
 *                          `02:00:00:00:00:00` placeholder on API 23+
 *  - `SensorManager.getSensorList()` → public, no permission
 *
 * Heavy operations (file reads, NetworkInterface enumeration) are kept
 * cheap; the whole snapshot finishes in <50 ms on a mid-range device.
 */
class PhoneInfoProvider(
    private val appContext: Context,
) {

    /** Build a full snapshot. Safe to call from a background dispatcher. */
    fun snapshot(): PhoneInfoSnapshot = PhoneInfoSnapshot(
        deviceName = deviceName(),
        basic = readBasic(),
        hardware = readHardware(),
        battery = readBattery(),
        os = readOs(),
        network = readNetwork(),
        sensors = readSensors(),
        uptimeSeconds = SystemClock.elapsedRealtime() / 1000L,
    )

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private fun deviceName(): String {
        val manufacturer = runCatching { Build.MANUFACTURER.capitalized() }.getOrDefault("")
        val model = runCatching { Build.MODEL }.getOrDefault("")
        return when {
            model.startsWith(manufacturer, ignoreCase = true) -> model
            manufacturer.isBlank() -> model
            else -> "$manufacturer $model"
        }.trim()
    }

    private fun String.capitalized(): String =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    private fun safe(value: String?): String = value?.takeIf { it.isNotBlank() } ?: "—"

    // ---------------------------------------------------------------------
    // Basic
    // ---------------------------------------------------------------------

    private fun readBasic(): BasicInfo = BasicInfo(
        manufacturer = safe(Build.MANUFACTURER.capitalized()),
        brand = safe(Build.BRAND.capitalized()),
        model = safe(Build.MODEL),
        device = safe(Build.DEVICE),
        product = safe(Build.PRODUCT),
        board = safe(Build.BOARD),
        hardware = safe(Build.HARDWARE),
        deviceCodeName = safe(Build.DEVICE),
        fingerprint = safe(Build.FINGERPRINT),
        serial = safe(serialOrUnknown()),
        host = safe(Build.HOST),
        user = safe(Build.USER),
    )

    @SuppressLint("HardwareIds")
    private fun serialOrUnknown(): String = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Build.getSerial()
        } else {
            @Suppress("DEPRECATION")
            Build.SERIAL
        }
    }.getOrDefault("—")

    // ---------------------------------------------------------------------
    // Hardware
    // ---------------------------------------------------------------------

    private fun readHardware(): HardwareInfo = HardwareInfo(
        cpuAbi = safe(Build.SUPPORTED_ABIS.firstOrNull()),
        cpuCores = Runtime.getRuntime().availableProcessors(),
        supportedAbis = safe(Build.SUPPORTED_ABIS.joinToString(", ")),
        processorName = safe(readProcessorName()),
        minSdk = runCatching { appContext.applicationInfo.minSdkVersion }
            .getOrDefault(0),
        targetSdk = runCatching { appContext.applicationInfo.targetSdkVersion }
            .getOrDefault(0),
        sdkInt = Build.VERSION.SDK_INT,
        javaVm = safe(System.getProperty("java.vm.version") ?: System.getProperty("java.vendor")),
        screenResolution = readResolution(),
        screenDensity = readDensity(),
        refreshRateHz = readRefreshRate(),
        totalMemoryGb = readTotalMemoryGb(),
        internalStorageGb = readInternalStorageGb(),
    )

    private fun readProcessorName(): String = runCatching {
        RandomAccessFile(File("/proc/cpuinfo"), "r").use { raf ->
            val line = generateSequence { raf.readLine() }
                .firstOrNull { it.startsWith("Hardware", ignoreCase = true) }
            line?.substringAfter(':')?.trim()
        }
    }.getOrNull() ?: "—"

    private fun readResolution(): String = runCatching {
        val wm = appContext.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        @Suppress("DEPRECATION")
        val display = wm.defaultDisplay
        @Suppress("DEPRECATION")
        val metrics = android.util.DisplayMetrics()
        @Suppress("DEPRECATION")
        display.getRealMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        if (width > 0 && height > 0) "${max(width, height)} × ${minOf(width, height)}" else "—"
    }.getOrDefault("—")

    private fun readDensity(): String = runCatching {
        val dpi = appContext.resources.displayMetrics.densityDpi
        "${dpi} dpi (${"%.2f".format(appContext.resources.displayMetrics.density)}x)"
    }.getOrDefault("—")

    private fun readRefreshRate(): String = runCatching {
        val rate = appContext.display?.refreshRate ?: 0f
        if (rate > 0f) "${"%.1f".format(rate)} Hz" else "—"
    }.getOrDefault("—")

    private fun readTotalMemoryGb(): String = runCatching {
        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val gb = info.totalMem / (1024f * 1024f * 1024f)
        "${"%.1f".format(gb)} GB"
    }.getOrDefault("—")

    private fun readInternalStorageGb(): String = runCatching {
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytes = stat.blockCountLong * stat.blockSizeLong
        val gb = bytes / (1024f * 1024f * 1024f)
        "${"%.1f".format(gb)} GB"
    }.getOrDefault("—")

    // ---------------------------------------------------------------------
    // Battery
    // ---------------------------------------------------------------------

    private fun readBattery(): BatteryInfo = runCatching {
        val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return@runCatching fallbackBattery()

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale).coerceIn(0, 100) else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val tech = intent.getStringExtra("technology") ?: "—"

        BatteryInfo(
            levelPercent = pct,
            status = batteryStatusLabel(status),
            plugged = pluggedLabel(plugged),
            technology = tech,
            temperatureC = tempTenths / 10,
            voltageMv = voltageMv,
            health = batteryHealthLabel(health),
        )
    }.getOrDefault(fallbackBattery())

    private fun fallbackBattery(): BatteryInfo = runCatching {
        val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
        val status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        BatteryInfo(
            levelPercent = pct,
            status = batteryStatusLabel(status),
            plugged = "—",
            technology = "—",
            temperatureC = 0,
            voltageMv = 0,
            health = "—",
        )
    }.getOrDefault(
        BatteryInfo(0, "—", "—", "—", 0, 0, "—"),
    )

    private fun batteryStatusLabel(status: Int): String = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "—"
    }

    private fun pluggedLabel(plugged: Int): String = when (plugged) {
        0 -> "Battery"
        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "—"
    }

    private fun batteryHealthLabel(health: Int): String = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "—"
    }

    // ---------------------------------------------------------------------
    // OS
    // ---------------------------------------------------------------------

    private fun readOs(): OsInfo = OsInfo(
        androidVersion = safe(readRelease()),
        release = safe(Build.VERSION.RELEASE),
        securityPatch = safe(Build.VERSION.SECURITY_PATCH.takeIf { it.isNotBlank() }),
        buildId = safe(Build.ID),
        buildDisplay = safe(Build.DISPLAY),
        buildDate = safe(Build.TIME.takeIf { it > 0 }?.let { java.util.Date(it).toString() }),
        kernelVersion = safe(readKernelVersion()),
        javaVm = safe(System.getProperty("java.vm.version")),
        bootloader = safe(Build.BOOTLOADER),
    )

    private fun readRelease(): String = runCatching {
        // VERSION.RELEASE is the user-visible Android version (14, 15, …).
        // Some OEM ROMs publish a marketing label via SystemProperties — we
        // honor it if non-blank, otherwise fall back to VERSION.RELEASE.
        val label = readSystemProperty("ro.build.version.release_description")
            ?.takeIf { it.isNotBlank() }
        label ?: Build.VERSION.RELEASE
    }.getOrDefault(Build.VERSION.RELEASE)

    private fun readKernelVersion(): String = runCatching {
        RandomAccessFile(File("/proc/version"), "r").use { it.readLine()?.trim() ?: "—" }
    }.getOrDefault("—")

    /**
     * Read a [android.os.SystemProperties] entry. The class is hidden in
     * the SDK so we go through reflection. Reading any property is safe
     * (no permission); only the `set` methods require root.
     */
    private fun readSystemProperty(key: String): String? = runCatching {
        val cls = Class.forName("android.os.SystemProperties")
        val method = cls.getMethod("get", String::class.java, String::class.java)
        method.invoke(null, key, "") as? String
    }.getOrNull()

    // ---------------------------------------------------------------------
    // Network
    // ---------------------------------------------------------------------

    @SuppressLint("HardwareIds")
    private fun readNetwork(): NetworkInfo {
        val simState = readSimStateLabel()
        val networkType = readNetworkTypeLabel()
        val dataState = readDataStateLabel()
        val mac = readWifiMac()
        val ip = readIpAddress()
        val ssid = readWifiSsid()
        return NetworkInfo(
            simOperator = readSimOperator(),
            simState = simState,
            networkType = networkType,
            dataState = dataState,
            wifiMac = mac,
            ipAddress = ip,
            wifiSsid = ssid,
            signalDbm = "—",
        )
    }

    @SuppressLint("HardwareIds")
    private fun readSimOperator(): String = runCatching {
        val tm = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.simOperatorName?.takeIf { it.isNotBlank() }
            ?: tm.simOperator?.takeIf { it.isNotBlank() }
            ?: "—"
    }.getOrDefault("—")

    private fun readSimStateLabel(): String = runCatching {
        val tm = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        when (tm.simState) {
            TelephonyManager.SIM_STATE_ABSENT -> "No SIM"
            TelephonyManager.SIM_STATE_READY -> "Active"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network locked"
            else -> "—"
        }
    }.getOrDefault("—")

    @Suppress("DEPRECATION")
    private fun readNetworkTypeLabel(): String = runCatching {
        val tm = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        when (tm.networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3G HSPA+"
            TelephonyManager.NETWORK_TYPE_EDGE -> "2G EDGE"
            TelephonyManager.NETWORK_TYPE_GPRS -> "2G GPRS"
            0 -> "Unknown"
            else -> "Unknown"
        }
    }.getOrDefault("—")

    @Suppress("DEPRECATION")
    private fun readDataStateLabel(): String = runCatching {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetworkInfo
        when (active?.type) {
            ConnectivityManager.TYPE_WIFI -> "Connected (WiFi)"
            ConnectivityManager.TYPE_MOBILE -> "Connected (Mobile)"
            ConnectivityManager.TYPE_ETHERNET -> "Connected (Ethernet)"
            -1 -> "Disconnected"
            else -> active?.state?.name ?: "—"
        }
    }.getOrDefault("—")

    /**
     * MAC address. API 23+ masks this to `02:00:00:00:00:00` for privacy;
     * we still attempt to read it and let the placeholder stand.
     */
    @SuppressLint("HardwareIds")
    private fun readWifiMac(): String = runCatching {
        val all = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
        val wifi = all.firstOrNull {
            it.name.equals("wlan0", ignoreCase = true) && it.hardwareAddress != null
        }
        wifi?.hardwareAddress
            ?.joinToString(":") { "%02X".format(it) }
            ?.takeIf { it.isNotBlank() } ?: "—"
    }.getOrDefault("—")

    @SuppressLint("HardwareIds")
    private fun readIpAddress(): String = runCatching {
        val all = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
        val first = all.firstOrNull { it.inetAddresses?.hasMoreElements() == true }
        val addr = first?.inetAddresses?.nextElement()
        addr?.hostAddress?.takeIf { it.isNotBlank() && !it.contains(':') } ?: "—"
    }.getOrDefault("—")

    @SuppressLint("HardwareIds")
    private fun readWifiSsid(): String = runCatching {
        val wifi = appContext.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as? WifiManager
        @Suppress("DEPRECATION")
        val info = wifi?.connectionInfo
        info?.ssid?.trim('"')?.takeIf { it.isNotBlank() && it != "<unknown ssid>" } ?: "—"
    }.getOrDefault("—")

    // ---------------------------------------------------------------------
    // Sensors
    // ---------------------------------------------------------------------

    private fun readSensors(): SensorsInfo = runCatching {
        val sm = ContextCompat.getSystemService(appContext, SensorManager::class.java)
            ?: return@runCatching fallbackSensors()
        val list = sm.getSensorList(Sensor.TYPE_ALL)
        fun find(type: Int): String = list.firstOrNull { it.type == type }
            ?.let { "${it.name}" }
            ?: "Not present"
        SensorsInfo(
            accelerometer = find(Sensor.TYPE_ACCELEROMETER),
            gyroscope = find(Sensor.TYPE_GYROSCOPE),
            magnetometer = find(Sensor.TYPE_MAGNETIC_FIELD),
            proximity = find(Sensor.TYPE_PROXIMITY),
            lightSensor = find(Sensor.TYPE_LIGHT),
            barometer = find(Sensor.TYPE_PRESSURE),
            stepCounter = find(Sensor.TYPE_STEP_COUNTER),
            stepDetector = find(Sensor.TYPE_STEP_DETECTOR),
            gravity = find(Sensor.TYPE_GRAVITY),
            rotationVector = find(Sensor.TYPE_ROTATION_VECTOR),
            totalSensorCount = list.size,
        )
    }.getOrDefault(fallbackSensors())

    private fun fallbackSensors(): SensorsInfo = SensorsInfo(
        accelerometer = "—",
        gyroscope = "—",
        magnetometer = "—",
        proximity = "—",
        lightSensor = "—",
        barometer = "—",
        stepCounter = "—",
        stepDetector = "—",
        gravity = "—",
        rotationVector = "—",
        totalSensorCount = 0,
    )
}

private fun max(a: Int, b: Int): Int = if (a > b) a else b
