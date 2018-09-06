package net.cyclestreets.liveride

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

import android.media.AudioAttributes.CONTENT_TYPE_SPEECH
import android.media.AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
import android.speech.tts.UtteranceProgressListener
import java.util.*

class AudioFocuser(context: Context) : UtteranceProgressListener(), AudioManager.OnAudioFocusChangeListener  {

    private val audioAttributes = android.media.AudioAttributes.Builder()
        .setUsage(USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(CONTENT_TYPE_SPEECH)
        .build()
    private val am: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val afrQueue: Deque<AudioFocusRequest> = LinkedList()

    @Suppress("deprecation")
    override fun onStart(utteranceId: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val afr = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(audioAttributes).build()
            afrQueue.addLast(afr)
            am.requestAudioFocus(afr)
        } else {
            am.requestAudioFocus(this, CONTENT_TYPE_SPEECH, AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    @Suppress("deprecation")
    override fun onDone(utteranceId: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (afrQueue.isNotEmpty())
                am.abandonAudioFocusRequest(afrQueue.removeFirst())
        } else {
            am.abandonAudioFocus(this)
        }
    }

    @Deprecated("")
    override fun onError(utteranceId: String?) {
        // Nothing to do - it's not the end of the world if we can't get audio focus
    }

    override fun onAudioFocusChange(focusChange: Int) {
        // Nothing to do - it's not the end of the world if we can't get audio focus
    }
}
