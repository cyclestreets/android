package net.cyclestreets.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import net.cyclestreets.util.Permissions.justifications
import net.cyclestreets.view.R

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

fun hasPermission(context: Context, permission: String): Boolean {
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

private fun requestPermission(activity: Activity, permission: String) {
    activity.requestPermissions(arrayOf(permission), 1)
}

private fun justification(context: Context, permission: String): String {
    val reason = context.getString(justifications.get(permission)!!)
    val permString = permission.replace("android.permission.", "")
    return context.getString(R.string.perm_justification_format, permString, reason)
}

object Permissions {
    val justifications: Map<String, Int> = hashMapOf(
        Manifest.permission.READ_EXTERNAL_STORAGE to R.string.perm_justification_read_external_storage,
        Manifest.permission.WRITE_EXTERNAL_STORAGE to R.string.perm_justification_write_external_storage,
        Manifest.permission.ACCESS_FINE_LOCATION to R.string.perm_justification_access_fine_location,
        Manifest.permission.READ_CONTACTS to R.string.perm_justification_read_contacts
    )
}
