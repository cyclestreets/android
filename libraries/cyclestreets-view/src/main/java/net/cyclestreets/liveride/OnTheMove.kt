package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.routing.Journey

internal class OnTheMove(previous: LiveRideState) :
        MovingState(previous, CycleStreetsPreferences.nearingTurnDistance()) {

    override fun transitionState(journey: Journey): LiveRideState {
        return NearingTurn(this, journey)
    }
}
