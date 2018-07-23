package net.cyclestreets.liveride;

import net.cyclestreets.routing.Segment;
import net.cyclestreets.CycleStreetsPreferences;

import net.cyclestreets.routing.Journey;

final class NearingTurn extends MovingState
{
  NearingTurn(final LiveRideState previous, final Journey journey) {
    super(previous, CycleStreetsPreferences.turnNowDistance());

    final Segment segment = journey.segments().get(journey.activeSegmentIndex() + 1);
    String nextAction = (segment.turn() != null && !segment.turn().isEmpty()) ? segment.turn() : Arrivee.ARRIVERAI;
    notify("Get ready to " + nextAction);
  }

  @Override
  protected LiveRideState transitionState(final Journey journey) {
    return new AdvanceToSegment(this, journey);
  }
}
