package net.cyclestreets.routing

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.RoutePlans
import net.cyclestreets.content.RouteData
import net.cyclestreets.content.RouteDatabase
import net.cyclestreets.content.RouteSummary
import net.cyclestreets.routing.Journey.Companion.NULL_JOURNEY
import net.cyclestreets.routing.Journey.Companion.loadFromJson
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R
import net.cyclestreets.views.overlay.RouteOverlay
import org.osmdroid.views.overlay.OverlayItem
import java.util.*

object Route {
    @kotlin.jvm.JvmStatic
    lateinit var altRouteOverlay: RouteOverlay

    private val TAG = Logging.getTag(Route::class.java)
    private val listeners_ = Listeners()
    private var currentJourneyPlan: String = ""
    private var altRouteQuery: CycleStreetsRoutingTask? = null
    private var altJson: String = ""
    var altRouteWpCount: Int = 0

    @JvmStatic
    fun registerListener(l: Listener) {
        listeners_.register(l)
    }

    @JvmStatic
    fun softRegisterListener(l: Listener) {
        listeners_.softRegister(l)
    }

    @JvmStatic
    fun unregisterListener(l: Listener) {
        listeners_.unregister(l)
    }

    // Called when user has tapped waypoints on screen and then taps button to route,
    // or when button at top to plan a route by address tapped
    @JvmStatic
    fun PlotRoute(plan: String,
                  speed: Int,
                  context: Context,
                  waypoints: Waypoints) {
        clearAltRoute()
        val query = CycleStreetsRoutingTask(plan, speed, context)
        query.execute(waypoints)
    }

    @JvmStatic
    fun plotAltRoute(context: Context,
                     waypoints: Waypoints) {
        val speed = CycleStreetsPreferences.speed()
        // Cancel previous query, if there is one, as it no longer has any use now user has added another waypoint
        cancelPreviousQuery()

        altRouteQuery = CycleStreetsRoutingTask(currentJourneyPlan, speed, context, pAltRoute = true)
        altRouteQuery!!.execute(waypoints)
    }

    @JvmStatic
    private fun cancelPreviousQuery() {
        if (altRouteQuery != null) {
            val status = altRouteQuery!!.status
            Log.d(TAG, "altRouteQuery $status")
            if (status != AsyncTask.Status.FINISHED)
                Log.d(TAG, "Cancelling alt RoutingTask query")
            altRouteQuery!!.cancel(true)
        }
    }

    @JvmStatic
    fun plotCircularRoute(plan: String,
                          distance: Int?,
                          duration: Int?,
                          poiTypes: String?,
                          context: Context) {
        clearAltRoute()
        val query = CycleStreetsRoutingTask(plan, 0, context, distance, duration, poiTypes)
        query.execute(waypoints_)
    }

    fun LiveReplanRoute(speed: Int,
                        context: Context,
                        waypoints: Waypoints) {
        clearAltRoute()
        var newPlan: String
        // Check current plan is a linear route plan.
        if (currentJourneyPlan in RoutePlans.allPlans())
            newPlan = currentJourneyPlan
        else
            newPlan = RoutePlans.PLAN_QUIETEST

        val query = LiveRideReplanRoutingTask(newPlan, speed, context)
        query.execute(waypoints)
    }

    // Open route by number (could also be a route no which has come via sms)
    @JvmStatic
    fun FetchRoute(plan: String,
                   itinerary: Long,
                   speed: Int,
                   context: Context) {
        clearAltRoute()
        val query = FetchCycleStreetsRouteTask(plan, speed, context)
        query.execute(itinerary)
    }

    // Same route, different plan
    fun RePlotRoute(plan: String,
                    context: Context) {
        val query = ReplanRoutingTask(plan, db_, context)
        query.execute(plannedRoute_)
    }

    // Saved route
    @JvmStatic
    fun PlotStoredRoute(localId: Int,
                        context: Context) {
        clearAltRoute()
        val query = StoredRoutingTask(db_, context)
        query.execute(localId)
    }

    @JvmStatic
    fun RenameRoute(localId: Int, newName: String) {
        db_.renameRoute(localId, newName)
    }

