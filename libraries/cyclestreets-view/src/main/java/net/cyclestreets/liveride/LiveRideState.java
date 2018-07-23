package net.cyclestreets.liveride;

import net.cyclestreets.LiveRideActivity;
import net.cyclestreets.view.R;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segment;

import org.osmdroid.util.GeoPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public abstract class LiveRideState
{
  private static final int NOTIFICATION_ID = 1;

  private final Context context_;
  private final TextToSpeech tts_;
  private String title_;

  public static LiveRideState InitialState(final Context context) {
    final TextToSpeech tts = new TextToSpeech(context, arg0 -> { });
    return new LiveRideStart(context, tts);
  }

  public static LiveRideState StoppedState(final Context context) {
    return new Stopped(context);
  }
  //////////////////////////////////////////

  protected LiveRideState(final Context context, final TextToSpeech tts) {
    context_ = context;
    tts_ = tts;
    title_ = context.getString(context.getApplicationInfo().labelRes);
    Log.d("LiveRideState", "New State: " + this.getClass().getSimpleName());
  }

  protected LiveRideState(final LiveRideState state) {
    context_ = state.context();
    tts_ = state.tts();
    Log.d("LiveRideState", "State: " + this.getClass().getSimpleName());
  }

  public abstract LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy);
  public abstract boolean isStopped();
  public abstract boolean arePedalling();

  protected Context context() { return context_; }
  protected TextToSpeech tts() { return tts_; }

  protected void notify(final Segment seg) {
    notification(seg.street() + " " + seg.distance(), seg.toString());

    final StringBuilder instruction = new StringBuilder();
    if (seg.turn().length() != 0)
      instruction.append(seg.turn()).append(" into ");
    instruction.append(seg.street().replace("un-", "un").replace("Un-", "un"));
    instruction.append(". Continue ").append(seg.distance());
    speak(instruction.toString());
  }

  protected void notify(final String text) {
    notify(text, text);
  }

  protected void notify(final String text, final String ticker) {
    notification(text, ticker);
    speak(text);
  }

  private void notification(final String text, final String ticker) {
    final NotificationManager nm = nm();
    final Intent notificationIntent = new Intent(context(), LiveRideActivity.class);
    final PendingIntent contentIntent = PendingIntent.getActivity(context(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    Notification notification = new Notification.Builder(context())
            .setSmallIcon(R.drawable.ic_launcher)
            .setTicker(ticker)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentTitle(title_)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .build();

    nm.notify(NOTIFICATION_ID, notification);
  }

  protected void cancelNotification() {
    nm().cancel(NOTIFICATION_ID);
  }

  private NotificationManager nm() {
    return (NotificationManager)context().getSystemService(Context.NOTIFICATION_SERVICE);
  }

  private void speak(final String words) {
    String toSpeak = words.replace("LiveRide", "Live Ride");
    tts().speak(toSpeak, TextToSpeech.QUEUE_ADD, null, null);
  }
}
