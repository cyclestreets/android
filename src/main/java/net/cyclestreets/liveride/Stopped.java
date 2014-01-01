package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

import android.content.Context;

final class Stopped extends LiveRideState
{
  Stopped(final Context context) 
  {
    super(context, null);
    cancelNotification();
  } // Stopped
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy) 
  { 
    return this; 
  } // update

  @Override
  public boolean isStopped() { return true; }
  @Override
  public boolean arePedalling() { return false; }
} // class Stopped
