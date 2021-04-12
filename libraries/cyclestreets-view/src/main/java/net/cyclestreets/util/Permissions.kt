package net.cyclestreets.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivity
import androidx.fragment.app.Fragment
import net.cyclestreets.CycleStreetsPreferences.*
import net.cyclestreets.GENERIC_PERMISSION_REQUEST
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

/**
 * Do or request permission - where you only have a context, so use hackery to derive the activity
 *
 * @param activity - the activity (if available - fragment is preferred; one or both must be provided)
 * @param fragment - the fragment (if available - preferred to activity; one or both must be provided)
 * @param permission - the permission being requested, e.g. [Manifest.permission.ACCESS_FINE_LOCATION]
 * @param requestCode - can be used for correlating in the onRequestPermissionsResult() callback
 * @param action - the closure indicating the action to be taken if the permission has already been granted
 */
fun doOrRequestPermission(activity: Activity?, fragment: Fragment?, permission: String,
                          requestCode: Int = GENERIC_PERMISSION_REQUEST, action: () -> Unit) {
    val context: Context = fragment?.requireContext() ?: activity!!

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
            MessageBox.OkHtml(context, justification(context, permission, R.string.perm_justification_format)) { _, _ ->
                requestPermission(activity, fragment, permission, requestCode)
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
                requestPermission(activity, fragment, permission, requestCode)
            }
            else {
                // User has previously denied, and said "don't ask me again".  Tell them they'll have to go into app settings now.
                logSettingsLastTime(permission)
                Log.d(TAG, "Asking for permission $permission after previous denial")
                MessageBox.OkHtml(context, justification(context, permission, R.string.perm_justification_after_denial_format)) { _, _ ->
                    goToSettings(context)
                }
            }
        }
    }
}

/**
 * Do or request permission - where only a context is available (using hackery to derive the activity)
 *
 * @param providedContext - the context, from which we try to derive an [Activity]
 * @param permission - the permission being requested, e.g. [Manifest.permission.ACCESS_FINE_LOCATION]
 * @param requestCode - can be used for correlating in the onRequestPermissionsResult() callback
 * @param action - the closure indicating the action to be taken if the permission has already been granted
 */
fun doOrRequestPermission(providedContext: Context, permission: String, requestCode: Int, action: () -> Unit) {

    // See https://stackoverflow.com/questions/8276634/android-get-hosting-activity-from-a-view for a discussion of this hackery
    var context = providedContext
    while (context is ContextWrapper) {
        if (context is Activity) {
            doOrRequestPermission(context, null, permission, requestCode, action)
        }
        context = context.baseContext
    }

    Log.w(TAG, "Unable to retrieve activity from context: ${context}")
}

// Request permissions: activity or fragment
private fun requestPermission(activity: Activity?, fragment: Fragment?, permission: String, requestCode: Int) {
    try {
        if (fragment != null) {
            Log.d(TAG, "Entering Fragment requestPermissions operation for permission $permission")
            fragment.requestPermissions(arrayOf(permission), requestCode)
        }
        else {
            Log.d(TAG, "Entering Activity requestPermissions operation for permission $permission")
            activity?.requestPermissions(arrayOf(permission), requestCode)
        }
    } catch (e: IllegalStateException) {
        Log.w(TAG, "Unable to request permission $permission from ${if (fragment != null) "fragment $fragment" else "activity $activity"}", e)
    }
}

/**
 * Generic handler for permission Handle
 *
 * @param grantResult - either [PERMISSION_GRANTED] or [PERMISSION_DENIED]
 * @param permission - the string identifier of the permission, e.g. [Manifest.permission.ACCESS_FINE_LOCATION]
 * @param action - the closure indicating the action to be taken if the permission was granted
 */
fun requestPermissionsResultAction(grantResult: Int?, permission: String, action: () -> Unit) {
    if (grantResult == PERMISSION_GRANTED) {
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
