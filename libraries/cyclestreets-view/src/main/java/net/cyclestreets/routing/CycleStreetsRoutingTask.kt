package net.cyclestreets.routing

import android.content.Context

import net.cyclestreets.content.RouteData
import net.cyclestreets.view.R

internal open class CycleStreetsRoutingTask(private val routeType: String,
                                            private val speed: Int,
                                            context: Context) : RoutingTask<Waypoints>(R.string.route_finding_new, context) {
    override fun doInBackground(vararg waypoints: Waypoints): RouteData? {
        val wp = waypoints[0]
        return fetchRoute(routeType, speed, wp)
    }
}
