package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsNotifications
import net.cyclestreets.LiveRideActivity
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Segment

import org.osmdroid.util.GeoPoint

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.speech.tts.TextToSpeech
import android.util.Log

import net.cyclestreets.CycleStreetsNotifications.CHANNEL_LIVERIDE_ID
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R

internal fun initialState(context: Context): LiveRideState {
    val tts = TextToSpeech(context) { _ -> }
    return LiveRideStart(context, tts)
}

internal fun stoppedState(context: Context): LiveRideState {
    return Stopped(context)
}

private val TAG = Logging.getTag(LiveRideState::class.java)
private const val NOTIFICATION_ID = 1

internal abstract class LiveRideState(protected val context: Context,
                                      val tts: TextToSpeech?,
                                      private val title: String) {
    init {
        Log.d(TAG, "New State: " + this.javaClass.simpleName)
    }

    protected constructor(context: Context, tts: TextToSpeech?):
        this(context, tts, context.getString(R.string.app_name))

    protected constructor(state: LiveRideState):
        this(state.context, state.tts, state.title)

    abstract val isStopped: Boolean
    abstract fun update(journey: Journey, whereIam: GeoPoint, accuracy: Int): LiveRideState
    abstract fun arePedalling(): Boolean

    protected fun notify(seg: Segment) {
        notification(seg.street() + " " + seg.distance(), seg.toString())

        val instruction = StringBuilder()
        if (seg.turn().isNotEmpty())
            instruction.append(seg.turn()).append(" into ")
        instruction.append(seg.street().replace("un-", "un").replace("Un-", "un"))
        if (seg.turn().isNotEmpty())
            instruction.append(". Continue ").append(seg.distance())
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
                .setSmallIcon(R.drawable.baseline_directions_bike_24)
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
                .replace("Live", "Lyve") // Otherwise some TTS engines pronounce as "lɪv" instead of "laɪv"
                .replace(Arrivee.ARRIVEE, "arreev eh")
    }
}
