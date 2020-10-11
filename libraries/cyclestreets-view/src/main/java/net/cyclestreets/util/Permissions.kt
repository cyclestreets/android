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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivity
import androidx.fragment.app.Fragment
import net.cyclestreets.CycleStreetsPreferences.*
import net.cyclestreets.util.Permissions.justifications
import net.cyclestreets.view.R


private val TAG = Logging.getTag(Permissions::class.java)

// Check for permissions
fun hasPermission(context: Context?, permission: String): Boolean {
    return if (context != null)
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    else false
}

// See https://stackoverflow.com/questions/32347532/android-m-permissions-confused-on-the-usage-of-shouldshowrequestpermissionrati
// for some details about the different "requesting permissions" context that Android has.

// Do or request permission - from activity, fragment, or (with hackery) context

fun doOrRequestPermission(activity: Activity?, fragment: Fragment?, permission: String, request_code: Int, action: () -> Unit) {
    var context: Context? = activity
    if (fragment != null)
        context = fragment.requireContext()

    if (hasPermission(context, permission)) {
        // all good, carry on
        Log.d(TAG, "Permission $permission has already been granted")
        clearSettingsLastTime(permission)
        clearPermissionRequested(permission)    // Only needed here for when no callback is used
        action()
    } else {
        var shouldShowRationale = false
        if (fragment != null)
            shouldShowRationale = fragment.shouldShowRequestPermissionRationale(permission)
        if (activity != null)
            shouldShowRationale = activity.shouldShowRequestPermissionRationale(permission)

        if (!permissionPreviouslyRequested(permission) || shouldShowRationale) {
            // Give details of why we're asking for permission, be it when we ask for the first time
            // or after a user clicked "deny" the first time
            Log.d(TAG, "Asking for permission $permission for the first time")
            logPermissionAsRequested(permission)    // Only needed here for when no callback is used
            MessageBox.OkHtml(context, context?.let { justification(it, permission, R.string.perm_justification_format) }) { _, _ ->
                requestPermission(activity, fragment, permission, request_code)
            }
        } else {
            if (settingsLastTime(permission)) {
                // If the user was taken to device settings last time, then for sensitive permissions Android 11 / API 30 gives them 3 options:
                // Allow for this app / Ask every time / Deny
                // To get to this point they would have taken "Ask every time" or "Deny" but unfortunately we don't know which
                // as both appear as Permission not granted.
                // However the Android framework will know which option was taken in the settings
                // and if we request permission here it will take the appropriate action.
                // If the "Deny" option was taken in the device settings then the permission box won't be displayed.
                // If the "Ask every time" option was taken then the permission box will be displayed.
                // No justification box is shown here as they have already had them both and it wouldn't be appropriate if the permissions
                // box then wasn't displayed.
                // Displaying a toast message so user knows why nothing is happening when they press the button.
                Toast.makeText(context, R.string.perm_needed, Toast.LENGTH_LONG).show()
                requestPermission(activity, fragment, permission, request_code)
            }
            else {
                // User has previously denied, and said "don't ask me again".  Tell them they'll have to go into app settings now.
                logSettingsLastTime(permission)
                Log.d(TAG, "Asking for permission $permission after previous denial")
                MessageBox.OkHtml(context, context?.let { justification(it, permission, R.string.perm_justification_after_denial_format) }) { _, _ ->
                    if (context != null) {
                        goToSettings(context)
                    }
                }
            }
        }
    }
}

fun doOrRequestPermission(context: Context, permission: String, request_code: Int, action: () -> Unit) {
    doOrRequestPermission(activityFromContext(context)!!, null, permission, request_code, action)
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
fun requestPermission(activity: Activity?, fragment: Fragment?, permission: String, request_code: Int) {
    try {
        if (fragment != null) {
            Log.d(TAG, "Entering Fragment requestPermissions operation for permission $permission")
            fragment.requestPermissions(arrayOf(permission), request_code)
        }
        else {
            Log.d(TAG, "Entering Activity requestPermissions operation for permission $permission")
            if (activity != null) {
                activity.requestPermissions(arrayOf(permission), request_code)
            }
        }
    } catch (e: IllegalStateException) {
        Log.w(TAG, "Unable to request permission $permission from ${if (fragment != null) "fragment $fragment" else "activity $activity"}", e)
    }
}

fun requestPermissionsResultAction(grantResults: IntArray, grantResult: Int?, permission: String, action: () -> Unit) {
    if (grantResults.isNotEmpty() && grantResult == PackageManager.PERMISSION_GRANTED) {
        clearSettingsLastTime(permission)
        clearPermissionRequested(permission)
        action()
    } else {
        // Permission request was denied.
        logPermissionAsRequested(permission)
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
