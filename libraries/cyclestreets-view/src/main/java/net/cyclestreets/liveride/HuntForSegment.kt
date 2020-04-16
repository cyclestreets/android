package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Segment
import org.osmdroid.util.GeoPoint

internal class HuntForSegment(state: LiveRideState) : LiveRideState(state) {

    private var waitToSettle = 5

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        if (waitToSettle > 0) {
            --waitToSettle
            return this
        }

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
        if (nearestSeg === journey.activeSegment()) {
            return OnTheMove(this)
        }
        return AdvanceToSegment(this, journey, nearestSeg)
    }

    override fun isStopped(): Boolean { return false }
    override fun arePedalling(): Boolean { return true }
}
