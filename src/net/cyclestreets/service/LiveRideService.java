package net.cyclestreets.service;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.CycleStreets;
import net.cyclestreets.R;
import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Segment;
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

public class LiveRideService extends Service implements LocationListener, OnInitListener
{
  private IBinder binder_;
  private LocationManager locationManager_;
  private TextToSpeech tts_;
  private boolean riding_;

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
    if(riding_)
      return;
    
    locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    riding_ = true;
    notify("Live Ride", "Starting Live Ride");
  } // startRiding
  
  public void stopRiding() 
  {
    if(!riding_)
      return;
    
    locationManager_.removeUpdates(this);
    riding_ = false;
  } // stopRiding
  
  public boolean areRiding()
  {
    return riding_;
  } // onRide
  
  public class Binding extends Binder 
  {
    private LiveRideService service() 
    {
      return LiveRideService.this;
    } // getService
    
    public void startRiding() { service().startRiding(); }
    public void stopRiding() { service().stopRiding(); }
    public boolean areRiding() { return service().areRiding(); }
  } // class LocalBinder

  /////////////////////////////////////////////////
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

    int minDistance = Integer.MAX_VALUE;
    Segment nearestSeg = null;
    for(final Segment seg : journey.segments())
      for(final GeoPoint pos : seg.points())
      {
        int distance = pos.distanceTo(whereIam);
        if(distance > minDistance)
          continue;
        
        minDistance = distance;
        nearestSeg = seg;
      } // for ...
        
    if(nearestSeg != journey.activeSegment())
    {
      journey.setActiveSegment(nearestSeg);
      
      notify(nearestSeg.street() + " " + nearestSeg.distance(), 
             nearestSeg.toString());
    } // if ...
    
    if(journey.atEnd())
    {
      notify("Arrivee", "Arrivee");
      stopRiding();
    } // if ...
  } // onLocationChanged

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

  private void notify(final String text, final String ticker)
  {
    final NotificationManager nm = nm();
    final Notification notification = new Notification(R.drawable.icon, ticker, System.currentTimeMillis());
    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
    final Intent notificationIntent = new Intent(this, CycleStreets.class);
    final PendingIntent contentIntent = 
         PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    notification.setLatestEventInfo(this, "CycleStreets", text, contentIntent);
    nm.notify(1, notification);
    
    tts_.speak(text, TextToSpeech.QUEUE_FLUSH, null);
  } // notify
  
  private void cancelNotification()
  {
    final NotificationManager nm = nm();
    nm.cancel(1);
  } // cancelNotification

  private NotificationManager nm() 
  {
    return (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
  } // nm

  @Override
  public void onInit(int arg0)
  {
  } // onInit
} // class LiveRideService
