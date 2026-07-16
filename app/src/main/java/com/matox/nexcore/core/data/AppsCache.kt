package com.matox.nexcore.core.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.matox.nexcore.domain.model.AppIconRef
import com.matox.nexcore.domain.model.AppInfo
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Tiny on-disk cache for the App Manager snapshot.
 *
 * Layout (one file per app + a single manifest.json):
 *
 *   filesDir/apps_cache/
 *     manifest.json     ← ordered list of {packageName, v, sizeBytes,
 *                            updatedMs, isSystem, hasLauncher, displayName,
 *                            versionName, categoryLabel, iconRef}
 *     icons/<pkg>.png   ← 96×96 Bitmap for each app's icon
 *
 * Why this exists:
 *  - First open of App Manager fetches ~300 apps × icon decode = slow.
 *  - With this cache we paint the list from disk instantly and refresh
 *    the snapshot in the background. The screen never hangs because
 *    the cached data was always already there.
 *  - The cache is invalidated when PackageManager broadcasts tell us a
 *    package was added/removed/replaced — AppManagerDataSource owns
 *    that hook.
 *
 * Versioned (`SCHEMA_VERSION`) so we can drop old payloads cleanly if
 * the [AppInfo] shape changes.
 */
class AppsCache(context: Context) {

    private val cacheDir: File = File(context.filesDir, "apps_cache").apply { mkdirs() }
    private val iconsDir: File = File(cacheDir, "icons").apply { mkdirs() }
    private val manifestFile: File = File(cacheDir, "manifest.json")

    fun isEmpty(): Boolean = !manifestFile.exists()

    /** Snapshot what's on disk, or null if there's no usable cache. */
    fun read(): List<AppInfo>? {
        if (!manifestFile.exists()) return null
        return runCatching {
            val raw = manifestFile.readBytes()
            if (raw.isEmpty()) return null
            val root = JSONObject(String(raw, Charsets.UTF_8))
            if (root.optInt("version", -1) != SCHEMA_VERSION) return null
            val arr = root.optJSONArray("apps") ?: return null
            buildList { repeat(arr.length()) { add(deserialize(arr.getJSONObject(it))) } }
        }.getOrNull()
    }

    /** Persist a fresh snapshot. Walks the list, decodes icons to 96×96 PNG,
     *  and writes everything atomically through .tmp + rename. */
    fun write(apps: List<AppInfo>) {
        runCatching {
            val tmpManifest = File(cacheDir, "manifest.json.tmp")
            val arr = JSONArray()
            for (app in apps) {
                val iconPath = persistIcon(app.packageName, app.iconRef)
                arr.put(serialize(app, iconPath))
            }
            val root = JSONObject()
                .put("version", SCHEMA_VERSION)
                .put("savedAtMs", System.currentTimeMillis())
                .put("apps", arr)
            tmpManifest.writeText(root.toString(), Charsets.UTF_8)
            if (!tmpManifest.renameTo(manifestFile)) {
                tmpManifest.copyTo(manifestFile, overwrite = true)
                tmpManifest.delete()
            }
        }
    }

    fun clear() {
        runCatching {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            iconsDir.mkdirs()
        }
    }

    private fun persistIcon(packageName: String, iconRef: AppIconRef): String? {
        val bitmap = (iconRef as? AppIconRef.Loaded)?.bitmap ?: return null
        val safeName = packageName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val out = File(iconsDir, "$safeName.png")
        return runCatching {
            FileOutputStream(out).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            out.absolutePath
        }.getOrNull()
    }

    private fun serialize(app: AppInfo, iconPath: String?): JSONObject = JSONObject()
        .put("packageName", app.packageName)
        .put("displayName", app.displayName)
        .put("versionName", app.versionName)
        .put("categoryLabel", app.categoryLabel)
        .put("sizeBytes", app.sizeBytes)
        .put("lastUpdatedEpochMs", app.lastUpdatedEpochMs)
        .put("isSystem", app.isSystem)
        .put("hasLauncher", app.hasLauncher)
        .put("iconPath", iconPath ?: "")

    private fun deserialize(o: JSONObject): AppInfo {
        val iconPath = o.optString("iconPath").takeIf { it.isNotBlank() }
        val iconRef: AppIconRef = when (iconPath) {
            null -> AppIconRef.Failed
            else -> {
                val f = File(iconPath)
                if (!f.exists()) AppIconRef.Failed
                else {
                    val bmp: Bitmap? = BitmapFactory.decodeFile(f.absolutePath)
                    if (bmp != null) AppIconRef.Loaded(bmp) else AppIconRef.Failed
                }
            }
        }
        return AppInfo(
            packageName = o.getString("packageName"),
            displayName = o.getString("displayName"),
            versionName = o.optString("versionName", "—"),
            categoryLabel = o.optString("categoryLabel", "Apps"),
            sizeBytes = o.optLong("sizeBytes", 0L),
            lastUpdatedEpochMs = o.optLong("lastUpdatedEpochMs", 0L),
            isSystem = o.optBoolean("isSystem", false),
            hasLauncher = o.optBoolean("hasLauncher", false),
            iconRef = iconRef,
        )
    }

    companion object {
        private const val SCHEMA_VERSION = 1
    }
}
