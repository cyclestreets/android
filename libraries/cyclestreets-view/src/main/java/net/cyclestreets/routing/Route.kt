package net.cyclestreets.routing

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.content.RouteData
import net.cyclestreets.content.RouteDatabase
import net.cyclestreets.content.RouteSummary
import net.cyclestreets.routing.Journey.Companion.NULL_JOURNEY
import net.cyclestreets.routing.Journey.Companion.loadFromJson
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R
import java.util.*

object Route {
    private val TAG = Logging.getTag(Route::class.java)
    private val listeners_ = Listeners()
    @JvmStatic
    fun registerListener(l: Listener) {
        listeners_.register(l)
    }

    fun softRegisterListener(l: Listener) {
        listeners_.softRegister(l)
    }

    @JvmStatic
    fun unregisterListener(l: Listener) {
        listeners_.unregister(l)
    }

    @JvmStatic
    fun PlotRoute(plan: String,
                  speed: Int,
                  context: Context,
                  waypoints: Waypoints) {
        val query = CycleStreetsRoutingTask(plan, speed, context)
        query.execute(waypoints)
    }

    fun LiveReplanRoute(plan: String,
                        speed: Int,
                        context: Context,
                        waypoints: Waypoints) {
        val query = LiveRideReplanRoutingTask(plan, speed, context)
        query.execute(waypoints)
    }

    @JvmStatic
    fun FetchRoute(plan: String,
                   itinerary: Long,
                   speed: Int,
                   context: Context) {
        val query = FetchCycleStreetsRouteTask(plan, speed, context)
        query.execute(itinerary)
    }

    fun RePlotRoute(plan: String,
                    context: Context) {
        val query = ReplanRoutingTask(plan, db_, context)
        query.execute(plannedRoute_)
    }

    @JvmStatic
    fun PlotStoredRoute(localId: Int,
                        context: Context) {
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
    private var waypoints_ = plannedRoute_.waypoints
    private lateinit var db_: RouteDatabase
    private lateinit var context_: Context
    @JvmStatic
    fun initialise(context: Context) {
        context_ = context
        db_ = RouteDatabase(context)
        if (isLoaded)
            loadLastJourney()
        else
            restoreWaypoints()
    }

    fun resetJourney() {
        onNewJourney(null)
    }

    fun onPause(waypoints: Waypoints) {
        if (!isLoaded) {
            waypoints_ = waypoints
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
    fun onNewJourney(route: RouteData?): Boolean {
        try {
            doOnNewJourney(route)
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Route finding failed", e)
            Toast.makeText(context_, R.string.route_finding_failed, Toast.LENGTH_LONG).show()
        }
        return false
    }

    private fun doOnNewJourney(route: RouteData?) {
        if (route == null) {
            plannedRoute_ = NULL_JOURNEY
            waypoints_ = NULL_WAYPOINTS
            listeners_.onReset()
            clearRouteLoaded()
            return
        }
        plannedRoute_ = loadFromJson(route.json(), route.points(), route.name())
        db_.saveRoute(plannedRoute_, route.json())
        waypoints_ = plannedRoute_.waypoints
        listeners_.onNewJourney(plannedRoute_, waypoints_)
        setRouteLoaded()
    }

    fun waypoints(): Waypoints {
        return waypoints_
    }

    @JvmStatic
    fun routeAvailable(): Boolean {
        return plannedRoute_ != NULL_JOURNEY
    }

    @JvmStatic
    fun journey(): Journey {
        return plannedRoute_
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
        if (stash != null && stash.isNotEmpty()) {
            waypoints_ = Waypoints(RouteDatabase.deserializeWaypoints(stash))
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