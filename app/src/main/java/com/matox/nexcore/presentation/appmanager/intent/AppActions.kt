package com.matox.nexcore.presentation.appmanager.intent

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Helpers for the four app actions surfaced in the App Manager UI.
 * Each one wraps a system intent so the OS handles the UI flow
 * (uninstall confirmation, Settings screen, etc.).
 */
object AppActions {

    /** Launch the app's main activity, if any. Returns true on success. */
    fun open(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    /**
     * Open the system "App info" page for the package. This is also
     * the surface we use for "Disable" — there's no public API for
     * a normal app to programmatically disable another app, so we
     * route the user to the system's own disable control.
     */
    fun info(context: Context, packageName: String): Boolean {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }

    fun uninstall(context: Context, packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }

    /** Routes to the same system App info page; the user can disable there. */
    fun disable(context: Context, packageName: String): Boolean = info(context, packageName)
}