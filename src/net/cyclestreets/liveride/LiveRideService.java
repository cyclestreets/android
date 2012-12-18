package net.cyclestreets.liveride;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.api.Journey;
import net.cyclestreets.planned.Route;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LiveRideService extends Service
  implements LocationListener
{
  private IBinder binder_;
  private LocationManager locationManager_ = null;
  private LiveRideState stage_;

  @Override
  public void onCreate()
  {
    binder_ = new Binding();
    locationManager_ = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
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
    
    stage_ = LiveRideState.InitialState(this);
    locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
  } // startRiding

  public void stopRiding()
  {
    if(stage_.isStopped())
      return;

    locationManager_.removeUpdates(this);
    stage_ = LiveRideState.StoppedState(this);
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
 
    stage_ = stage_.update(journey, whereIam);
/*    switch(stage_) 
    {
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
*/
    
  } // onLocationChanged

/*  private void checkIfTooFarAway(final Journey journey, final GeoPoint whereIam)
  {
    final int distance = journey.activeSegment().distanceFrom(whereIam);
    
    if(needsReplan(distance, whereIam))
      return;
    
    if(!stage_.offCourse() && (distance > NEAR_DISTANCE))
    {
      notify(stage_ == Stage.SETTING_OFF ? "Some way from start" : "Moving away from route");
      stage_ = (stage_ == Stage.SETTING_OFF) ? Stage.MOVING_AWAY_FROM_START : Stage.MOVING_AWAY;
    } // if ...
    else if(stage_.offCourse() && (distance < (NEAR_DISTANCE-5)))
    {
      notify("Getting back on track");
      stage_ = (stage_ == Stage.MOVING_AWAY_FROM_START) ? Stage.SETTING_OFF: Stage.ON_THE_MOVE;
    }
  } // checkIfTooFarAway
  
  private void checkApproachingTurn(final Journey journey, final GeoPoint whereIam)
  {
    final int distanceFromEnd = journey.activeSegment().distanceFromEnd(whereIam);

    if(distanceFromEnd > NEAR_DISTANCE)
      return;
    
    notify("Get ready");
    stage_ = Stage.NEARING_TURN;
  } // checkApproachingTurn
  
  private void checkNextSegImminent(final Journey journey, final GeoPoint whereIam)
  {
    final int distanceFromEnd = journey.activeSegment().distanceFromEnd(whereIam);
    if(distanceFromEnd > IMMEDIATE_DISTANCE)
      return;
    
    journey.advanceActiveSegment();
    notify(journey.activeSegment());
    
    stage_ = journey.atEnd() ? Stage.ARRIVEE : Stage.ON_THE_MOVE;
  } // checkNextSegImminent
  */
  
  @Override
  public void onProviderDisabled(String arg0) { }
  @Override
  public void onProviderEnabled(String arg0) { }
  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2) { }
} // class LiveRideService
