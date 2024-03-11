package net.cyclestreets.liveride

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.toAndroidIconCompat
import net.cyclestreets.CycleStreetsNotifications
import net.cyclestreets.CycleStreetsNotifications.CHANNEL_LIVERIDE_ID
import net.cyclestreets.LiveRideActivity
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Segment
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R
import net.cyclestreets.views.overlay.MuteButtonOverlay
import org.osmdroid.util.GeoPoint
import java.util.*
import org.osmdroid.views.MapView

private val TAG = Logging.getTag(LiveRideState::class.java)
private const val NOTIFICATION_ID = 1

internal abstract class LiveRideState(protected val context: Context,
                                      val tts: TextToSpeech?,
                                      private val title: String) {
    init {
        Log.d(TAG, "New State: " + this.javaClass.simpleName)


    }

    private val sharedPreferences: SharedPreferences by lazy{
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    protected constructor(context: Context, tts: TextToSpeech?):
        this(context, tts, context.getString(R.string.app_name))

    protected constructor(state: LiveRideState):
        this(state.context, state.tts, state.title)

    abstract fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState
    abstract fun isStopped(): Boolean
    abstract fun arePedalling(): Boolean

    protected fun notify(seg: Segment, important: Boolean = false) {
        notification(seg.street() + " " + seg.formattedDistance(), seg.toString())

        val instruction = turnInto(seg)
        if (seg.turnInstruction().isNotEmpty()) {
            instruction.append(". Continue ").append(seg.formattedDistance())
        }

        speak(instruction.toString(), important)
    }

    protected fun turnInto(seg: Segment): StringBuilder {
        val instruction = StringBuilder()
        if (seg.turnInstruction().isNotEmpty()) {
            instruction.append(seg.turnInstruction()).append(" into ")
        }
        instruction.append(fixStreet(seg.street()))
        return instruction
    }

    // checked
    protected fun notify(text: String, directionIcon: Int, important: Boolean = false) {



             notification(text, text, directionIcon)
             speak(text, important)

    }

    @JvmOverloads
    protected fun notify(text: String, ticker: String = text, important: Boolean = false) {

            notification(text, ticker)
            speak(text, important)

    }

    protected fun notifyAndSetServiceForeground(service: Service, text: String) {
        val notification = getNotification(text, text, null)
        service.startForeground(NOTIFICATION_ID, notification)
        speak(text, true)
    }

    private fun notification(text: String, ticker: String, directionIcon: Int? = null) {
        val notification = getNotification(text, ticker, directionIcon)
        nm().notify(NOTIFICATION_ID, notification)
    }

    private fun getNotification(text: String, ticker: String, directionIcon: Int? = null): Notification {
        val notificationIntent = Intent(context, LiveRideActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = CycleStreetsNotifications.getBuilder(context, CHANNEL_LIVERIDE_ID)
                .setSmallIcon(materialIcon(context, GoogleMaterial.Icon.gmd_directions_bike).toAndroidIconCompat().toIcon(context))
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

        return notificationBuilder.build()
    }

    protected fun cancelNotification() {
        nm().cancel(NOTIFICATION_ID)
    }

    private fun nm(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    companion object {
        private const val LOCK_PREF = "muteButton"
    }



    private fun speak(words: String, important: Boolean = false) {
       //mute = false
        val muteAudio: Boolean = sharedPreferences.getBoolean("MuteAudio", false)

        if(!muteAudio){
            Log.i("Is it mute: " , muteAudio.toString())
            if (important) {
                tts?.speak(speechify(words), TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
            } else {
                tts?.speak(speechify(words), TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
            }
       } else {
            Log.i("Is it mute: " , muteAudio.toString())
        }


    }
}
