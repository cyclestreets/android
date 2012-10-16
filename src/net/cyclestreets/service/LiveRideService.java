package net.cyclestreets.service;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.LiveRideActivity;
import net.cyclestreets.R;
import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Segment;
import net.cyclestreets.api.Waypoints;
import net.cyclestreets.planned.Route;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class LiveRideService extends Service
  implements LocationListener, 
             OnInitListener,
             Route.Listener
{
  private static int NOTIFICATION_ID = 1;
  
  private enum Stage 
  {
    SETTING_OFF,
    HUNTING,
    ON_THE_MOVE,
    NEARING_TURN,
    MOVING_AWAY,
    MOVING_AWAY_FROM_START,
    ARRIVEE,
    REPLANNING,
    STOPPED;

    public boolean arePedalling() { return !isStopped(); }
    public boolean isStopped() { return this == STOPPED; }
    public boolean offCourse() { return this == MOVING_AWAY || this == MOVING_AWAY_FROM_START; }
  };
  
  private IBinder binder_;
  private LocationManager locationManager_ = null;
  private TextToSpeech tts_ = null;
  private Stage stage_ = Stage.STOPPED;

  @Override
  public void onCreate()
  {
    binder_ = new Binding();
    locationManager_ = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    tts_ = new TextToSpeech(this, this);
  } // onCreate

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId)
  {
    return Service.START_NOT_STICKY;
  } // onStartCommand

  @Override
  public void onDestroy()
  {
    stopRiding();
    cancelNotification();
    super.onDestroy();
  } // onDestroy

  @Override
  public IBinder onBind(final Intent intent)
  {
    return binder_;
  } // onBind

  public void startRiding()
  {
    if(!stage_.isStopped())
      return;
    
    stage_ = Stage.SETTING_OFF;
    Route.registerListener(this);
    locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
  } // startRiding

  public void stopRiding()
  {
    if(stage_.isStopped())
      return;

    Route.unregisterListener(this);
    locationManager_.removeUpdates(this);
    cancelNotification();
    stage_ = Stage.STOPPED;
  } // stopRiding

  public boolean areRiding()
  {
    return stage_.arePedalling();
  } // onRide

  public class Binding extends Binder
  {
    private LiveRideService service() { return LiveRideService.this; }
    public void startRiding() { service().startRiding(); }
    public void stopRiding() { service().stopRiding(); }
    public boolean areRiding() { return service().areRiding(); }
  } // class LocalBinder

  // ///////////////////////////////////////////////
  // location listener
  @Override
  public void onLocationChanged(final Location location)
  {
    if(!Route.available())
    {
      stopRiding();
      return;
    } // if ...

    final GeoPoint whereIam = new GeoPoint(location);

    final Journey journey = Route.journey();
    
    switch(stage_) 
    {
    case SETTING_OFF:
      journey.setActiveSegmentIndex(0);
      notify("Live Ride", "Starting Live Ride");
      notify(journey.activeSegment());
      stage_ = Stage.HUNTING;
      break;
    case HUNTING:
      findNearestSeg(journey, whereIam);
      break;
    case ON_THE_MOVE:
      checkIfTooFarAway(journey, whereIam);
      checkApproachingTurn(journey, whereIam);
      break;
    case NEARING_TURN:
      checkIfTooFarAway(journey, whereIam);
      checkNextSegImminent(journey, whereIam);
      break;
    case ARRIVEE:
      notify("You have arrived at your destination.", "Destination");
      stopRiding();
      break;
    case STOPPED:
      // whoa, something's gone wonky
      break;
    } // switch
  } // onLocationChanged
  
  private void findNearestSeg(final Journey journey, final GeoPoint whereIam)
  {
    Segment nearestSeg = null;
    int distance = Integer.MAX_VALUE;
    
    for(final Segment seg : journey.segments())
    {
      int from = seg.distanceFrom(whereIam);
      if(from < distance) 
      {
        distance = from;
        nearestSeg = seg;
      } // if ...
    } // for ...

    if(needsReplan(distance, whereIam))
      return;
    
    stage_ = Stage.ON_THE_MOVE;
    
    if(nearestSeg == journey.activeSegment())
      return;
    
    journey.setActiveSegment(nearestSeg);
    notify(nearestSeg);
  } // findNearestSeg
  
  private boolean needsReplan(final int distance, final GeoPoint whereIam) 
  {
    if(distance <= 50)
      return false;
    
    notify("Too far away. Re-planning the journey.");

    stage_ = Stage.REPLANNING;
    final GeoPoint finish = Route.waypoints().last();      
    Route.PlotRoute(CycleStreetsPreferences.routeType(), 
                    CycleStreetsPreferences.speed(),
                    this,
                    Waypoints.fromTo(whereIam, finish));
    return true;
  } // needsReplan
  
  private void checkIfTooFarAway(final Journey journey, final GeoPoint whereIam)
  {
    final int distance = journey.activeSegment().distanceFrom(whereIam);
    
    if(needsReplan(distance, whereIam))
      return;
    
    if(!stage_.offCourse() && (distance > 30))
    {
      notify(stage_ == Stage.SETTING_OFF ? "Some way from start" : "Moving away from route");
      stage_ = (stage_ == Stage.SETTING_OFF) ? Stage.MOVING_AWAY_FROM_START : Stage.MOVING_AWAY;
    } // if ...
    else if(stage_.offCourse() && (distance < 20))
    {
      notify("Getting back on track");
      stage_ = (stage_ == Stage.MOVING_AWAY_FROM_START) ? Stage.SETTING_OFF: Stage.ON_THE_MOVE;
    }
  } // checkIfTooFarAway
  
  private void checkApproachingTurn(final Journey journey, final GeoPoint whereIam)
  {
    final int distanceFromEnd = journey.activeSegment().distanceFromEnd(whereIam);

    if(distanceFromEnd > 30)
      return;
    
    notify("Get ready");
    stage_ = Stage.NEARING_TURN;
  } // checkApproachingTurn
  
  private void checkNextSegImminent(final Journey journey, final GeoPoint whereIam)
  {
    final int distanceFromEnd = journey.activeSegment().distanceFromEnd(whereIam);
    if(distanceFromEnd > 15)
      return;
    
    journey.advanceActiveSegment();
    notify(journey.activeSegment());
    
    stage_ = journey.atEnd() ? Stage.ARRIVEE : Stage.ON_THE_MOVE;
  } // checkNextSegImminent
  
  @Override
  public void onProviderDisabled(final String arg0)
  {
  } // onProviderDisabled

  @Override
  public void onProviderEnabled(String arg0)
  {
  } // onProviderEnabled

  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2)
  {
  } // onStatusChanged

  private void notify(final Segment seg) 
  {
    notification(seg.street() + " " + seg.distance(), seg.toString());
    
    final StringBuilder instruction = new StringBuilder();
    if(seg.turn().length() != 0)
      instruction.append(seg.turn()).append(" into ");
    instruction.append(seg.street().replace("un-", "un").replace("Un-", "un"));
    instruction.append(". Continue ").append(seg.distance());
    speak(instruction.toString());
  } // notify
  
  private void notify(final String text)
  {
    notify(text, text);
  } // notify
  
  private void notify(final String text, final String ticker) 
  {
    notification(text, ticker);
    speak(text);
  } // notify
  
  private void notification(final String text, final String ticker)
  {
    final NotificationManager nm = nm();
    final Notification notification = new Notification(R.drawable.icon, ticker, System.currentTimeMillis());
    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
    final Intent notificationIntent = new Intent(this, LiveRideActivity.class);
    final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    notification.setLatestEventInfo(this, "CycleStreets", text, contentIntent);
    nm.notify(NOTIFICATION_ID, notification);
  } // notify

  private void cancelNotification()
  {
    nm().cancel(NOTIFICATION_ID);
  } // cancelNotification

  private NotificationManager nm()
  {
    return (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
  } // nm

  private void speak(final String words)
  {
    tts_.speak(words, TextToSpeech.QUEUE_ADD, null);
  } // speak
  
  @Override
  public void onInit(int arg0)
  {
  } // onInit

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints)
  {    
    if(stage_ == Stage.REPLANNING)
      stage_ = Stage.HUNTING; 
  } // onNewJourney

  @Override
  public void onResetJourney()
  {
  } // onResetJourney
} // class LiveRideService
