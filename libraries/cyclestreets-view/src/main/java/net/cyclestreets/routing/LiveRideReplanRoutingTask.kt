package net.cyclestreets.routing

import android.content.Context

import net.cyclestreets.content.RouteData

internal class LiveRideReplanRoutingTask(routeType: String,
                                         speed: Int,
                                         context: Context) : CycleStreetsRoutingTask(routeType, speed, context) {
    override fun onPostExecute(route: RouteData?) {
        super.onPostExecute(route)
        if (route != null)
            Route.waypoints().firstWaypointEphemeral = true
    }
}
