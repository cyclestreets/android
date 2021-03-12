package net.cyclestreets.routing

import android.content.Context

import net.cyclestreets.content.RouteData
import net.cyclestreets.view.R

internal open class CycleStreetsRoutingTask(private val routeType: String,
                                            private val speed: Int,
                                            context: Context,
                                            private val distance: Int? = null,
                                            private val duration: Int? = null,
                                            private val pois: String? = null) : RoutingTask<Waypoints>(R.string.route_finding_new, context) {
    override fun doInBackground(vararg waypoints: Waypoints): RouteData? {
        val wp = waypoints[0]
        return fetchRoute(routeType,
                          speed = speed,
                          waypoints = wp,
                          distance = distance,
                          duration = duration,
                          pois = pois)
    }
}
