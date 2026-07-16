package com.matox.nexcore.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Storage-related permission helpers.
 *
 * On Android 13+ the new granular permissions are:
 *   READ_MEDIA_IMAGES  — for photos
 *   READ_MEDIA_VIDEO   — for videos
 *   READ_MEDIA_AUDIO   — for audio files
 *
 * On Android 12 and below the legacy READ_EXTERNAL_STORAGE covers
 * everything media-related.
 *
 * READ_MEDIA_* are runtime permissions that must be requested at
 * runtime via `ActivityResultContracts.RequestMultiplePermissions()`.
 * They are NOT dangerous in the sense that they require user education
 * — granting them just lets the app see what's already in MediaStore.
 *
 * On devices that return `READ_EXTERNAL_STORAGE` "granted" via
 * `shouldShowRequestPermissionRationale`, we know the user previously
 * chose "Don't ask again" and we have to fall back to the system
 * settings intent.
 */
object StoragePermissions {

    /** Returns the list of runtime storage permissions needed on this
     *  Android version. Always non-empty on min SDK 24. */
    fun required(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /** True if all required runtime storage permissions are granted. */
    fun areGranted(context: Context): Boolean {
        return required().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    /** A short human label for the permission rationale dialog. */
    fun rationale(): String =
        "NexCore needs access to your photos, videos, and audio so the " +
            "Storage Analyzer can show what's taking up space."
}