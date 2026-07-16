package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.PhoneInfoSnapshot

/**
 * Repository contract for the Phone Info screen.
 *
 * snapshot() is a synchronous read meant to be called off the main
 * thread. The implementation is responsible for fanning out to Android
 * system APIs (Build, SystemProperties via reflection, BatteryManager,
 * TelephonyManager, SensorManager, NetworkInterface, etc.) and for
 * wrapping every external call in runCatching so a single failure
 * never blanks the whole screen.
 */
interface PhoneInfoRepository {
    fun snapshot(): PhoneInfoSnapshot
}
