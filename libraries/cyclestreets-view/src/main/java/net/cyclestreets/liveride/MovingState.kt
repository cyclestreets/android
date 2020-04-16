package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.routing.Journey
import org.osmdroid.util.GeoPoint

internal abstract class MovingState(previous: LiveRideState, private val transition_: Int) :
        LiveRideState(previous) {

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        var distanceFromEnd = journey.activeSegment()!!.distanceFromEnd(myLocation)
        distanceFromEnd -= accuracy

        if (distanceFromEnd < transition_) {
            return transitionState(journey)
        }
        return checkCourse(journey, myLocation, accuracy)
    }

    protected abstract fun transitionState(journey: Journey): LiveRideState

    private fun checkCourse(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        var distance = journey.activeSegment()!!.distanceFrom(myLocation)
        distance -= accuracy

        if (distance > CycleStreetsPreferences.replanDistance()) {
            return ReplanFromHere(this, myLocation)
        }
        if (distance > CycleStreetsPreferences.offtrackDistance()) {
            return GoingOffCourse(this)
        }
        return this
    }

    override fun isStopped(): Boolean { return false }
    override fun arePedalling(): Boolean { return true }
}
