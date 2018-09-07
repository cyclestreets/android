package net.cyclestreets

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

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
            String.format("%s/%s", info.versionName, getVersionCode(info))
        } catch (e: PackageManager.NameNotFoundException) {
            // like this is going to happen
            UNKNOWN
        }
    }

    @SuppressLint("NewApi")
    @Suppress("deprecation")
    private fun getVersionCode(info: PackageInfo): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toString()
        } else {
            info.versionCode.toString()
        }
    }
}
