package net.cyclestreets

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

import net.cyclestreets.core.R

object CycleStreetsNotifications {
    const val CHANNEL_LIVERIDE_ID = "liveride_v2"
    const val CHANNEL_TRACK_ID = "track"
    private val DEPRECATED_CHANNELS = listOf("liveride")

    fun initialise(context: Context) {
        createNotificationChannel(context,
                                  R.string.channel_liveride_name,
                                  R.string.channel_liveride_description,
                                  CHANNEL_LIVERIDE_ID)
        // When cyclestreets-track is integrated, we'll need to create this channel.
        // createNotificationChannel(context,
        //                           R.string.channel_track_name,
        //                           R.string.channel_track_description,
        //                           CHANNEL_TRACK_ID);
        deletedDeprecatedChannels(context, DEPRECATED_CHANNELS);
    }

    fun getBuilder(context: Context, channelId: String): Notification.Builder {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            getBuilderPreOreo(context)
        }
    }

    // see https://developer.android.com/training/notify-user/channels
    private fun createNotificationChannel(context: Context,
                                          nameResourceId: Int,
                                          descriptionResourceId: Int,
                                          channelId: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(nameResourceId)
            val description = context.getString(descriptionResourceId)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    private fun deletedDeprecatedChannels(context: Context, channelIds: List<String>) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            for (channelId in channelIds) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.deleteNotificationChannel(channelId)
            }
        }
    }

    @Suppress("deprecation")
    private fun getBuilderPreOreo(context: Context): Notification.Builder {
        return Notification.Builder(context)
    }
}
