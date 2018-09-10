package net.cyclestreets.routing

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast

import net.cyclestreets.api.JourneyPlanner
import net.cyclestreets.content.RouteData
import net.cyclestreets.util.Dialog
import net.cyclestreets.util.ProgressDialog

abstract class RoutingTask<Params> protected constructor(private val initialMsg: String,
                                                         private val context: Context) : AsyncTask<Params, Int, RouteData>() {
    private var progress: ProgressDialog? = null
    private var error: String? = null

    protected constructor(progressMessageId: Int,
                          context: Context) : this(context.getString(progressMessageId), context)

    protected fun fetchRoute(routeType: String,
                             speed: Int,
                             waypoints: Waypoints): RouteData? {
        return fetchRoute(routeType, -1, speed, waypoints)
    }

    @JvmOverloads
    protected fun fetchRoute(routeType: String,
                             itinerary: Long,
                             speed: Int,
                             waypoints: Waypoints? = null): RouteData? {
        return try {
            val json = doFetchRoute(routeType, itinerary, speed, waypoints)
            RouteData(json, waypoints, null)
        } catch (e: Exception) {
            error = "Could not contact CycleStreets.net : " + e.message
            null
        }
    }

    private fun doFetchRoute(routeType: String,
                             itinerary: Long,
                             speed: Int,
                             waypoints: Waypoints?): String {
        return if (itinerary != -1L)
            JourneyPlanner.getJourneyJson(routeType, itinerary)
        else
            JourneyPlanner.getJourneyJson(routeType, speed, waypoints!!)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        try {
            progress = Dialog.createProgressDialog(context, initialMsg)
            progress!!.show()
        } catch (e: Exception) {
            progress = null
        }
    }

    override fun onProgressUpdate(vararg p: Int?) {
        progress?.setMessage(context.getString(p[0]!!))
    }

    override fun onPostExecute(route: RouteData?) {
        if (route != null)
            Route.onNewJourney(route)
        progressDismiss()
        if (error != null)
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
    }

    private fun progressDismiss() {
        try {
            // some devices, in rare situations, can throw here so just catch and swallow
            progress?.dismiss()
        } catch (e: Exception) {}
    }
}
