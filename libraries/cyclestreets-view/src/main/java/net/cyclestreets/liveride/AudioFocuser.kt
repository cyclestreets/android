package net.cyclestreets.liveride

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

import android.media.AudioAttributes.CONTENT_TYPE_SPEECH
import android.media.AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
import android.media.AudioManager.*
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import net.cyclestreets.util.Logging
import kotlin.collections.HashMap

private val TAG = Logging.getTag(AudioFocuser::class.java)

class AudioFocuser(context: Context) : UtteranceProgressListener(), AudioManager.OnAudioFocusChangeListener  {

    private val audioAttributes = android.media.AudioAttributes.Builder()
        .setUsage(USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(CONTENT_TYPE_SPEECH)
        .build()
    private val am: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioFocusRequests: MutableMap<String, AudioFocusRequest> = HashMap()

    @Suppress("deprecation")
    override fun onStart(utteranceId: String) {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val afr = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(audioAttributes).build()
            audioFocusRequests[utteranceId] = afr
            am.requestAudioFocus(afr)
        } else {
            am.requestAudioFocus(this, CONTENT_TYPE_SPEECH, AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
        Log.d(TAG, "Audio focus request for utterance $utteranceId: result $result ($AUDIOFOCUS_REQUEST_GRANTED=granted, $AUDIOFOCUS_REQUEST_FAILED=failed)")
    }

    @Suppress("deprecation")
    override fun onDone(utteranceId: String) {
        val result: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequests.remove(utteranceId)?.let {
                am.abandonAudioFocusRequest(it)
            } ?: -1
        } else {
            am.abandonAudioFocus(this)
        }
        Log.d(TAG, "Audio focus release for utterance $utteranceId: result $result ($AUDIOFOCUS_REQUEST_GRANTED=granted, $AUDIOFOCUS_REQUEST_FAILED=failed, -1=utterance unknown)")
    }

    @Deprecated("")
    override fun onError(utteranceId: String) {
        Log.d(TAG, "TTS error occurred processing utterance $utteranceId")
        audioFocusRequests.remove(utteranceId)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        // Nothing to do - it's not the end of the world if we can't get audio focus
    }
}
