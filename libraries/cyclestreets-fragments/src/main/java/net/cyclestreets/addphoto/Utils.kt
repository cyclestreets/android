package net.cyclestreets.addphoto

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import net.cyclestreets.fragments.R
import net.cyclestreets.util.AsyncDelete
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.io.File

internal const val TAKE_PHOTO = 2
internal const val CHOOSE_PHOTO = 3
internal const val ACCOUNT_DETAILS = 4

private const val PHOTO_FILE_PREFIX = "CS_PHOTO_"

internal fun photoUploadMetaData(activity: Activity?): String {
    try {
        val ai = activity!!.packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
        return ai.metaData.getString("CycleStreetsPhotoUpload") ?: ""
    } catch (e: Exception) {
        return ""
    }
}

internal fun backNextButtons(parentView: View,
                             backText: String, backDrawable: Drawable,
                             nextText: String, nextDrawable: Drawable) {
    parentView.findViewById<Button>(R.id.back).apply {
        text = backText
        setCompoundDrawables(backDrawable, null, null, null)
    }
    parentView.findViewById<Button>(R.id.next).apply {
        text = nextText
        setCompoundDrawables(null, null, nextDrawable, null)
    }
}

@SuppressLint("SimpleDateFormat")
internal fun createImageFile(context: Context?): File {
    deletePreviouslyCapturedImages(context)
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "$PHOTO_FILE_PREFIX$timeStamp"
    val file = File.createTempFile(imageFileName, ".jpg", storageDir(context))
    Log.i(TAG, "Created temporary image file ${file.absolutePath}")
    return file
}

private fun deletePreviouslyCapturedImages(context: Context?) {
    val files = storageDir(context).listFiles { _, filename -> filename.matches(Regex("$PHOTO_FILE_PREFIX.*")) }
    AsyncDelete().execute(*files)
}

private fun storageDir(context: Context?): File {
    return context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
}

internal fun getImageFilePath(data: Intent, activity: Activity?): String {
    val selectedImage = data.data
    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

    activity!!.contentResolver
            .query(selectedImage!!, filePathColumn, null, null, null)!!
            .use { cursor ->
        cursor.moveToFirst()
        return cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
    }
}

internal fun photoLocation(photoExif: ExifInterface): GeoPoint? {
    val coords: DoubleArray? = photoExif.latLong
    return if (coords != null) GeoPoint(coords[0], coords[1]) else null
}

@SuppressLint("SimpleDateFormat")
internal fun photoTimestamp(photoExif: ExifInterface): String {
    var date = Date()
    try {
        val df = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        val dateString = photoExif.getAttribute(ExifInterface.TAG_DATETIME)!!
        if (!TextUtils.isEmpty(dateString))
            date = df.parse(dateString)!!
    } catch (e: Exception) {
        // ah well
    }
    return (date.time / 1000).toString()
}
