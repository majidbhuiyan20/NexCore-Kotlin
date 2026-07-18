package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.PhoneInfoProvider
import com.matox.nexcore.domain.model.PhoneInfoSnapshot
import com.matox.nexcore.domain.repository.PhoneInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhoneInfoRepositoryImpl(
    private val provider: PhoneInfoProvider,
) : PhoneInfoRepository {

    override suspend fun snapshot(): PhoneInfoSnapshot =
        withContext(Dispatchers.IO) { provider.snapshot() }
}
