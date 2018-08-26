package net.cyclestreets.liveride;

import net.cyclestreets.routing.Segment;
import net.cyclestreets.CycleStreetsPreferences;

import net.cyclestreets.routing.Journey;

final class NearingTurn extends MovingState
{
  NearingTurn(final LiveRideState previous, final Journey journey) {
    super(previous, CycleStreetsPreferences.turnNowDistance());

    final Segment segment = journey.segments().get(journey.activeSegmentIndex() + 1);
    if (segment.turn() != null && !segment.turn().isEmpty()) {
      notify("Get ready to " + segment.turn());
    } else {
      notify("You are approaching the " + Arrivee.ARRIVEE);
    }
  }

  @Override
  protected LiveRideState transitionState(final Journey journey) {
    return new AdvanceToSegment(this, journey);
  }
}
