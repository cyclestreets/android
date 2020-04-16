package net.cyclestreets.liveride

import net.cyclestreets.routing.Journey
import org.osmdroid.util.GeoPoint

internal class PassingWaypoint(previous: LiveRideState?) : LiveRideState(previous!!) {

    init {
        notify("Passing waypoint")
    }

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        return AdvanceToSegment(this, journey)
    }

    override fun isStopped(): Boolean { return false }
    override fun arePedalling(): Boolean { return true }
}
