package net.cyclestreets.photos

import android.content.Context
import net.cyclestreets.api.Photo
import net.cyclestreets.util.Screen

internal interface ImageDisplay {
    fun show()
}

fun displayPhoto(photo: Photo, context: Context) {
    if (photo.hasVideos()) {
        if (Screen.isSmall(context))
            ExternalVideoPlayer(photo, context).show()
        else
            VideoDisplayDialog(photo, context).show()
    }
    else
        PhotoDisplayDialog(photo, context).show()
}
