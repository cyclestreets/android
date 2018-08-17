package net.cyclestreets.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import net.cyclestreets.util.Permissions.justifications
import net.cyclestreets.view.R

@Deprecated("requestPermissions returns asynchronously, so this is not a sensible mechanism")
fun verify(activity: Activity, permission: String): Boolean {
    if (!hasPermission(activity, permission))
        activity.requestPermissions(arrayOf(permission), 1)
    return hasPermission(activity, permission)
}

fun doOrRequestPermission(context: Context, permission: String, action: () -> Unit) {
    doOrRequestPermission(activityFromContext(context)!!, permission, action)
}

fun doOrRequestPermission(activity: Activity, permission: String, action: () -> Unit) {
    if (hasPermission(activity, permission))
        action()
    else {
        if (activity.shouldShowRequestPermissionRationale(permission)) {
            MessageBox.OkHtml(activity, justification(activity, permission)) {
                _, _ -> requestPermission(activity, permission)
            }
        } else
            requestPermission(activity, permission)
    }
}

fun hasPermission(context: Context, permission: String): Boolean {
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

private fun activityFromContext(initialContext: Context): Activity? {
    var context = initialContext
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext;
    }
    return null
}

private fun requestPermission(activity: Activity, permission: String) {
    activity.requestPermissions(arrayOf(permission), 1)
}

private fun justification(context: Context, permission: String): String {
    val reason = context.getString(justifications.get(permission)!!)
    val permString = permission.replace("android.permission.", "")
    return context.getString(R.string.perm_justification_format, "<i>$permString</i>", "<ul>$reason</ul>")
}

object Permissions {
    val justifications: Map<String, Int> = hashMapOf(
        Manifest.permission.READ_EXTERNAL_STORAGE to R.string.perm_justification_read_external_storage,
        Manifest.permission.WRITE_EXTERNAL_STORAGE to R.string.perm_justification_write_external_storage,
        Manifest.permission.ACCESS_FINE_LOCATION to R.string.perm_justification_access_fine_location,
        Manifest.permission.READ_CONTACTS to R.string.perm_justification_read_contacts
    )
}
