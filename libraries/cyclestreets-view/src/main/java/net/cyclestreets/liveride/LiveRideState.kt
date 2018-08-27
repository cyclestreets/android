package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsNotifications
import net.cyclestreets.LiveRideActivity
import net.cyclestreets.view.R
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Segment

import org.osmdroid.util.GeoPoint

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.speech.tts.TextToSpeech
import android.util.Log
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon

import net.cyclestreets.CycleStreetsNotifications.CHANNEL_LIVERIDE_ID
import net.cyclestreets.iconics.IconicsHelper
import net.cyclestreets.util.Logging

fun initialState(context: Context): LiveRideState {
    val tts = TextToSpeech(context) { _ -> }
    return LiveRideStart(context, tts)
}

fun stoppedState(context: Context): LiveRideState {
    return Stopped(context)
}

private val TAG = Logging.getTag(LiveRideState::class.java)
private const val NOTIFICATION_ID = 1

abstract class LiveRideState(protected val context: Context,
                             val tts: TextToSpeech,
                             private val title: String) {

    init {
        Log.d(TAG, "New State: " + this.javaClass.simpleName)
    }

    protected constructor(context: Context, tts: TextToSpeech):
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
        instruction.append(". Continue ").append(seg.distance())
        speak(instruction.toString())
    }

    @JvmOverloads
    protected fun notify(text: String, ticker: String = text) {
        notification(text, ticker)
        speak(text)
    }

    private fun notification(text: String, ticker: String) {
        val notificationIntent = Intent(context, LiveRideActivity::class.java)
        val contentIntent = PendingIntent.getActivity(context,
                                                      0,
                                                      notificationIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = CycleStreetsNotifications.getBuilder(context, CHANNEL_LIVERIDE_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(ticker)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .build()

        nm().notify(NOTIFICATION_ID, notification)
    }

    protected fun cancelNotification() {
        nm().cancel(NOTIFICATION_ID)
    }

    private fun nm(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun speak(words: String) {
        tts.speak(speechify(words), TextToSpeech.QUEUE_ADD, null, null)
    }

    private fun speechify(words: String): String {
        return words
                .replace("LiveRide", "Live Ride")
                .replace("Live", "<speak xml:lang=\"en-US\"><phoneme alphabet=\"xsampa\" ph=\"la_Iv\"/></speak>")
                .replace(Arrivee.ARRIVEE, "<speak xml:lang=\"en-US\"><phoneme alphabet=\"xsampa\" ph=\"ari:ve:\"/></speak>")
    }
}
