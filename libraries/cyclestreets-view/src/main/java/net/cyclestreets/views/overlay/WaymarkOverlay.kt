package net.cyclestreets.views.overlay

import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.Dialog
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.MapView
import android.graphics.Canvas
import net.cyclestreets.util.Brush

import java.util.ArrayList

private val TAG = Logging.getTag(WaymarkOverlay::class.java)

class WaymarkOverlay(private val mapView: CycleMapView, private val ttrOverlay: TapToRouteOverlay? = null) :
            ItemizedOverlay<OverlayItem>(mapView.mapView(), ArrayList()),
            PauseResumeListener,
            Route.Listener,
            DialogInterface.OnClickListener {

    private val HORIZONTAL_TEXT_POSITION_ADJUSTMENT = 6.0f
    private val VERTICAL_TEXT_POSITION_ADJUSTMENT = 1.7f
    private val REDUCE_TEXT_SIZE = 0.8f
    private val REMOVE_WAYPOINT_OPTION = 0
    private val res = mapView.context.resources

    private val wispWpStart = makeWisp(R.drawable.wp_start_wisp)
    private val wispWpMid = makeWisp(R.drawable.wp_mid_wisp)
    private val wispWpFinish = makeWisp(R.drawable.wp_finish_wisp)

    private var activeItem: OverlayItem? = null
    private var activeItemIndex = 0
    private val wpStartInitial: String = res.getString(R.string.waypoint_start_initial)
    private val wpFinishInitial: String = res.getString(R.string.waypoint_finish_initial)
    private val startLabel = res.getString(R.string.waypoint_start)
    private val waypointLabel = res.getString(R.string.waypoint)
    private val finishLabel = res.getString(R.string.waypoint_finish)

    private fun makeWisp(drawable: Int) : Drawable? {
        return ResourcesCompat.getDrawable(mapView.context.resources, drawable, null)
    }

    //////////////////////////////////////
    fun waymarkersCount(): Int {
        return items().size
    }

    fun waypoints(): Waypoints {
        return Waypoints(items().map { wp -> wp.point })
    }

    fun finish(): IGeoPoint {
        return items().last().point
    }

    fun addWaypoint(point: IGeoPoint?) {
        if (point == null)
            return
        when (waymarkersCount()) {
            0 -> pushMarker(point, startLabel, wispWpStart)
            1 -> pushMarker(point, finishLabel, wispWpFinish)
            else -> {
                // Change current Finish waypoint to an intermediate one
                correctLabelAndIcon(
                    items().last().snippet,
                    waypointLabel,
                    items().lastIndex,
                    wispWpMid
                )
                pushMarker(point, finishLabel, wispWpFinish)
            }
        }
        Log.d(TAG, "Added waypoint $point")
    }

    fun removeWaypoint(index: Int) {
        // Shouldn't happen:
        if (waymarkersCount() == 0)
            return

        removeMarker(index)
        checkWaypoints()
    }

    private fun pushMarker(point: IGeoPoint, label: String, icon: Drawable?) {
        items().add(makeMarker(point, label, icon))
    }

    private fun removeMarker(index: Int) {
        items().removeAt(index)
    }

    private fun makeMarker(point: IGeoPoint, label: String, icon: Drawable?): OverlayItem {
        return OverlayItem(label, label, GeoPoint(point.latitude, point.longitude)).apply {
            setMarker(icon)
            markerHotspot = OverlayItem.HotspotPlace.BOTTOM_CENTER
        }
    }

    ////////////////////////////////////
    fun setWaypoints(waypoints: Waypoints) {
        resetWaypoints()

        waypoints.forEach { wp -> addWaypoint(wp) }
    }

    private fun resetWaypoints() {
        items().clear()
    }

    ////////////////////////////////////
    override fun onResume(prefs: SharedPreferences) {
        Route.registerListener(this)
    }

    override fun onPause(prefs: Editor) {
        Route.unregisterListener(this)
    }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        setWaypoints(waypoints)
    }

    override fun onResetJourney() {
        resetWaypoints()
    }

    // Waymark has already been scaled when added to List - no need to scale again
    override fun scale(mapView: MapView): Float {
        return 1.0F
    }

    override fun drawTextOnMarker(canvas: Canvas, rect: Rect, x: Int, y: Int, itemIndex: Int) {
        super.drawTextOnMarker(canvas, rect, x, y, itemIndex)

        val boldTextBrush = Brush.createBoldTextBrush((offset(mapView.getContext()) * REDUCE_TEXT_SIZE).toInt())
        val height = rect.height()
        val width = rect.width()

        canvas.drawText(
            waymarkNumber(itemIndex),
            x - width / HORIZONTAL_TEXT_POSITION_ADJUSTMENT,
            y - height / VERTICAL_TEXT_POSITION_ADJUSTMENT,
            boldTextBrush
        )
    }

    private fun waymarkNumber(index: Int): String {
        val finishIndex = (items().size - 1)
        return when (index) {
            0 -> wpStartInitial // Start waymark
            finishIndex -> wpFinishInitial // Finish waymark
            else -> Integer.toString(index) // Numbered intermediate waymark
        }
    }

    override fun onItemSingleTap(item: OverlayItem?): Boolean {
        if (ttrOverlay == null) // LiveRide
            return false
        if (ttrOverlay.tapState.routeIsPlanned())
            return false

        activeItem = item
        activeItemIndex = items().indexOf(item)
        // If a route is being planned and a waypoint is tapped,
        // show a menu to remove or renumber the waypoint:
        Dialog.listViewDialog(mapView.context,
            R.string.waypoint_renumber_title,
            setUpOptions(),
            this)
        return true
    }

    /**
     * Option on waypoint menu tapped.
     *
     * @param optionTapped the zero-based index of the option that was selected from the list.
     *   For an example of what the list contents look like, see [setUpOptions].
     */
    override fun onClick(dialog: DialogInterface, optionTapped: Int) {
        if (optionTapped == REMOVE_WAYPOINT_OPTION) {
            ttrOverlay?.stepBack(false, activeItemIndex)
        }
        else {
            renumberWaypoints(optionTapped)
        }
        activeItem = null
        mapView.invalidate()
    }

    private fun renumberWaypoints(optionTapped: Int) {
        // Remove waypoint from list and put it back at desired position
        removeMarker(activeItemIndex)
        if (optionTapped <= activeItemIndex) {
            items().add(optionTapped - 1, activeItem)
        }
        else {
            items().add(optionTapped, activeItem)
        }
        // Now check items are of correct waypoint type (Start / intermediate waypoint / Finish)
        checkWaypoints()
    }

    // Check waypoints have correct labels and icons
    private fun checkWaypoints() {
        if (waymarkersCount() == 0)
            return

        correctLabelAndIcon(items().first().snippet, startLabel, 0, wispWpStart)

        // Intermediate waypoints
        for (i in 1 .. waymarkersCount() - 2) {
            correctLabelAndIcon(items()[i].snippet, waypointLabel, i, wispWpMid)
        }

        if (waymarkersCount() > 1) {
            correctLabelAndIcon(items().last().snippet, finishLabel, items().lastIndex, wispWpFinish)
        }
    }

    private fun correctLabelAndIcon(snippet: String?, label: String, i: Int, wisp: Drawable?) {
        if (snippet != label) {
            val prevPoint = items().get(i).point
            removeMarker(i)
            items().add(i, makeMarker(prevPoint, label, wisp))
        }
    }

    /**
     * Generates a list of options that should appear when you tap on an existing waypoint marker.
     *
     * For example, if the following waypoints exist: `Start, 1, 2, 3, 4, Finish` then, when the
     * user taps on Waypoint 2, they will be presented with the following list:
     *
     * - Remove waypoint
     * - Change to Start
     * - Change to 1
     * - Change to 3
     * - Change to 4
     * - Change to Finish
     */
    private fun setUpOptions(): List<String> {
        // Get appropriate string from each OverlayItem, e.g. "Change to Start", "Change to waypoint 4"
        val itemsAsOptions = items().mapIndexed {
                index, wp ->
                res.getString(R.string.change_to) + wp.snippet + " " + waypointNumber(wp.snippet, index)
        }

        val optionsList: List<String> = listOf(
            res.getString(R.string.remove_waypoint),
            *itemsAsOptions.toTypedArray()
        )

        // Remove current item
        return optionsList.filterIndexed { index, _ -> (index != activeItemIndex + 1) }
    }

    private fun waypointNumber(snippet: String, index: Int): String {
        return if (snippet.contains( waypointLabel, true))
            index.toString()
        else
            // It's a Start or Finish waypoint, so no number
            ""
    }
}
