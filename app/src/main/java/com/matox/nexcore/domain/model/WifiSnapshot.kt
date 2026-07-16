package com.matox.nexcore.domain.model

/**
 * Domain model for the WiFi & Network screen.
 *
 * - [connection] describes the currently-associated Wi-Fi link (or
 *   `null` when the device is offline / on cellular only).
 * - [ip] is the local IP / gateway / DNS / DHCP server from
 *   `DhcpInfo`.
 * - [publicIp] is the result of a one-shot HTTPS lookup (or `null`
 *   when the call failed / hasn't completed yet).
 * - [appTraffic] is per-UID byte counters via `TrafficStats`, sorted
 *   by total bytes — same approach as the Data Usage screen.
 * - [cellularType] is "5G" / "4G" / "3G" / "—".
 */
data class WifiSnapshot(
    val connection: WifiConnection?,
    val ip: WifiIpInfo?,
    val publicIp: String?,
    val appTraffic: List<AppTrafficRow>,
    val cellularType: String,
)

/**
 * Currently-associated Wi-Fi link — empty when the device isn't on
 * Wi-Fi.
 */
data class WifiConnection(
    val ssid: String,
    val bssid: String,
    val rssiDbm: Int,
    val linkSpeedMbps: Int,
    /** Center frequency in MHz — used to derive 2.4 / 5 / 6 GHz band. */
    val frequencyMhz: Int,
    val channel: Int,
    val security: WifiSecurity,
    val signalPercent: Int,
)

/** Wi-Fi security mode mapped from `WifiInfo`'s capabilities. */
enum class WifiSecurity {
    OPEN,
    WEP,
    WPA_PSK,
    WPA2_PSK,
    WPA3_SAE,
    EAP,
    OWE,
    UNKNOWN,
}

data class WifiIpInfo(
    val localIp: String,
    val gateway: String,
    val dns1: String,
    val dns2: String,
    val dhcpServer: String,
)

data class AppTrafficRow(
    val packageName: String,
    val displayName: String,
    val rxBytes: Long,
    val txBytes: Long,
)