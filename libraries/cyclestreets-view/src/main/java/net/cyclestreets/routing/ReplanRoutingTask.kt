package net.cyclestreets.routing

import android.content.Context

import net.cyclestreets.content.RouteData
import net.cyclestreets.content.RouteDatabase
import net.cyclestreets.view.R

internal class ReplanRoutingTask(private val newPlan: String,
                                 private val db: RouteDatabase,
                                 context: Context) : RoutingTask<Journey>(R.string.route_loading, context) {
    override fun doInBackground(vararg params: Journey): RouteData? {
        val pr = params[0]
        val rd = db.route(pr.itinerary(), newPlan)
        if (rd != null)
            return rd

        publishProgress(R.string.route_finding_new)
        return fetchRoute(newPlan, pr.itinerary().toLong(), 0, pr.waypoints)
    }
}
