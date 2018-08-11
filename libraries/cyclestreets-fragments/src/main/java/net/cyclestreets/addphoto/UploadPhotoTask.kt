package net.cyclestreets.addphoto

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask

import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.api.Upload
import net.cyclestreets.fragments.R
import net.cyclestreets.util.Bitmaps
import net.cyclestreets.util.Dialog

import org.osmdroid.api.IGeoPoint

import java.io.File

internal class UploadPhotoTask(context: Context,
                               filename: String,
                               private val username: String,
                               private val password: String,
                               private val location: IGeoPoint,
                               private val metaCat: String,
                               private val category: String,
                               private val dateTime: String,
                               private val caption: String) : AsyncTask<Any, Void, Upload.Result>() {
    private val smallImage: Boolean = CycleStreetsPreferences.uploadSmallImages()
    private val filename: String
    private val progress: ProgressDialog

    init {
        this.filename = if (smallImage) Bitmaps.resizePhoto(filename) else filename
        progress = Dialog.createProgressDialog(context, R.string.photo_uploading)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        progress.show()
    }

    override fun doInBackground(vararg params: Any): Upload.Result {
        return try {
            Upload.photo(filename, username, password, location,
                    metaCat, category, dateTime, caption)
        } catch (e: Exception) {
            Upload.Result.error(e.message)
        }

    }

    override fun onPostExecute(result: Upload.Result) {
        if (smallImage)
            File(filename).delete()
        progress.dismiss()
        
        if (result.ok())
            uploadComplete(result.url())
        else
            uploadFailed(result.message())
    }
}
