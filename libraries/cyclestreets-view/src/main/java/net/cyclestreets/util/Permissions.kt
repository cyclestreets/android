package net.cyclestreets.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.util.Log
import net.cyclestreets.util.Permissions.justifications
import net.cyclestreets.view.R

private val TAG = Logging.getTag(Permissions::class.java)

// Check for permissions
fun hasPermission(context: Context, permission: String): Boolean {
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

// Do or request permission - from activity, fragment, or (with hackery) context
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
fun doOrRequestPermission(fragment: Fragment, permission: String, action: () -> Unit) {
    val context = fragment.context!!
    if (hasPermission(context, permission))
        action()
    else {
        if (fragment.shouldShowRequestPermissionRationale(permission)) {
            MessageBox.OkHtml(context, justification(context, permission)) {
                    _, _ -> requestPermission(fragment, permission)
            }
        } else
            requestPermission(fragment, permission)
    }
}
fun doOrRequestPermission(context: Context, permission: String, action: () -> Unit) {
    doOrRequestPermission(activityFromContext(context)!!, permission, action)
}

// See https://stackoverflow.com/questions/8276634/android-get-hosting-activity-from-a-view for a discussion of this hackery
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

// Request permissions: activity or fragment
private fun requestPermission(activity: Activity, permission: String) {
    activity.requestPermissions(arrayOf(permission), 1)
}
private fun requestPermission(fragment: Fragment, permission: String) {
    try {
        fragment.requestPermissions(arrayOf(permission), 1)
    } catch (e: IllegalStateException) {
        Log.w(TAG, "Unable to request permission $permission from fragment $fragment", e)
    }
}

private fun justification(context: Context, permission: String): String {
    val reason = context.getString(justifications[permission]!!)
    return context.getString(R.string.perm_justification_format, "<ul>$reason</ul>")
}

private object Permissions {
    val justifications: Map<String, Int> = hashMapOf(
        Manifest.permission.READ_EXTERNAL_STORAGE to R.string.perm_justification_read_external_storage,
        Manifest.permission.WRITE_EXTERNAL_STORAGE to R.string.perm_justification_write_external_storage,
        Manifest.permission.ACCESS_FINE_LOCATION to R.string.perm_justification_access_fine_location,
        Manifest.permission.READ_CONTACTS to R.string.perm_justification_read_contacts
    )
}
