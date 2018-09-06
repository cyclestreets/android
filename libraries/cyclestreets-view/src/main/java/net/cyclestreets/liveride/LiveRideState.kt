package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsNotifications
import net.cyclestreets.CycleStreetsNotifications.CHANNEL_LIVERIDE_ID
import net.cyclestreets.LiveRideActivity
import net.cyclestreets.iconics.IconicsHelper
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Segment
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R

import com.mikepenz.google_material_typeface_library.GoogleMaterial
import org.osmdroid.util.GeoPoint
import ru.ztrap.iconics.kt.toAndroidIconCompat

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioAttributes.CONTENT_TYPE_SPEECH
import android.media.AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log

internal fun initialState(context: Context, tts: TextToSpeech): LiveRideState {
    return LiveRideStart(context, tts)
}

internal fun stoppedState(context: Context): LiveRideState {
    return Stopped(context)
}

private val TAG = Logging.getTag(LiveRideState::class.java)
private const val NOTIFICATION_ID = 1
private val AUDIO_ATTRIBUTES = android.media.AudioAttributes.Builder()
    .setUsage(USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
    .setContentType(CONTENT_TYPE_SPEECH)
    .build()

internal abstract class LiveRideState(protected val context: Context,
                                      val tts: TextToSpeech?,
                                      private val title: String) : UtteranceProgressListener(), AudioManager.OnAudioFocusChangeListener {
    private val am: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var afr: AudioFocusRequest? = null

    init {
        Log.d(TAG, "New State: " + this.javaClass.simpleName)
        tts?.setOnUtteranceProgressListener(this)
    }

    protected constructor(context: Context, tts: TextToSpeech?):
        this(context, tts, context.getString(R.string.app_name))

    protected constructor(state: LiveRideState):
        this(state.context, state.tts, state.title)

    abstract fun update(journey: Journey, whereIam: GeoPoint, accuracy: Int): LiveRideState
    abstract val isStopped: Boolean
    abstract fun arePedalling(): Boolean

    protected fun notify(seg: Segment) {
        notification(seg.street() + " " + seg.formattedDistance(), seg.toString())

        val instruction = StringBuilder()
        if (seg.turnInstruction().isNotEmpty())
            instruction.append(seg.turnInstruction()).append(" into ")
        instruction.append(seg.street().replace("un-", "un").replace("Un-", "un"))
        if (seg.turnInstruction().isNotEmpty())
            instruction.append(". Continue ").append(seg.formattedDistance())
        speak(instruction.toString())
    }

    protected fun notify(text: String, directionIcon: Int) {
        notification(text, text, directionIcon)
        speak(text)
    }

    @JvmOverloads
    protected fun notify(text: String, ticker: String = text) {
        notification(text, ticker)
        speak(text)
    }

    private fun notification(text: String, ticker: String, directionIcon: Int? = null) {
        val notificationIntent = Intent(context, LiveRideActivity::class.java)
        val contentIntent = PendingIntent.getActivity(context,
                                                      0,
                                                      notificationIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT)

        val notificationBuilder = CycleStreetsNotifications.getBuilder(context, CHANNEL_LIVERIDE_ID)
                .setSmallIcon(IconicsHelper.materialIcon(context = context, icon = GoogleMaterial.Icon.gmd_directions_bike).toAndroidIconCompat().toIcon())
                .setTicker(ticker)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)

        directionIcon?.let {
            notificationBuilder.setLargeIcon(Icon.createWithResource(context, it))
        }

        nm().notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    protected fun cancelNotification() {
        nm().cancel(NOTIFICATION_ID)
    }

    private fun nm(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun speak(words: String) {
        tts?.speak(speechify(words), TextToSpeech.QUEUE_ADD, null, null)
    }

    private fun speechify(words: String): String {
        return words
                .replace("LiveRide", "Live Ride")
                .replace(Arrivee.ARRIVEE, "arreev eh")
    }

    @Suppress("deprecation")
    override fun onStart(utteranceId: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            afr = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(AUDIO_ATTRIBUTES).build()
            am.requestAudioFocus(afr!!)
        } else {
            am.requestAudioFocus(this, CONTENT_TYPE_SPEECH, AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    @Suppress("deprecation")
    override fun onDone(utteranceId: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.abandonAudioFocusRequest(afr!!)
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
