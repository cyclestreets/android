package net.cyclestreets.photos

import android.os.AsyncTask
import android.util.Log
import net.cyclestreets.api.ApiClient
import net.cyclestreets.api.Photo
import net.cyclestreets.util.Logging

private val TAG = Logging.getTag(FetchIndividualPhotoTask::class.java)

internal class FetchIndividualPhotoTask constructor() : AsyncTask<Long, Int, Photo>() {

    override fun doInBackground(vararg params: Long?): Photo? {
        val photoId = params[0]!!
        return try {
            Log.d(TAG, "Querying API for photo $photoId")
            val photos = ApiClient.getPhoto(photoId)
            photos.first()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get photo $photoId", e)
            null
        }
    }

    override fun onPostExecute(photo: Photo?) {
        if (photo != null)
            IndividualPhoto.onPhotoLoaded(photo)
    }
}
