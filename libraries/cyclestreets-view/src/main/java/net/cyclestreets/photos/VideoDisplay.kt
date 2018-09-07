package net.cyclestreets.photos

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.LinearLayout
import android.widget.VideoView
import net.cyclestreets.api.Photo
import net.cyclestreets.view.R

internal class VideoDisplay internal constructor(photo: Photo, context: Context) : DisplayDialog(photo, context), MediaPlayer.OnPreparedListener {
    private lateinit var videoView: VideoView
    private lateinit var controller: VideoControllerView
    private var progress: ProgressDialog? = null

    override fun title(): String {
        return String.format("Video #%d", photo.id())
    }

    override fun caption(): String {
        return photo.caption().replace('\n', ' ')
    }

    override fun loadLayout(): View {
        val layout = View.inflate(context, R.layout.showvideo, null)
        controller = VideoControllerView(layout.findViewById<View>(R.id.videocontroller))
        videoView = layout.findViewById<View>(R.id.video) as VideoView
        return layout
    }

    override fun postShowSetup(dialog: AlertDialog) {
        val uri = Uri.parse(videoUrl(photo))

        videoView.apply {
            sizeView(this as View, dialog.context)
            setVideoURI(uri)
            setZOrderOnTop(true)
            requestFocus()
            start()
            setOnPreparedListener(this@VideoDisplay)
        }

        progress = ProgressDialog(dialog.context).apply {
            setMessage("Loading video ...")
            show()
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        progress!!.dismiss()

        controller.setMediaPlayer(videoView)
        controller.show()

        val vw = videoView.width
        val cw = controller.getWidth()

        if (vw >= cw) {
            // need to resize
            val scale = cw / vw.toFloat()
            val newWidth = (vw * scale).toInt()
            val newHeight = (videoView.height * scale).toInt()

            videoView.layoutParams = LinearLayout.LayoutParams(newWidth, newHeight)
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
