package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

import android.speech.tts.TextToSpeech;

final class LiveRideStart extends LiveRideState
{
  LiveRideStart(final LiveRideService liveRideService, final TextToSpeech tts) {
    super(liveRideService, tts);
    notifyAndSetServiceForeground(liveRideService, "Starting LiveRide");
  }

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy) {
    journey.setActiveSegmentIndex(0);
    notify(journey.activeSegment());
    return new HuntForSegment(this);
  }

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return false; }
}
