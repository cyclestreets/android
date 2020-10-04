package net.cyclestreets.liveride

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.speech.tts.TextToSpeech
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
import org.osmdroid.util.GeoPoint
import java.util.*

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

    abstract fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState
    abstract fun isStopped(): Boolean
    abstract fun arePedalling(): Boolean

    protected fun notify(seg: Segment) {
        notification(seg.street() + " " + seg.formattedDistance(), seg.toString())

        val instruction = turnInto(seg)
        if (seg.turnInstruction().isNotEmpty()) {
            instruction.append(". Continue ").append(seg.formattedDistance())
        }
        speak(instruction.toString())
    }

    protected fun turnInto(seg: Segment): StringBuilder {
        val instruction = StringBuilder()
        if (seg.turnInstruction().isNotEmpty()) {
            instruction.append(seg.turnInstruction()).append(" into ")
        }
        instruction.append(fixStreet(seg.street()))
        return instruction
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

    protected fun notifyAndSetServiceForeground(service: Service, text: String) {
        val notification = getNotification(text, text, null)
        service.startForeground(NOTIFICATION_ID, notification)
        speak(text)
    }

    private fun notification(text: String, ticker: String, directionIcon: Int? = null) {
        val notification = getNotification(text, ticker, directionIcon)
        nm().notify(NOTIFICATION_ID, notification)
    }

    private fun getNotification(text: String, ticker: String, directionIcon: Int? = null): Notification {
        val notificationIntent = Intent(context, LiveRideActivity::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

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

    private fun speak(words: String) {
        tts?.speak(speechify(words), TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
    }

}
