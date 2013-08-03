package net.cyclestreets.liveride;

import net.cyclestreets.LiveRideActivity;
import net.cyclestreets.R;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segment;

import org.osmdroid.util.GeoPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

public abstract class LiveRideState
{
  private static final int NOTIFICATION_ID = 1;
  
  protected static final int RIGHT_OFF_PISTE = 50;
  protected static final int OFF_PISTE = 30;

  protected static final int APPROACHING_TURN = 40;
  protected static final int TURN_NOW = 20;

  static public LiveRideState InitialState(final Context context) 
  { 
    final TextToSpeech tts = new TextToSpeech(context, 
          new TextToSpeech.OnInitListener() { public void onInit(int arg0) { } }
    );
    return new LiveRideStart(context, tts); 
  } // InitialState
  
  static public LiveRideState StoppedState(final Context context) 
  { 
    return new Stopped(context); 
  } // StoppedState
  //////////////////////////////////////////
  
  private Context context_;
  private TextToSpeech tts_;
  
  protected LiveRideState(final Context context, final TextToSpeech tts) 
  {
    context_ = context;
    tts_ = tts;
  } // LiveRideState
  
  protected LiveRideState(final LiveRideState state) 
  {
    context_ = state.context();
    tts_ = state.tts();
  } // LiveRideState
  
  public abstract LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy);
  public abstract boolean isStopped();
  public abstract boolean arePedalling();
  
  protected Context context() { return context_; }
  protected TextToSpeech tts() { return tts_; }
  
  protected void notify(final Segment seg) 
  {
    notification(seg.street() + " " + seg.distance(), seg.toString());
    
    final StringBuilder instruction = new StringBuilder();
    if(seg.turn().length() != 0)
      instruction.append(seg.turn()).append(" into ");
    instruction.append(seg.street().replace("un-", "un").replace("Un-", "un"));
    instruction.append(". Continue ").append(seg.distance());
    speak(instruction.toString());
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
    final Notification notification = new Notification(R.drawable.icon, ticker, System.currentTimeMillis());
    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
    final Intent notificationIntent = new Intent(context(), LiveRideActivity.class);
    final PendingIntent contentIntent = PendingIntent.getActivity(context(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    notification.setLatestEventInfo(context(), "CycleStreets", text, contentIntent);
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
    tts().speak(words, TextToSpeech.QUEUE_ADD, null);
  } // speak
} // interface LiveRideState

