package net.cyclestreets.photos

import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageButton
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.TextView
import net.cyclestreets.view.R
import java.lang.ref.WeakReference

private const val SHOW_PROGRESS = 2

internal class VideoControllerView(private val controllerView: View) {
    private lateinit var pauseBtn: ImageButton
    private lateinit var ffBtn: ImageButton
    private lateinit var rewBtn: ImageButton
    private lateinit var seekBar: SeekBar

    private lateinit var timeElapsed: TextView
    private lateinit var endTime: TextView

    private var videoPlayer: MediaController.MediaPlayerControl? = null
    private var isDragging: Boolean = false
    private val msgHandler = MessageHandler(this)

    init {
        initControllerView(controllerView)
    }

    private fun initControllerView(v: View) {
        pauseBtn = v.findViewById<View>(R.id.pause) as ImageButton
        pauseBtn.setOnClickListener(pauseListener)

        ffBtn = v.findViewById<View>(R.id.ffwd) as ImageButton
        ffBtn.setOnClickListener(ffListener)

        rewBtn = v.findViewById<View>(R.id.rew) as ImageButton
        rewBtn.setOnClickListener(rewListener)

        seekBar = v.findViewById<View>(R.id.mediacontroller_progress) as SeekBar
        seekBar.setOnSeekBarChangeListener(seekListener)
        seekBar.max = 1000

        timeElapsed = v.findViewById<View>(R.id.time_current) as TextView
        endTime = v.findViewById<View>(R.id.time) as TextView
    }

    fun getWidth(): Int { return controllerView.width }
    fun isShowing(): Boolean { return controllerView.visibility == View.VISIBLE }

    fun setMediaPlayer(player: MediaController.MediaPlayerControl) {
        videoPlayer = player
        updatePausePlay()
    }

    //////////// Button listeners
    private val pauseListener = View.OnClickListener { doPauseResume() }

    private val ffListener = View.OnClickListener {
        var pos = videoPlayer!!.currentPosition
        pos += 15000 // milliseconds
        videoPlayer!!.seekTo(pos)
        setProgress()
    }

    private val rewListener = View.OnClickListener {
        var pos = videoPlayer!!.currentPosition
        pos -= 5000 // milliseconds
        videoPlayer!!.seekTo(pos)
        setProgress()
    }

    private val seekListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(bar: SeekBar) {
            isDragging = true

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            msgHandler.removeMessages(SHOW_PROGRESS)
        }

        override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
            if (!fromuser)
                return

            val duration = videoPlayer!!.duration.toLong()
            val newposition = duration * progress / 1000L
            videoPlayer!!.seekTo(newposition.toInt())
            timeElapsed.text = formatTime(newposition.toInt())
        }

        override fun onStopTrackingTouch(bar: SeekBar) {
            isDragging = false
            setProgress()
            updatePausePlay()
            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            msgHandler.sendEmptyMessage(SHOW_PROGRESS)
        }
    }

    private fun disableUnsupportedButtons() {
        if (videoPlayer == null)
            return

        try {
            if (!videoPlayer!!.canPause())
                pauseBtn.isEnabled = false
            if (!videoPlayer!!.canSeekBackward())
                rewBtn.isEnabled = false
            if (!videoPlayer!!.canSeekForward())
                ffBtn.isEnabled = false
        } catch (ex: IncompatibleClassChangeError) {
        }

    }

    // Show the controller on screen...
    fun show() {
        if (!isShowing()) {
            setProgress()
            pauseBtn.requestFocus()
            disableUnsupportedButtons()
        }

        controllerView.visibility = View.VISIBLE
        updatePausePlay()
        msgHandler.sendEmptyMessage(SHOW_PROGRESS)
    }

    fun hide() {
        controllerView.visibility = View.GONE
    }

    private fun formatTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000

        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600

        return if (hours > 0)
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        else
            String.format("%02d:%02d", minutes, seconds)
    }

    private fun setProgress(): Int {
        if (videoPlayer == null || isDragging)
            return 0

        val position = videoPlayer!!.currentPosition
        val duration = videoPlayer!!.duration
        if (duration > 0) {
            val pos = 1000L * position / duration
            seekBar.progress = pos.toInt()
        }
        val percent = videoPlayer!!.bufferPercentage
        seekBar.secondaryProgress = percent * 10

        timeElapsed.text = formatTime(position)
        endTime.text = formatTime(duration)

        return position
    }

    fun updatePausePlay() {
        pauseBtn.setImageResource(if (videoPlayer!!.isPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play)
    }

    private fun doPauseResume() {
        if (videoPlayer!!.isPlaying)
            videoPlayer!!.pause()
        else
            videoPlayer!!.start()
        updatePausePlay()
    }

    private class MessageHandler internal constructor(view: VideoControllerView) : Handler() {
        private val view: WeakReference<VideoControllerView> = WeakReference(view)

        override fun handleMessage(msg: Message) {
            val view = view.get()
            if (view?.videoPlayer != null && msg.what == SHOW_PROGRESS) {
                val pos = view.setProgress()
                if (!view.isDragging && view.isShowing() && view.videoPlayer!!.isPlaying) {
                    sendMessageDelayed(obtainMessage(SHOW_PROGRESS), (1000 - pos % 1000).toLong())
                }
                view.updatePausePlay()
            }
        }
    }
}
