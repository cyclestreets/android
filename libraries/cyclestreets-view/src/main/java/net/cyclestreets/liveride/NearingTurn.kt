package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsPreferences

import net.cyclestreets.routing.Journey
import net.cyclestreets.util.TurnIcons

internal class NearingTurn(previous: LiveRideState, journey: Journey) :
        MovingState(previous, CycleStreetsPreferences.turnNowDistance()) {

    init {
        val segment = journey.segments.get(journey.activeSegmentIndex() + 1)

        if (!segment.turnInstruction().isNullOrEmpty()) {
            notify("Get ready to ${turnInto(segment)}", TurnIcons.iconId(segment.turn()))
        } else {
            notify("You are approaching the ${Arrivee.ARRIVEE}")
        }
    }

    override fun transitionState(journey: Journey): LiveRideState {
        return AdvanceToSegment(this, journey)
    }
}
