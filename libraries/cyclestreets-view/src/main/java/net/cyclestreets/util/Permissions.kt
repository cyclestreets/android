package net.cyclestreets.util

import android.app.Activity
import android.content.pm.PackageManager

@Deprecated("requestPermissions returns asynchronously, so this is not a sensible mechanism")
fun verify(activity: Activity, permission: String): Boolean {
    if (!hasPermission(activity, permission))
        activity.requestPermissions(arrayOf(permission), 1)
    return hasPermission(activity, permission)
}

fun doOrRequestPermission(activity: Activity, permission: String, action: () -> Unit) {
    if (hasPermission(activity, permission))
        action()
    else
        requestPermission(activity, permission)
}

private fun requestPermission(activity: Activity, permission: String) {
    activity.requestPermissions(arrayOf(permission), 1)
}

private fun hasPermission(activity: Activity, permission: String): Boolean {
    return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
