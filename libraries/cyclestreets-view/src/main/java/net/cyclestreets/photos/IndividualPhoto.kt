package net.cyclestreets.photos

import net.cyclestreets.api.Photo
import net.cyclestreets.util.Logging

import java.util.ArrayList

private val TAG = Logging.getTag(IndividualPhoto::class.java)

object IndividualPhoto {

    interface Listener {
        fun onPhotoLoaded(photo: Photo)
    }

    private val listeners = ArrayList<Listener>()
    fun registerListener(listener: Listener) {
        if (!listeners.contains(listener))
            listeners.add(listener)
    }
    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }
    fun onPhotoLoaded(photo: Photo) {
        for (l in listeners)
            l.onPhotoLoaded(photo)
    }

    fun fetchPhoto(photoId: Long) {
        val query = FetchIndividualPhotoTask()
        query.execute(photoId)
    }
}
