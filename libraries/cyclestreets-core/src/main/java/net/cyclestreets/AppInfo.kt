package net.cyclestreets

import android.content.Context
import android.content.pm.PackageManager

private const val UNKNOWN = "unknown"

object AppInfo {
    private var version: String? = null

    fun version(context: Context): String {
        if (version == null)
            version = String.format("%s/%s", context.packageName, versionNameAndCode(context))
        return version!!
    }

    private fun versionNameAndCode(context: Context): String {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            String.format("%s/%s", info.versionName, info.versionCode.toString())
        } catch (e: PackageManager.NameNotFoundException) {
            // like this is going to happen
            UNKNOWN
        }
    }
}
