package net.cyclestreets.routing

import android.content.Context

import net.cyclestreets.content.RouteData
import net.cyclestreets.view.R

internal class FetchCycleStreetsRouteTask(private val routeType: String,
                                          private val speed: Int,
                                          context: Context) : RoutingTask<Long>(R.string.route_fetching_existing, context) {
    override fun doInBackground(vararg params: Long?): RouteData? {
        return fetchRoute(routeType, params[0]!!, speed)
    }
}