    @JvmStatic
    fun DeleteRoute(localId: Int) {
        db_.deleteRoute(localId)
    }

    /////////////////////////////////////////
    private var plannedRoute_ = NULL_JOURNEY
    private var altRoute = NULL_JOURNEY
    private var waypoints_ = plannedRoute_.waypoints
    private lateinit var db_: RouteDatabase
    private lateinit var context_: Context
    private var waymarksListAtPause = mutableListOf<OverlayItem>()

    @JvmStatic
    fun initialise(context: Context) {
        context_ = context
        db_ = RouteDatabase(context)
        if (isLoaded)
            loadLastJourney()
        else
            restoreWaypoints()
    }

    fun resetJourney(clearWaypoints: Boolean) {
        onNewJourney(null, clearWaypoints)
    }

    fun onPause(waypoints: Waypoints) {
        waypoints_ = waypoints
        if (!isLoaded) {
            stashWaypoints()
        }
    }

    fun onResume() {
        Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units())
    }

    /////////////////////////////////////
    fun storedCount(): Int {
        return db_.routeCount()
    }

    @JvmStatic
    fun storedRoutes(): List<RouteSummary> {
        return db_.savedRoutes()
    }

    /////////////////////////////////////
    fun onNewJourney(route: RouteData?, clearWaypoints: Boolean = true): Boolean {
        try {
            doOnNewJourney(route, clearWaypoints)
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Route finding failed", e)
            Toast.makeText(context_, R.string.route_finding_failed, Toast.LENGTH_LONG).show()
        }
        return false
    }

    private fun doOnNewJourney(route: RouteData?, clearWaypoints: Boolean) {
        // Null route is passed when user requests new route
        if (route == null) {
            plannedRoute_ = NULL_JOURNEY
            altRoute = NULL_JOURNEY
            if (clearWaypoints)
                waypoints_ = NULL_WAYPOINTS
            listeners_.onReset()
            clearRouteLoaded()
            return
        }
        plannedRoute_ = loadFromJson(route.json(), route.points(), route.name(), context_)
        currentJourneyPlan = plannedRoute_.plan()
        if (route.saveRoute())
            db_.saveRoute(plannedRoute_, route.json())
        waypoints_ = plannedRoute_.waypoints
        listeners_.onNewJourney(plannedRoute_, waypoints_)
        setRouteLoaded()
    }

    fun doOnNewAltJourney(route: RouteData?) {
        try {
            if (route != null) {
                altJson = route.json()
                altRoute = loadFromJson(altJson, route.points(), route.name(), context_)
            }
            altRouteOverlay.setRoute(altRoute.segments)
        } catch (e: Exception) {
            Log.w(TAG, "Alternative route finding failed", e)
            Toast.makeText(context_, R.string.route_finding_failed, Toast.LENGTH_LONG).show()
        }
    }

    fun acceptAltRoute(waypoints: Waypoints): Boolean {
        if (altJson != "") {
            // Passing null for the waypoints means that the waypoints from the json will be loaded to altRoute,
            //  which allows us to check the number of waypoints in the latest alt route retrieval
            altRoute = loadFromJson(altJson, null, null, context_)
        }
        //  It's possible the latest alt route retrieval may have failed, in which case don't accept the alt route.
        if ((altJson == "") || (altRoute.waypoints.count() < waypoints.count())) {
            Toast.makeText(context_, R.string.alt_route_finding_failed, Toast.LENGTH_LONG).show()
            return false
        }

        plannedRoute_ = altRoute
        db_.saveRoute(plannedRoute_, altJson)
        altRoute = NULL_JOURNEY
        altJson = ""
        altRouteWpCount = 0
        waypoints_ = plannedRoute_.waypoints
        listeners_.onNewJourney(plannedRoute_, waypoints_)
        return true
    }

    fun clearAltRoute() {
        cancelPreviousQuery()
        altRoute = NULL_JOURNEY
        altJson = ""
        altRouteWpCount = 0
        try {
            altRouteOverlay.onResetJourney()
        }
        catch(e: Exception) {
            // This will error if called from test - no need to do anything
        }
    }

    fun waypoints(): Waypoints {
        return waypoints_
    }

    fun saveAltWaymarks(waymarks: MutableList<OverlayItem>) {
        waymarksListAtPause = waymarks.toMutableList()
    }

    fun restoreAltWaymarks(): MutableList<OverlayItem> {
        return waymarksListAtPause
    }

    fun clearAltWaymarks() {
        waymarksListAtPause.clear()
    }

    @JvmStatic
    fun routeAvailable(): Boolean {
        return plannedRoute_ != NULL_JOURNEY
    }

    @JvmStatic
    fun journey(): Journey {
        return plannedRoute_
    }

    @JvmStatic
    fun altRouteInProgress(): Boolean {
        return altRouteWpCount != 0
    }

    @JvmStatic
    fun currentJourneyPlan(): String {
        return currentJourneyPlan
    }

    private fun loadLastJourney() {
        val routeSummaries = storedRoutes()
        if (!storedRoutes().isEmpty()) {
            val lastRoute = routeSummaries[0]
            val route = db_.route(lastRoute.localId())
            onNewJourney(route)
        }
    }

    private fun setRouteLoaded() {
        prefs()
                .edit()
                .putBoolean(routeLoadedPref, true)
                .remove(waypointsInProgressPref)
                .apply()
    }

    private fun clearRouteLoaded() {
        prefs()
                .edit()
                .remove(routeLoadedPref)
                .apply()
    }

    private fun stashWaypoints() {
        val stash = RouteDatabase.serializeWaypoints(waypoints_)
        prefs()
                .edit()
                .putString(waypointsInProgressPref, stash)
                .apply()
    }

    private fun restoreWaypoints() {
        val stash = prefs().getString(waypointsInProgressPref, "")
        if (stash != null && stash.isNotEmpty())
            waypoints_ = Waypoints(RouteDatabase.deserializeWaypoints(stash))
    }

    @JvmStatic
    fun reloadAltRoute() {
        if (altRouteWpCount > 0) {
            if (altJson != "") {
                // Passing null for the waypoints means that the waypoints from the json will be loaded to altRoute,
                //  which may not be (quite) the same as those tapped on the screen
                // (the ones in the json may be in a slightly different location)
                altRoute = loadFromJson(altJson, null, null, context_)
            }
            //  It's possible the app could have been paused before latest alt route was retrieved.
            //  In that case, retrieve the route, but only if a task isn't still running
            //  (there's no need to do plotAltRoute at all if there is a task running)
            if ((altJson == "") || (altRoute.waypoints.count() < waypoints_.count())) {
                if ((altRouteQuery != null) && (altRouteQuery!!.status == AsyncTask.Status.FINISHED)) {
                    plotAltRoute(context_, waypoints_)
                }
            }
            else
                altRouteOverlay.setRoute(altRoute.segments)
        }
    }

    private val isLoaded: Boolean
        get() = prefs().getBoolean(routeLoadedPref, false)

    private const val routeLoadedPref = "route"
    private const val waypointsInProgressPref = "waypoints-in-progress"
    private fun prefs(): SharedPreferences {
        return context_.getSharedPreferences("net.cyclestreets.CycleStreets", Context.MODE_PRIVATE)
    }

    interface Listener {
        fun onNewJourney(journey: Journey, waypoints: Waypoints)
        fun onResetJourney()
    }

    private class Listeners {
        private val listeners_: MutableList<Listener> = ArrayList()

        fun register(listener: Listener) {
            if (!doRegister(listener)) return
            if (journey() != NULL_JOURNEY || waypoints() != NULL_WAYPOINTS)
                listener.onNewJourney(journey(), waypoints())
            else
                listener.onResetJourney()
        }

        fun softRegister(listener: Listener) {
            doRegister(listener)
        }

        private fun doRegister(listener: Listener): Boolean {
            if (listeners_.contains(listener)) return false

            listeners_.add(listener)
            return true
        }

        fun unregister(listener: Listener) {
            listeners_.remove(listener)
        }

        fun onNewJourney(journey: Journey, waypoints: Waypoints) {
            listeners_.forEach { it.onNewJourney(journey, waypoints) }
        }

        fun onReset() {
            listeners_.forEach { it.onResetJourney() }
        }
    }
}