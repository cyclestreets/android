package net.cyclestreets.views.overlay

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Canvas
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.FeedbackActivity
import net.cyclestreets.RoutePlans
import net.cyclestreets.Undoable
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.Logging
import net.cyclestreets.util.MenuHelper.createMenuItem
import net.cyclestreets.util.MenuHelper.showMenuItem
import net.cyclestreets.util.MessageBox
import net.cyclestreets.util.Share
import net.cyclestreets.util.Theme
import net.cyclestreets.util.Theme.lowlightColor
import net.cyclestreets.util.Theme.lowlightColorInverse
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay


class TapToRouteOverlay(private val mapView: CycleMapView) : Overlay(), TapListener, ContextMenuListener,
                                                             Undoable, PauseResumeListener, Route.Listener {

    private val routingInfoRect: Button
    private val routeNowIcon: ImageView
    private val restartButton: FloatingActionButton

    private val context = mapView.context

    private val shareIcon = materialIcon(context, GoogleMaterial.Icon.gmd_share, lowlightColorInverse(context))
    private val commentIcon = materialIcon(context, GoogleMaterial.Icon.gmd_comment, lowlightColorInverse(context))
    private val changeRouteTypeIcon = materialIcon(context, GoogleMaterial.Icon.gmd_arrow_drop_down_circle, lowlightColorInverse(context))

    private val highlightColour = Theme.highlightColor(context) or 0xFF000000.toInt()
    private val lowlightColour = Theme.lowlightColor(context) or 0xFF000000.toInt()

    private val waymarks = WaymarkOverlay(mapView)
    private val controller = OverlayHelper(mapView).controller()

    private var tapState = TapToRoute.WAITING_FOR_START

    init {
        mapView.overlayPushTop(waymarks)

        // The view is shared, and has already been added by the RouteHighlightOverlay.
        // So find that, and don't inflate a second copy.
        val routeView = mapView.findViewById<View>(R.id.route_view)

        routingInfoRect = routeView.findViewById(R.id.routing_info_rect)
        routingInfoRect.setOnClickListener { _ -> onRouteNow(waypoints()) }

        restartButton = routeView.findViewById<FloatingActionButton>(R.id.restartbutton).apply {
            setImageDrawable(materialIcon(context, GoogleMaterial.Icon.gmd_replay, lowlightColor(context!!)))
            setOnClickListener { _ -> tapRestart() }
        }

        routeNowIcon = routeView.findViewById(R.id.route_now_icon)
    }

    private fun setRoute(noJourney: Boolean, waypointCount: Int) {
        controller.flushUndo(this)
        if (noJourney) {
            tapState = TapToRoute.fromCount(waypointCount)
            for (i in 1..waypointCount) controller.pushUndo(this)
        } else
            tapState = TapToRoute.ALL_DONE
    }

    private fun resetRoute() {
        tapState = TapToRoute.WAITING_FOR_START
        controller.flushUndo(this)
    }

    private fun onRouteNow(waypoints: Waypoints) {
        Route.PlotRoute(CycleStreetsPreferences.routeType(),
                CycleStreetsPreferences.speed(),
                context,
                waypoints)
    }

    ////////////////////////////////////////////
    fun waypoints(): Waypoints {
        return waymarks.waypoints()
    }

    private fun waypointsCount(): Int {
        return waymarks.waymarkersCount()
    }

    ////////////////////////////////////////////
    override fun onCreateOptionsMenu(menu: Menu) {
        createMenuItem(menu, R.string.route_menu_change, Menu.FIRST, changeRouteTypeIcon)
        createMenuItem(menu, R.string.route_menu_change_share, Menu.NONE, shareIcon)
        createMenuItem(menu, R.string.route_menu_change_comment, Menu.NONE, commentIcon)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        showMenuItem(menu, R.string.route_menu_change, tapState.routeIsPlanned())
        showMenuItem(menu, R.string.route_menu_change_share, tapState.routeIsPlanned())
        showMenuItem(menu, R.string.route_menu_change_comment, tapState.routeIsPlanned())
    }

    override fun onCreateContextMenu(menu: ContextMenu) {
        if (!tapState.routeIsPlanned())
            return

        val currentPlan = Route.journey().plan()
        REPLAN_MENU_IDS
                .filter { id -> currentPlan != REPLAN_MENU_PLANS[id] }
                .forEach { id -> createMenuItem(menu, id)}

        if (mapView.isMyLocationEnabled)
            createMenuItem(menu, R.string.route_menu_change_reroute_from_here)

        createMenuItem(menu, R.string.route_menu_change_reverse)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val menuId = item.itemId

        when (menuId) {
            R.string.route_menu_change ->
                mapView.showContextMenu()
            R.string.route_menu_change_reroute_from_here ->
                mapView.lastFix.apply {
                    if (this == null)
                        Toast.makeText(context, R.string.route_no_location, Toast.LENGTH_LONG).show()
                    else
                        onRouteNow(Waypoints.fromTo(GeoPoint(latitude, longitude), waymarks.finish()))
                }
            R.string.route_menu_change_reverse ->
                onRouteNow(waypoints().reversed())
            R.string.route_menu_change_share ->
                Share.Url(mapView,
                          Route.journey().url(),
                          Route.journey().name(),
                          "CycleStreets journey")
            R.string.route_menu_change_comment ->
                context.startActivity(Intent(context, FeedbackActivity::class.java))
            else ->
                if (REPLAN_MENU_PLANS.containsKey(menuId))
                    Route.RePlotRoute(REPLAN_MENU_PLANS[menuId]!!, context)
                else
                    return false
        }

        return true // we handled it!
    }

    ////////////////////////////////////////////
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        drawRoutingInfoRect()
        drawRestartButton()
    }

    private fun drawRestartButton() {
        if (tapState.routeIsPlanned()) {
            restartButton.show()
        } else {
            restartButton.hide()
        }
    }

    private fun drawRoutingInfoRect() {
        if (tapState.routeIsPlanned()) {
            // In this case, populating the routing info is done by the RouteHighlightOverlay
            return
        }

        routeNowIcon.visibility = if (tapState.canRoute()) View.VISIBLE else View.INVISIBLE

        routingInfoRect.apply {
            setBackgroundColor(if (tapState.canRoute()) highlightColour else lowlightColour)
            gravity = Gravity.CENTER
            text = tapState.actionDescription
            isEnabled = tapState.canRoute()
        }
    }

    //////////////////////////////////////////////
    override fun onSingleTap(event: MotionEvent): Boolean {
        return tapMarker(event)
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        return false
    }

    private fun tapRestart(): Boolean {
        if (!CycleStreetsPreferences.confirmNewRoute())
            return stepBack(true)

        MessageBox.YesNo(mapView, "Start a new route?") { _, _ -> stepBack(true) }

        return true
    }

    override fun onBackPressed(): Boolean {
        return stepBack(false)
    }

    private fun stepBack(tap: Boolean): Boolean {
        if (!tap && !tapState.waypointingInProgress)
            return false

        when (tapState) {
            TapToRoute.WAITING_FOR_START -> return true
            TapToRoute.WAITING_TO_ROUTE,
            TapToRoute.WAITING_FOR_SECOND,
            TapToRoute.WAITING_FOR_NEXT -> waymarks.removeWaypoint()
            TapToRoute.ALL_DONE -> Route.resetJourney()
        }

        tapState = tapState.previous(waypointsCount())
        mapView.postInvalidate()

        return true
    }

    private fun tapMarker(event: MotionEvent): Boolean {
        val p = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt())
        tapAction(p)
        return true
    }

    fun setNextMarker(point: IGeoPoint) {
        tapAction(point)
    }

    private fun tapAction(point: IGeoPoint) {
        if (tapState.noFurtherWaypoints()) {
            return
        }

        waymarks.addWaypoint(point)
        controller.pushUndo(this)
        tapState = tapState.next(waypointsCount())
        mapView.invalidate()
    }

    ////////////////////////////////////
    private enum class TapToRoute private constructor(val waypointingInProgress: Boolean, val actionDescription: String) {
        WAITING_FOR_START(false, "Tap map to set Start"),
        WAITING_FOR_SECOND(true, "Tap map to set Waypoint"),
        WAITING_FOR_NEXT(true, "Tap map to set Waypoint\nTap here to Route"),
        WAITING_TO_ROUTE(true, "Tap here to Route"),
        ALL_DONE(false, "");

        fun previous(count: Int): TapToRoute {
            val previous: TapToRoute
            when (this) {
                WAITING_FOR_START, WAITING_FOR_SECOND, ALL_DONE -> previous = WAITING_FOR_START
                WAITING_FOR_NEXT -> previous = if (count == 1) WAITING_FOR_SECOND else WAITING_FOR_NEXT
                WAITING_TO_ROUTE -> previous = WAITING_FOR_NEXT
            }
            Log.d(TAG, "Moving to previous TapToRoute state=${previous.name} with waypoints=$count")
            return previous
        }

        fun next(count: Int): TapToRoute {
            val next: TapToRoute
            when (this) {
                WAITING_FOR_START -> next = WAITING_FOR_SECOND
                WAITING_FOR_SECOND -> next = WAITING_FOR_NEXT
                WAITING_FOR_NEXT -> next = if (count == MAX_WAYPOINTS) WAITING_TO_ROUTE else WAITING_FOR_NEXT
                WAITING_TO_ROUTE, ALL_DONE -> next = ALL_DONE
            }
            Log.d(TAG, "Moving to next TapToRoute state=${next.name} with waypoints=$count")
            return next
        }

        fun canRoute(): Boolean {
            return this == TapToRoute.WAITING_FOR_NEXT || this == TapToRoute.WAITING_TO_ROUTE
        }
        fun noFurtherWaypoints(): Boolean {
            return this == TapToRoute.WAITING_TO_ROUTE || this == TapToRoute.ALL_DONE
        }
        fun routeIsPlanned(): Boolean {
            return this == TapToRoute.ALL_DONE
        }

        companion object {
            fun fromCount(count: Int): TapToRoute {
                val next: TapToRoute
                when (count) {
                    0 -> next = WAITING_FOR_START
                    1 -> next = WAITING_FOR_SECOND
                    MAX_WAYPOINTS -> next = WAITING_TO_ROUTE
                    else -> next = WAITING_FOR_NEXT
                }
                Log.d(TAG, "Restoring to TapToRoute state=" + next.name + " with waypoints=" + count)
                return next
            }
        }
    }

    ////////////////////////////////////
    override fun onResume(prefs: SharedPreferences) {
        Route.registerListener(this)
    }

    override fun onPause(edit: Editor) {
        Route.unregisterListener(this)
    }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        setRoute(journey.isEmpty(), waypoints.count())
    }

    override fun onResetJourney() {
        resetRoute()
    }

    companion object {
        private val TAG = Logging.getTag(TapToRouteOverlay::class.java)
        private val REPLAN_MENU_IDS = arrayOf(
            R.string.route_menu_change_replan_quietest,
            R.string.route_menu_change_replan_balanced,
            R.string.route_menu_change_replan_fastest,
            R.string.route_menu_change_replan_shortest
        )
        private val REPLAN_MENU_PLANS = mapOf(
            R.string.route_menu_change_replan_quietest to RoutePlans.PLAN_QUIETEST,
            R.string.route_menu_change_replan_balanced to RoutePlans.PLAN_BALANCED,
            R.string.route_menu_change_replan_fastest to RoutePlans.PLAN_FASTEST,
            R.string.route_menu_change_replan_shortest to RoutePlans.PLAN_SHORTEST
        )
        private const val MAX_WAYPOINTS = 30
    }
}
