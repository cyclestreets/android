package net.cyclestreets.views.overlay

import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.Dialog
import net.cyclestreets.util.GeoHelper
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem

import java.util.ArrayList

private val TAG = Logging.getTag(WaymarkOverlay::class.java)

class WaymarkOverlay(private val mapView: CycleMapView, private val TTROverlay: TapToRouteOverlay? = null) :
                                                            ItemizedOverlay<OverlayItem>(mapView.mapView(), ArrayList(), true),
                                                            PauseResumeListener,
                                                            Route.Listener,
                                                            DialogInterface.OnClickListener {

    private val REMOVE_WAYPOINT_OPTION = 0
    private val res = mapView.context.resources

    private val wispWpStart = makeWisp(R.drawable.wp_start_wisp)
    private val wispWpMid = makeWisp(R.drawable.wp_mid_wisp)
    private val wispWpFinish = makeWisp(R.drawable.wp_finish_wisp)

    private var activeItem: OverlayItem? = null
    private var itemIndex = 0

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

    fun addAltWaypoint(point: IGeoPoint, index: Int, uid: String) {
        pushMarker(point, waypointLabel, wispWpMid, uid, index)
        Log.d(TAG, "Added alternative waypoint $point")
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

        popMarker(index)
        checkWaypoints()

    }

    fun removeAltWaypoint(uid: String) {
        items().removeAll { it.uid == uid }
    }

    private fun pushMarker(point: IGeoPoint, label: String, icon: Drawable?, uid: String? = null, index: Int = waymarkersCount()) {
        items().add(index, makeMarker(uid, point, label, icon))
    }

    private fun popMarker(index: Int) {
        items().removeAt(index)
    }

    private fun makeMarker(uid: String?, point: IGeoPoint, label: String, icon: Drawable?): OverlayItem {
        return OverlayItem(uid, label, label, GeoPoint(point.latitude, point.longitude)).apply {
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
        // If there are any alt waymarks, save them so they can be restored when app resumes
        if (items().filter { it.uid != null }.size > 0)
            Route.saveWaymarks(items())
        Route.unregisterListener(this)
    }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        if (Route.altRouteInProgress()) {
            val items = Route.restoreWaymarks().toMutableList()
            for (item in items)
                items().add(item)
        }
        else
            setWaypoints(waypoints)
    }

    override fun onResetJourney() {
        resetWaypoints()
    }

    override fun onItemSingleTap(item: OverlayItem?): Boolean {
        if (TTROverlay == null) // LiveRide
            return false
        // Don't allow if a route has been planned.  Return true so a waypoint won't be added.
        if ((TTROverlay.tapState.routeIsPlanned()) || (TTROverlay.tapState.altRouteIsPlanned()))
            return true

        activeItem = item
        // If a route is being planned and a waypoint is tapped,
        // show a menu to remove or renumber the waypoint:
        Dialog.listViewDialog(mapView.context,
            R.string.waypoint_renumber_title,
            setUpOptions(item),
            this)
        return true
    }

    // Option on waypoint menu tapped:
    override fun onClick(dialog: DialogInterface, optionTapped: Int) {
        if (optionTapped == REMOVE_WAYPOINT_OPTION) {
            TTROverlay?.stepBack(itemIndex)
        }
        else {
            renumberWaypoints(optionTapped, itemIndex, activeItem)
        }
        activeItem = null
        mapView.invalidate()
    }

    fun renumberWaypoints(optionTapped: Int, itemIndex: Int, activeItem: OverlayItem?) {
        // Remove waypoint from list and put it back at desired position
        popMarker(itemIndex)
        if (optionTapped <= itemIndex) {
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
        for (i in 1 .. waymarkersCount() - 2){
                correctLabelAndIcon(items()[i].snippet, waypointLabel, i, wispWpMid)
        }

        if (waymarkersCount() > 1) {
            correctLabelAndIcon(items().last().snippet, finishLabel, items().lastIndex, wispWpFinish)
        }
    }

    private fun correctLabelAndIcon(snippet: String?, label: String, i: Int, wisp: Drawable?) {
        if (snippet != label) {
            val prevPoint = items()[i].point
            popMarker(i)
            items().add(i, makeMarker(null, prevPoint, label, wisp))
        }
    }

    /* Example:
    Waypoints: Start, 1, 2, 3, 4, Finish.
    Tapped on Waypoint 2.
    List of options should be:
        Remove waypoint
        Change to Start
        Change to 1
        Change to 3
        Change to 4
        Change to Finish
     */

    private fun setUpOptions(item: OverlayItem?): List<String> {
        itemIndex = items().indexOf(item)
        // Get appropriate string from each OverlayItem, e.g. "Change to Start", "Change to waypoint 4"
        val itemsAsOptions = items().mapIndexed {
                index, wp ->
                res.getString(R.string.change_to) + wp.snippet + " " + waypointNumber(wp.snippet, index)
        }

        val optionsList = mutableListOf(res.getString(R.string.remove_waypoint))
        optionsList.apply {
            addAll(itemsAsOptions)
        }
        // Remove current item
        return optionsList.filterIndexed { index, _ -> (index != itemIndex + 1) }
    }

    private fun waypointNumber(snippet: String, index: Int): String {
        return if (snippet.contains( waypointLabel, true))
            index.toString()
        else
            // It's a Start or Finish waypoint, so no number
            ""
    }

    fun getWaypointSequence(point: IGeoPoint): Int {

        val closestIndex = getClosestIndex(point)

        when (closestIndex) {
            // If Start is closest waypoint, new waypoint will be after Start
            0 -> return 1
            // If Finish point is closest, new waypoint will be before Finish
            waymarkersCount() - 1 -> return (closestIndex)
            // Otherwise, determine whether point is before or after closest one
            else -> {
                val prevPointLat = items()[closestIndex - 1].point.latitude
                val closestPointLat = items()[closestIndex].point.latitude
                val nextPointLat = items()[closestIndex + 1].point.latitude
                // Latitudes are in increasing order
                if (latlonIncreasing(prevPointLat, closestPointLat, nextPointLat, point.latitude))
                    return if (point.latitude <= closestPointLat)
                        closestIndex
                    else
                        closestIndex + 1
                // Latitudes are in decreasing order
                if (latlonDecreasing(prevPointLat, closestPointLat, nextPointLat, point.latitude))
                    return if (point.latitude >= closestPointLat)
                        closestIndex
                    else
                        closestIndex + 1

                val prevPointLon = items()[closestIndex - 1].point.longitude
                val closestPointLon = items()[closestIndex].point.longitude
                val nextPointLon = items()[closestIndex + 1].point.longitude
                // Longitudes are in increasing order
                if (latlonIncreasing(prevPointLon, closestPointLon, nextPointLon, point.longitude))
                    return if (point.longitude <= closestPointLon)
                        closestIndex
                    else
                        closestIndex + 1
                // Longitudes are in decreasing order
                if (latlonDecreasing(prevPointLon, closestPointLon, nextPointLon, point.longitude))
                    return if (point.longitude >= closestPointLon)
                        closestIndex
                    else
                        closestIndex + 1
                // Points aren't in a very logical order so just stick new point after closest one!
                // todo could probably calc nearest segment (see HuntForSegment) and work out from that if it should be
                //  before or after the waypoint
                return closestIndex + 1
            }
        }
    }

    private fun getClosestIndex(point: IGeoPoint): Int {
        var minDistance = Int.MAX_VALUE
        var closestIndex = 0
        for (item in items()) {
            val distance = GeoHelper.distanceBetween(point, item.point)
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = items().indexOf(item)
            }
        }
        return closestIndex
    }

    private fun latlonIncreasing(prevPointLatlon: Double, closestPointLatlon: Double, nextPointLatlon: Double, pointLatlon: Double): Boolean {
        // Latitudes are in increasing order
        return ((prevPointLatlon <= closestPointLatlon)
                && (closestPointLatlon <= nextPointLatlon)
                // ... and new point's latitude is between them
                && (prevPointLatlon <= pointLatlon)
                && ( pointLatlon <= nextPointLatlon))

    }

    private fun latlonDecreasing(prevPointLatlon: Double, closestPointLatlon: Double, nextPointLatlon: Double, pointLatlon: Double): Boolean {
        return ((prevPointLatlon >= closestPointLatlon)
                && (closestPointLatlon >= nextPointLatlon)
                // ... and new point's latitude is between them
                && (prevPointLatlon >= pointLatlon)
                && (pointLatlon >= nextPointLatlon)
                )
    }
}
