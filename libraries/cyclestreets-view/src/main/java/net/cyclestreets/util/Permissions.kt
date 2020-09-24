package net.cyclestreets.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivity
import androidx.fragment.app.Fragment
import net.cyclestreets.CycleStreetsPreferences.logPermissionAsRequested
import net.cyclestreets.CycleStreetsPreferences.permissionPreviouslyRequested
import net.cyclestreets.util.Permissions.justifications
import net.cyclestreets.view.R


private val TAG = Logging.getTag(Permissions::class.java)

// Check for permissions
fun hasPermission(context: Context, permission: String): Boolean {
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

// See https://stackoverflow.com/questions/32347532/android-m-permissions-confused-on-the-usage-of-shouldshowrequestpermissionrati
// for some details about the different "requesting permissions" context that Android has.

// Do or request permission - from activity, fragment, or (with hackery) context
fun doOrRequestPermission(activity: Activity, permission: String, action: () -> Unit) {
    if (hasPermission(activity, permission)) {
        // all good, carry on
        Log.d(TAG, "Permission $permission has already been granted")
        action()
    } else {
        if (!permissionPreviouslyRequested(permission) || activity.shouldShowRequestPermissionRationale(permission)) {
            // Give details of why we're asking for permission, be it when we ask for the first time
            // or after a user clicked "deny" the first time
            Log.d(TAG, "Asking for permission $permission for the first time")
            logPermissionAsRequested(permission)
            MessageBox.OkHtml(activity, justification(activity, permission, R.string.perm_justification_format)) { _, _ ->
                requestPermission(activity, permission)
            }
        } else {
            // User has previously denied, and said "don't ask me again".  Tell them they'll have to go into app settings now.
            Log.d(TAG, "Asking for permission $permission after previous denial")
            MessageBox.OkHtml(activity, justification(activity, permission, R.string.perm_justification_after_denial_format)) { _, _ ->
                goToSettings(activity)
            }
        }
    }
}
fun doOrRequestPermission(fragment: Fragment, permission: String, action: () -> Unit) {
    val context = fragment.requireContext()

    if (hasPermission(context, permission)) {
        // all good, carry on
        Log.d(TAG, "Permission $permission has already been granted")
        action()
    } else {
        if (!permissionPreviouslyRequested(permission) || fragment.shouldShowRequestPermissionRationale(permission)) {
            // Give details of why we're asking for permission, be it when we ask for the first time
            // or after a user clicked "deny" the first time
            Log.d(TAG, "Asking for permission $permission for the first time")
            logPermissionAsRequested(permission)
            MessageBox.OkHtml(context, justification(context, permission, R.string.perm_justification_format)) { _, _ ->
                requestPermission(fragment, permission)
            }
        } else {
            // User has previously denied, and said "don't ask me again".  Tell them they'll have to go into app settings now.
            Log.d(TAG, "Asking for permission $permission after previous denial")
            MessageBox.OkHtml(context, justification(context, permission, R.string.perm_justification_after_denial_format)) { _, _ ->
                goToSettings(context)
            }
        }
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
        context = context.baseContext
    }
    return null
}

// Request permissions: activity or fragment
private fun requestPermission(activity: Activity, permission: String) {
    Log.d(TAG, "Entering Activity requestPermissions operation for permission $permission")
    activity.requestPermissions(arrayOf(permission), 1)
}
private fun requestPermission(fragment: Fragment, permission: String) {
    try {
        Log.d(TAG, "Entering Fragment requestPermissions operation for permission $permission")
        fragment.requestPermissions(arrayOf(permission), 1)
    } catch (e: IllegalStateException) {
        Log.w(TAG, "Unable to request permission $permission from fragment $fragment", e)
    }
}

// Go to settings - if dynamic permission requesting is no long an option
@RequiresApi(api = Build.VERSION_CODES.M)
private fun goToSettings(context: Context) {
    Log.d(TAG, "Opening Settings screen to allow user to update permissions")
    val androidAppSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:net.cyclestreets"))
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(context, androidAppSettingsIntent, null)
}

// justifications
private fun justification(context: Context, permission: String, justificationFormatResource: Int): String {
    val reason = context.getString(justifications[permission]!!)
    val userFriendlyPermission = context.packageManager.getPermissionInfo(permission, 0).loadLabel(context.packageManager)
    return context.getString(justificationFormatResource, userFriendlyPermission, reason)
}

private object Permissions {
    val justifications: Map<String, Int> = hashMapOf(
        Manifest.permission.READ_EXTERNAL_STORAGE to R.string.perm_justification_read_external_storage,
        Manifest.permission.WRITE_EXTERNAL_STORAGE to R.string.perm_justification_write_external_storage,
        Manifest.permission.ACCESS_FINE_LOCATION to R.string.perm_justification_access_fine_location,
        Manifest.permission.READ_CONTACTS to R.string.perm_justification_read_contacts
    )
}
