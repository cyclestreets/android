package net.cyclestreets.routing

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import net.cyclestreets.RoutePlans.PLAN_LEISURE
import net.cyclestreets.api.JourneyPlanner
import net.cyclestreets.content.RouteData
import net.cyclestreets.util.Dialog
import net.cyclestreets.util.Logging
import net.cyclestreets.util.ProgressDialog
import net.cyclestreets.view.R

abstract class RoutingTask<Params> protected constructor(private val initialMsg: String,
                                                         private val context: Context) : AsyncTask<Params, Int, RouteData>() {
    private val objectMapper: ObjectMapper = ObjectMapper()
    private var progress: ProgressDialog? = null
    private var error: String? = null
    private val TAG = Logging.getTag(RoutingTask::class.java)
    private val NO_ITINERARY = -1L

    protected constructor(progressMessageId: Int,
                          context: Context) : this(context.getString(progressMessageId), context)

    protected fun fetchRoute(routeType: String,
                             itinerary: Long = NO_ITINERARY,
                             speed: Int,
                             waypoints: Waypoints? = null,
                             distance: Int? = null,
                             duration: Int? = null,
                             poiTypes: String? = null,
                             saveRoute: Boolean = true): RouteData? {
        return try {
            val json = doFetchRoute(routeType, itinerary, speed, waypoints, distance, duration, poiTypes)

            when {
                (json == "null") -> {
                    error = context.getString(R.string.route_not_found)
                    throw ErrorFromServerException(error!!)
                }
                containsError(json) -> {
                    // Show the error message returned by the server
                    throw ErrorFromServerException(error!!)
                }
                else ->
                    RouteData(json, waypoints, null, saveRoute)
            }
        } catch (e: Exception) {
            if (error == null) {
                error = context.getString(R.string.could_not_contact_server) + " " + e.message
            }
            Log.w(TAG, error, e)
            null
        }
    }

    private fun doFetchRoute(routeType: String,
                             itinerary: Long,
                             speed: Int,
                             waypoints: Waypoints?,
                             distance: Int?,
                             duration: Int?,
                             poiTypes: String?): String {
        return when {
            itinerary != NO_ITINERARY -> getRoutebyItineraryNo(routeType, itinerary)
            routeType == PLAN_LEISURE -> JourneyPlanner.getCircularJourneyJson(waypoints, distance, duration, poiTypes)
            else -> JourneyPlanner.getJourneyJson(routeType, speed, waypoints!!)
        }
    }

    private fun getRoutebyItineraryNo(routeType: String, itinerary: Long): String {
        val json = JourneyPlanner.retrievePreviousJourneyJson(routeType, itinerary)
        return if (json != "null" && !containsError(json))
            json
        else {
            // In normal circumstance, this branch should only be hit on retrieving a route by ID,
            // where type is not known.
            // Clear the error, and try again to see if there is a leisure (circular) route with this number
            error = null
            JourneyPlanner.retrievePreviousJourneyJson(PLAN_LEISURE, itinerary)
        }
    }

    private fun containsError(json: String): Boolean {
        val jsonNode = objectMapper.readTree(json)
        error = jsonNode.get("Error")?.asText(context.getString(R.string.route_not_found))
        return (error != null)
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

class ErrorFromServerException(errorMessage: String): Exception(errorMessage)
