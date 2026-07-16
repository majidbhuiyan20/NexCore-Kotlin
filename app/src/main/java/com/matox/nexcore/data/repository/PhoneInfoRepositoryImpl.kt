package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.PhoneInfoProvider
import com.matox.nexcore.domain.model.PhoneInfoSnapshot
import com.matox.nexcore.domain.repository.PhoneInfoRepository

/**
 * Thin pass-through to [PhoneInfoProvider]. The provider does the
 * heavy lifting (every external call is already wrapped in runCatching);
 * the repository only adds a stable domain-layer seam for the ViewModel.
 */
class PhoneInfoRepositoryImpl(
    private val provider: PhoneInfoProvider,
) : PhoneInfoRepository {

    override fun snapshot(): PhoneInfoSnapshot = provider.snapshot()
}
