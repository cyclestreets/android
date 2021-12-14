package net.cyclestreets.views.overlay

import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.Dialog
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem

import java.util.ArrayList

class WaymarkOverlay(private val mapView: CycleMapView, private val TTROverlay: TapToRouteOverlay? = null) :
                                                            ItemizedOverlay<OverlayItem>(mapView.mapView(), ArrayList(), true),
                                                            PauseResumeListener,
                                                            Route.Listener,
                                                            DialogInterface.OnClickListener {

    // TODO: check waymark / waypoint terminology (will change screen text to waypoint for consistency)

    private val res = mapView.context.resources
    private var options = mutableListOf(res.getString(R.string.remove_waymark))

    private val wispWpStart = makeWisp(R.drawable.wp_start_wisp)
    private val wispWpMid = makeWisp(R.drawable.wp_mid_wisp)
    private val wispWpFinish = makeWisp(R.drawable.wp_finish_wisp)

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
        val count = waymarkersCount() - 1
        when (waymarkersCount()) {
            0 -> pushMarker(point, res.getString(R.string.waymark_start), wispWpStart)
            1 -> pushMarker(point, res.getString(R.string.waymark_finish), wispWpFinish)
            else -> {
                val prevFinished = finish()
                popMarker()
                pushMarker(prevFinished, "waypoint $count", wispWpMid)
                pushMarker(point, res.getString(R.string.waymark_finish), wispWpFinish)
            }
        }
    }

    fun removeWaypoint() {
        when (waymarkersCount()) {
            0 -> { }
            1, 2 -> popMarker()
            else -> {
                popMarker()
                val prevFinished = finish()
                popMarker()
                pushMarker(prevFinished, "finish", wispWpFinish)
            }
        }
    }

    private fun pushMarker(point: IGeoPoint, label: String, icon: Drawable?) {
        items().add(makeMarker(point, label, icon))
        // todo remove line options.add(res.getString(R.string.move_waymark_before) + label)
    }

    private fun popMarker() {
        items().removeAt(items().lastIndex)
        // todo remove line options.removeAt(options.lastIndex)
    }

    private fun makeMarker(point: IGeoPoint, label: String, icon: Drawable?): OverlayItem {
        return OverlayItem(label, label, GeoPoint(point.latitude, point.longitude)).apply {
            setMarker(icon)
            markerHotspot = OverlayItem.HotspotPlace.BOTTOM_CENTER
        }
    }

    ////////////////////////////////////
    private fun setWaypoints(waypoints: Waypoints) {
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

    override fun onItemSingleTap(item: OverlayItem?): Boolean {
        if (TTROverlay == null) // LiveRide
            return false
        if (TTROverlay.tapState.routeIsPlanned())
            return false

        Toast.makeText(mapView.context, "Waymark tapped", Toast.LENGTH_LONG).show()
        // todo: put waypoint number (label) in title
        Dialog.listViewDialog(mapView.context,
            R.string.waymark_move_title,
            setUpOptions(item),
            this)
        return true
    }

    override fun onClick(dialog: DialogInterface, whichButton: Int) {

        val option = options[whichButton]

        Toast.makeText(mapView.context, "Option $whichButton selected", Toast.LENGTH_LONG).show()
    }

    /* Example:
    Waypoints: Start, 1, 2, 3, 4, Finish.
    Tapped on Waypoint 2.
    Remove "Move before Waypoint 2" and "Move before Waypoint 3" from list of options as this wouldn't result in a move.
    List of options should be:
        Remove waypoint
        Move before Start
        Move before waypoint 1
        Move before waypoint 4
        Move before Finish
        Move after Finish
     */

    private fun setUpOptions(item: OverlayItem?): List<String> {
        val itemIndex = items().indexOf(item)
        // todo check it works if just 2 wps, Start and Finish
        // Convert OverlayItem to a string, e.g. "Move before waypoint 4"
        val itemsAsOptions = items().map { wp -> res.getString(R.string.move_waymark_before) + wp.snippet }

        val optionsList = mutableListOf(res.getString(R.string.remove_waymark))
        optionsList.apply {
            addAll(itemsAsOptions)
            add(res.getString(R.string.move_waymark_after) + res.getString(R.string.waymark_finish))
        }
        // Remove current item and the one after it
        return optionsList.filterIndexed { index, _ -> (index < itemIndex + 1) || (index > itemIndex + 2) }
        }

    }

