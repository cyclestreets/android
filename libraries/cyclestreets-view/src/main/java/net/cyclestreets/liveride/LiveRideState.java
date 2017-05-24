package net.cyclestreets.liveride;

import net.cyclestreets.CycleStreetsPreferences;
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
  private final PebbleNotifier pebbleNotifier_;

  static public LiveRideState InitialState(final Context context, final PebbleNotifier pebbleNotifier)
  { 
    final TextToSpeech tts = new TextToSpeech(context, 
          new TextToSpeech.OnInitListener() { public void onInit(int arg0) { } }
    );
    return new LiveRideStart(context, pebbleNotifier, tts);
  } // InitialState
  
  static public LiveRideState StoppedState(final Context context, PebbleNotifier pebbleNotifier)
  { 
    return new Stopped(context, pebbleNotifier);
  } // StoppedState
  //////////////////////////////////////////
  
  private Context context_;
  private String title_;
  private TextToSpeech tts_;
  
  protected LiveRideState(final Context context, final PebbleNotifier pebbleNotifier, final TextToSpeech tts)
  {
    context_ = context;
    pebbleNotifier_ = pebbleNotifier;
    tts_ = tts;
    title_ = context.getString(context.getApplicationInfo().labelRes);
    Log.d("CS_PEBBLE LRS", "New State: " + this.getClass().getSimpleName());
  } // LiveRideState
  
  protected LiveRideState(final LiveRideState state) 
  {
    context_ = state.context();
    pebbleNotifier_ = state.getPebbleNotifier();
    tts_ = state.tts();
    Log.d("CS_PEBBLE LRS", "State: " + this.getClass().getSimpleName());
  } // LiveRideState


  public abstract LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy);
  public abstract boolean isStopped();
  public abstract boolean arePedalling();
  
  protected Context context() { return context_; }
  protected TextToSpeech tts() { return tts_; }
  protected PebbleNotifier getPebbleNotifier() {
    return pebbleNotifier_;
  }

  protected void notify(final Segment seg) 
  {
    notification(seg.street() + " " + seg.distance(), seg.toString());
    
    final StringBuilder instruction = new StringBuilder();
    if(seg.turn().length() != 0)
      instruction.append(seg.turn()).append(" into ");
    instruction.append(seg.street().replace("un-", "un").replace("Un-", "un"));
    instruction.append(". Continue ").append(seg.distance());
    speak(instruction.toString());
    getPebbleNotifier().notify(this, seg);
  } // notify
  
  protected void notify(final String text)
  {
    notify(text, text);
  } // notify
  
  protected void notify(final String text, final String ticker) 
  {
    notification(text, ticker);
    speak(text);
  } // notify
  
  private void notification(final String text, final String ticker)
  {
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
            .getNotification();

    nm.notify(NOTIFICATION_ID, notification);
  } // notify

  protected void cancelNotification()
  {
    nm().cancel(NOTIFICATION_ID);
  } // cancelNotification

  private NotificationManager nm()
  {
    return (NotificationManager)context().getSystemService(Context.NOTIFICATION_SERVICE);
  } // nm

  private void speak(final String words)
  {
    String toSpeak = words.replace("LiveRide", "Live Ride");

    if (getPebbleNotifier().isConnected()) {
      if (CycleStreetsPreferences.pebbleVoice())
        tts().speak(toSpeak, TextToSpeech.QUEUE_ADD, null);
    } else
      tts().speak(toSpeak, TextToSpeech.QUEUE_ADD, null);
  } // speak
} // interface LiveRideState

