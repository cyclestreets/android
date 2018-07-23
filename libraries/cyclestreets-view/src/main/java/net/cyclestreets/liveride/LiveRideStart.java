package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.speech.tts.TextToSpeech;

final class LiveRideStart extends LiveRideState
{
  LiveRideStart(final Context context, final TextToSpeech tts) {
    super(context, tts);
    notify("Starting LiveRide", "Starting LiveRide");
  }

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy) {
    notify("LiveRide", "LiveRide");
    journey.setActiveSegmentIndex(0);
    notify(journey.activeSegment());
    return new HuntForSegment(this);
  }

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return false; }
}
