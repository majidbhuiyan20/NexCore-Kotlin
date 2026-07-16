package com.matox.nexcore.data.device

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap

/**
 * Lightweight icon loader for the RAM detail screen.
 *
 * Unlike [AppsProvider.snapshot] (which builds a full `AppInfo` row
 * per app including size, version, icon, etc.), this loader only
 * fetches the icon bitmap for a single package — the RAM screen needs
 * to show many apps at once and only cares about the icon.
 *
 * Icons are downscaled to 64×64 px so memory stays bounded; with 8
 * apps on screen this is ~64 KB total.
 */
class AppIconLoader(
    private val appContext: Context,
) {

    /** Returns a 64×64 bitmap for [packageName] or null if unavailable. */
    fun load(packageName: String): Bitmap? = try {
        val pm = appContext.packageManager
        val drawable = pm.getApplicationIcon(packageName)
        drawable.toBitmap(width = ICON_PX, height = ICON_PX)
    } catch (_: Throwable) {
        null
    }

    private companion object {
        const val ICON_PX = 64
    }
}