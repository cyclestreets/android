package net.cyclestreets.addphoto

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.support.media.ExifInterface
import android.text.TextUtils
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date

internal const val TAKE_PHOTO = 2
internal const val CHOOSE_PHOTO = 3
internal const val ACCOUNT_DETAILS = 4

internal fun photoUploadMetaData(activity: Activity?): String {
    try {
        val ai = activity!!.packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
        return ai.metaData.getString("CycleStreetsPhotoUpload") ?: ""
    } catch (e: Exception) {
        return ""
    }
}

internal fun getImageFilePath(data: Intent, activity: Activity?): String {
    val selectedImage = data.data
    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

    val cursor = activity!!.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
    try {
        cursor!!.moveToFirst()
        return cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
    } finally {
        cursor!!.close()
    }
}

internal fun photoLocation(photoExif: ExifInterface): GeoPoint? {
    val coords: DoubleArray? = photoExif.latLong
    return if (coords != null) GeoPoint(coords[0], coords[1]) else null
}

internal fun photoTimestamp(photoExif: ExifInterface): String {
    var date = Date()
    try {
        val df = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        val dateString = photoExif.getAttribute(ExifInterface.TAG_DATETIME)
        if (!TextUtils.isEmpty(dateString))
            date = df.parse(dateString)
    } catch (e: Exception) {
        // ah well
    }
    return java.lang.Long.toString(date.time / 1000)
}
