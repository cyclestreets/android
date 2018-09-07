package net.cyclestreets.photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.cyclestreets.api.Photo

internal class ExternalVideoPlayer(private val photo: Photo,
                                   private val context: Context) : ImageDisplay {
    override fun show() {
        val videoUrl = videoUrl(photo) ?: return

        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(videoUrl), "video/*")
            context.startActivity(this)
        }
    }

    private fun videoUrl(photo: Photo): String? {
        for (format in arrayOf("mp4", "mov", "3gp")) {
            val v = photo.video(format)
            if (v != null)
                return v.url()
        }
        return null
    }
}
