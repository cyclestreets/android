package net.cyclestreets.views.overlay

import java.util.HashMap

import net.cyclestreets.RoutePlans

import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.FeedbackActivity
import net.cyclestreets.util.Logging
import net.cyclestreets.util.Theme
import net.cyclestreets.view.R
import net.cyclestreets.Undoable
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.MessageBox
import net.cyclestreets.util.Share
import net.cyclestreets.views.CycleMapView

import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.ContextMenu
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable

import net.cyclestreets.util.MenuHelper.createMenuItem
import net.cyclestreets.util.MenuHelper.showMenuItem

class TapToRouteOverlay(private val mapView: CycleMapView) : Overlay(), TapListener, ContextMenuListener, Undoable, PauseResumeListener, Route.Listener {

    private val routingInfoRect: Button
    private val routeNowIcon: ImageView
    private val restartButton: FloatingActionButton

    private val context: Context

    private val shareDrawable: Drawable
    private val commentDrawable: Drawable

    private val highlightColour: Int
    private val lowlightColour: Int

    private val waymarks = WaymarkOverlay(mapView)
    private val controller = OverlayHelper(mapView).controller()

    private var tapState = TapToRoute.WAITING_FOR_START

    init {
        mapView.overlayPushTop(waymarks)

        context = mapView.context

        shareDrawable = IconicsDrawable(context)
                .icon(GoogleMaterial.Icon.gmd_share)
                .color(Theme.lowlightColorInverse(context))
                .sizeDp(24)
        commentDrawable = IconicsDrawable(context)
                .icon(GoogleMaterial.Icon.gmd_comment)
                .color(Theme.lowlightColorInverse(context))
                .sizeDp(24)

        // The view is shared, and has already been added by the RouteHighlightOverlay.
        // So find that, and don't inflate a second copy.
        val routeView = mapView.findViewById<View>(R.id.route_view)

        routingInfoRect = routeView.findViewById(R.id.routing_info_rect)
        routingInfoRect.setOnClickListener { _ -> onRouteNow(waypoints()) }

        restartButton = routeView.findViewById(R.id.restartbutton)
        restartButton.setOnClickListener { _ -> tapRestart() }

        routeNowIcon = routeView.findViewById(R.id.route_now_icon)

        lowlightColour = Theme.lowlightColor(context) or -0x1000000
        highlightColour = Theme.highlightColor(context) or -0x1000000
    }

    private fun setRoute(empty: Boolean) {
        tapState = if (empty) TapToRoute.WAITING_FOR_START else TapToRoute.ALL_DONE
        controller.flushUndo(this)
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
        createMenuItem(menu, R.string.route_menu_change, Menu.FIRST, R.drawable.ic_menu_more)
        createMenuItem(menu, R.string.route_menu_change_share, Menu.NONE, shareDrawable)
        createMenuItem(menu, R.string.route_menu_change_comment, Menu.NONE, commentDrawable)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        showMenuItem(menu, R.string.route_menu_change, tapState == TapToRoute.ALL_DONE)
        showMenuItem(menu, R.string.route_menu_change_share, tapState == TapToRoute.ALL_DONE)
        showMenuItem(menu, R.string.route_menu_change_comment, tapState == TapToRoute.ALL_DONE)
    }

    override fun onCreateContextMenu(menu: ContextMenu) {
        if (tapState != TapToRoute.ALL_DONE)
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

        if (menuId == R.string.route_menu_change) {
            mapView.showContextMenu()
            return true
        }

        if (REPLAN_MENU_PLANS.containsKey(menuId)) {
            Route.RePlotRoute(REPLAN_MENU_PLANS[menuId], context)
            return true
        }

        if (R.string.route_menu_change_reroute_from_here == menuId) {
            val lastFix = mapView.lastFix
            if (lastFix == null) {
                Toast.makeText(mapView.context, R.string.route_no_location, Toast.LENGTH_LONG).show()
                return true
            }

            val from = GeoPoint(lastFix.latitude, lastFix.longitude)
            onRouteNow(Waypoints.fromTo(from, waymarks.finish()))
        }
        if (R.string.route_menu_change_reverse == menuId) {
            onRouteNow(waypoints().reversed())
            return true
        }
        if (R.string.route_menu_change_share == menuId) {
            Share.Url(mapView,
                    Route.journey().url(),
                    Route.journey().name(),
                    "CycleStreets journey")
            return true
        }
        if (R.string.route_menu_change_comment == menuId) {
            val context = mapView.context
            context.startActivity(Intent(context, FeedbackActivity::class.java))
            return true
        }

        return false
    }

    ////////////////////////////////////////////
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        drawRoutingInfoRect()
        drawRestartButton()
    }

    private fun drawRestartButton() {
        if (tapState == TapToRoute.ALL_DONE) {
            restartButton.show()
        } else {
            restartButton.hide()
        }
    }

    private fun drawRoutingInfoRect() {
        if (tapState == TapToRoute.ALL_DONE) {
            // In this case, populating the routing info is done by the RouteHighlightOverlay
            return
        }

        routeNowIcon.visibility = if (tapState == TapToRoute.WAITING_FOR_NEXT || tapState == TapToRoute.WAITING_TO_ROUTE)
            View.VISIBLE
        else
            View.INVISIBLE

        val bgColour = if (tapState == TapToRoute.WAITING_FOR_START || tapState == TapToRoute.WAITING_FOR_SECOND)
            lowlightColour
        else
            highlightColour
        routingInfoRect.apply {
            setBackgroundColor(bgColour)
            gravity = Gravity.CENTER
            text = tapState.actionDescription
            isEnabled = tapState == TapToRoute.WAITING_FOR_NEXT || tapState == TapToRoute.WAITING_TO_ROUTE
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

        MessageBox.YesNo(mapView,
                "Start a new route?"
        ) { _, _ -> stepBack(true) }

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
            TapToRoute.WAITING_TO_ROUTE, TapToRoute.WAITING_FOR_SECOND, TapToRoute.WAITING_FOR_NEXT -> waymarks.removeWaypoint()
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
        if (tapState == TapToRoute.WAITING_TO_ROUTE || tapState == TapToRoute.ALL_DONE) {
            // Any taps that hit the overlay shouldn't do anything - we can't accept more waypoints
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
            Log.d(TAG, "Moving to previous TapToRoute state=" + previous.name + " with waypoints=" + count)
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
            Log.d(TAG, "Moving to next TapToRoute state=" + next.name + " with waypoints=" + count)
            return next
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
        setRoute(journey.isEmpty())
    }

    override fun onResetJourney() {
        resetRoute()
    }

    companion object {
        private val TAG = Logging.getTag(TapToRouteOverlay::class.java)
        private val REPLAN_MENU_IDS = intArrayOf(R.string.route_menu_change_replan_quietest, R.string.route_menu_change_replan_balanced, R.string.route_menu_change_replan_fastest, R.string.route_menu_change_replan_shortest)
        private val REPLAN_MENU_PLANS = object : HashMap<Int, String>() {
            init {
                put(R.string.route_menu_change_replan_quietest, RoutePlans.PLAN_QUIETEST)
                put(R.string.route_menu_change_replan_balanced, RoutePlans.PLAN_BALANCED)
                put(R.string.route_menu_change_replan_fastest, RoutePlans.PLAN_FASTEST)
                put(R.string.route_menu_change_replan_shortest, RoutePlans.PLAN_SHORTEST)
            }
        }
        private const val MAX_WAYPOINTS = 30
    }
}
