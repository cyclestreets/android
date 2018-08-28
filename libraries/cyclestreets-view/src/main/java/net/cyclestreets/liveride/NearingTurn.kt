package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsPreferences

import net.cyclestreets.routing.Journey
import net.cyclestreets.util.TurnIcons

internal class NearingTurn(previous: LiveRideState, journey: Journey) :
        MovingState(previous, CycleStreetsPreferences.turnNowDistance()) {

    init {
        val turn: String? = journey.segments().get(journey.activeSegmentIndex() + 1).turn()

        if (!turn.isNullOrEmpty()) {
            notify("Get ready to $turn", TurnIcons.iconId(turn!!))
        } else {
            notify("You are approaching the ${Arrivee.ARRIVEE}")
        }
    }

    override fun transitionState(journey: Journey): LiveRideState {
        return AdvanceToSegment(this, journey)
    }
}
