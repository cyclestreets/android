package net.cyclestreets;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import net.cyclestreets.core.R;

public class CycleStreetsNotifications
{
  public final static String CHANNEL_LIVERIDE_ID = "liveride";
  public final static String CHANNEL_TRACK_ID = "track";

  public static void initialise(final Context context) {
    createNotificationChannel(context,
                              R.string.channel_liveride_name,
                              R.string.channel_liveride_description,
                              CHANNEL_LIVERIDE_ID);
    // When cyclestreets-track is integrated, we'll need to create this channel.
    // createNotificationChannel(context,
    //                           R.string.channel_track_name,
    //                           R.string.channel_track_description,
    //                           CHANNEL_TRACK_ID);
  }

  // see https://developer.android.com/training/notify-user/channels
  private static void createNotificationChannel(final Context context,
                                                int nameResourceId,
                                                int descriptionResourceId,
                                                String channelId) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = context.getString(nameResourceId);
      String description = context.getString(descriptionResourceId);
      int importance = NotificationManager.IMPORTANCE_LOW;
      NotificationChannel channel = new NotificationChannel(channelId, name, importance);
      channel.setDescription(description);

      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  public static Notification.Builder getBuilder(Context context, String channelId) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      return new Notification.Builder(context, channelId);
    } else {
      return getBuilderPreOreo(context);
    }
  }

  @SuppressWarnings("deprecation")
  private static Notification.Builder getBuilderPreOreo(Context context) {
    return new Notification.Builder(context);
  }
}
