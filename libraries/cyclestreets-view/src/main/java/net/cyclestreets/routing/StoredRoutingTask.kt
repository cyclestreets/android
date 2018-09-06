package net.cyclestreets.routing

import android.content.Context

import net.cyclestreets.content.RouteData
import net.cyclestreets.content.RouteDatabase
import net.cyclestreets.view.R

internal class StoredRoutingTask (private val db: RouteDatabase,
                                  context: Context) : RoutingTask<Int>(R.string.route_loading, context) {
    override fun doInBackground(vararg params: Int?): RouteData {
        return db.route(params[0]!!)
    }
}
