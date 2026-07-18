package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.PhoneInfoSnapshot

/** Repository contract for the Phone Info screen. */
interface PhoneInfoRepository {
    /** Reads system information without blocking the caller's thread. */
    suspend fun snapshot(): PhoneInfoSnapshot
}
