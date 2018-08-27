package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsPreferences

import net.cyclestreets.routing.Journey

internal class NearingTurn(previous: LiveRideState, journey: Journey) :
        MovingState(previous, CycleStreetsPreferences.turnNowDistance()) {

    init {
        val turn: String? = journey.segments().get(journey.activeSegmentIndex() + 1).turn()

        if (!turn.isNullOrEmpty()) {
            notify("Get ready to $turn")
        } else {
            notify("You are approaching the ${Arrivee.ARRIVEE}")
        }
    }

    override fun transitionState(journey: Journey): LiveRideState {
        return AdvanceToSegment(this, journey)
    }
}
