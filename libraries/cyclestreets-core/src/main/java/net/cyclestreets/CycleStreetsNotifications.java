package net.cyclestreets;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import net.cyclestreets.core.R;

public class CycleStreetsNotifications
{
  public final static String CHANNEL_LIVERIDE_ID = "liveride";

  public static void initialise(final Context context) {
    createNotificationChannels(context);
  }

  // see https://developer.android.com/training/notify-user/channels
  private static void createNotificationChannels(final Context context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = context.getString(R.string.channel_liveride_name);
      String description = context.getString(R.string.channel_liveride_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_LIVERIDE_ID, name, importance);
      channel.setDescription(description);

      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

}
