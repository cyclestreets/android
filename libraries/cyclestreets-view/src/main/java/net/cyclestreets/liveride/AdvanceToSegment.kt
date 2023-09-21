package net.cyclestreets.liveride

import android.util.Log
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Segment
import org.osmdroid.util.GeoPoint

internal class AdvanceToSegment @JvmOverloads constructor(previous: LiveRideState,
                                                          journey: Journey,
                                                          segment: Segment? = journey.segments[journey.activeSegmentIndex() + 1]) : LiveRideState(previous) {
    init {
        journey.setActiveSegment(segment!!)
        // this might not be an important message
        Log.d("importantTest", "AdvanceToSegment Init: ${segment.toString()}")
        notify(segment, true)
    }

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        if (journey.atWaypoint()) {
            return PassingWaypoint(this)
        }
        if (journey.atEnd()) {
            return Arrivee(this)
        }
        return OnTheMove(this)
    }

    override fun isStopped(): Boolean { return false }
    override fun arePedalling(): Boolean { return true }
}