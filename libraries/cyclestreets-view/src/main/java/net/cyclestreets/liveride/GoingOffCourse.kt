package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Segment
import org.osmdroid.util.GeoPoint

internal class GoingOffCourse(previous: LiveRideState) : LiveRideState(previous) {

    init {
        notify("Moving away from route")
    }

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        var nearestSeg: Segment = journey.segments.first()
        var distance = Int.MAX_VALUE

        for (seg in journey.segments) {
            val from = seg.distanceFrom(myLocation)
            if (from < distance) {
                distance = from
                nearestSeg = seg
            }
        }

        distance -= accuracy

        if (distance > CycleStreetsPreferences.replanDistance()) {
            return ReplanFromHere(this, myLocation)
        }
        if (nearestSeg !== journey.activeSegment()) {
            return AdvanceToSegment(this, journey, nearestSeg)
        }
        if (distance <= CycleStreetsPreferences.offtrackDistance() - 5) {
            notify("Getting back on track")
            return OnTheMove(this)
        }

        return this
    }

    override fun isStopped(): Boolean { return false }
    override fun arePedalling(): Boolean { return true }
}