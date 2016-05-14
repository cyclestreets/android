package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.speech.tts.TextToSpeech;

final class LiveRideStart extends LiveRideState
{
  LiveRideStart(final Context context, final PebbleNotifier pebbleNotifier, final TextToSpeech tts)
  {
    super(context, pebbleNotifier, tts);
    notify("Starting LiveRide", "Starting LiveRide");
  } // LiveRideStart
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    notify("LiveRide", "LiveRide");
    journey.setActiveSegmentIndex(0);
    notify(journey.activeSegment());
    getPebbleNotifier().notifyStart(this, journey.activeSegment());
    return new HuntForSegment(this);
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return false; }
} // class LiveRideStart
