package net.cyclestreets.liveride

import android.util.Log
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Segment
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.Logging

import org.osmdroid.util.GeoPoint

private val TAG = Logging.getTag(ReplanFromHere::class.java)

internal class ReplanFromHere(previous: LiveRideState, whereIam: GeoPoint) : LiveRideState(previous), Route.Listener {
    private var next: LiveRideState? = null

    init {
        notify("Too far away. Re-planning the journey.")

        next = this

        val activeSegment = Route.journey().activeSegment()
        val remainingWaypoints: Waypoints = when (activeSegment) {
            is Segment.Start ->  Route.waypoints().startingWith(whereIam)
            is Segment.End -> Waypoints.fromTo(whereIam, Route.waypoints().last())
            is Segment.Waymark -> Route.waypoints().fromLeg(activeSegment.legNumber()).startingWith(whereIam)
            is Segment.Step -> Route.waypoints().fromLeg(activeSegment.legNumber()).startingWith(whereIam)
            else -> {
                Log.w(TAG, "Unexpected segment type ${activeSegment?.javaClass ?: "'null'"}")
                throw IllegalStateException("Unexpected segment type ${activeSegment?.javaClass ?: "'null'"}")
            }
        }
        Route.softRegisterListener(this)
        Route.LiveReplanRoute(CycleStreetsPreferences.routeType(),
                              CycleStreetsPreferences.speed(),
                              context,
                              remainingWaypoints)
    }

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        return next!!
    }

    override fun isStopped(): Boolean { return false }
    override fun arePedalling(): Boolean { return true }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        next = HuntForSegment(this)
        Route.unregisterListener(this)
    }

    override fun onResetJourney() {}
}
