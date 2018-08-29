package net.cyclestreets.liveride

import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints

import org.osmdroid.util.GeoPoint

internal class ReplanFromHere(previous: LiveRideState, whereIam: GeoPoint) : LiveRideState(previous), Route.Listener {
    private var next: LiveRideState? = null

    init {
        notify("Too far away. Re-planning the journey.")

        next = this

        val finish = Route.waypoints().last()
        Route.softRegisterListener(this)
        Route.PlotRoute(
            CycleStreetsPreferences.routeType(),
            CycleStreetsPreferences.speed(),
            context,
            Waypoints.fromTo(whereIam, finish)
        )
    }

    override fun update(journey: Journey, whereIam: GeoPoint, accuracy: Int): LiveRideState {
        return next!!
    }

    override val isStopped: Boolean
        get() = false
    override fun arePedalling(): Boolean {
        return true
    }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        next = HuntForSegment(this)
        Route.unregisterListener(this)
    }

    override fun onResetJourney() {}
}
