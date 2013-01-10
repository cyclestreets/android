package net.cyclestreets.liveride;

import net.cyclestreets.api.Journey;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.speech.tts.TextToSpeech;

final class LiveRideStart extends LiveRideState
{
  LiveRideStart(final Context context, final TextToSpeech tts)
  {
    super(context, tts);
    notify("Live Ride", "Starting Live Ride");
  } // LiveRideStart
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    notify("Live Ride", "Live Ride");
    journey.setActiveSegmentIndex(0);
    notify(journey.activeSegment());
    return new HuntForSegment(this);
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return false; }
} // class LiveRideStart
