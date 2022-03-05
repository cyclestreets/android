package net.cyclestreets.views.overlay

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.MapView
import android.graphics.Canvas
import net.cyclestreets.util.Brush

import java.util.ArrayList

class WaymarkOverlay(private val mapView: CycleMapView) : ItemizedOverlay<OverlayItem>(mapView.mapView(), ArrayList()),
                                                            PauseResumeListener,
                                                            Route.Listener {

    private val HORIZONTAL_TEXT_POSITION_ADJUSTMENT = 6.0f
    private val VERTICAL_TEXT_POSITION_ADJUSTMENT = 1.7f
    private val REDUCE_TEXT_SIZE = 0.8f

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
        when (waymarkersCount()) {
            0 -> pushMarker(point, "start", wispWpStart)
            1 -> pushMarker(point, "finish", wispWpFinish)
            else -> {
                val prevFinished = finish()
                popMarker()
                pushMarker(prevFinished, "waypoint", wispWpMid)
                pushMarker(point, "finish", wispWpFinish)
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
    }

    private fun popMarker() {
        items().removeAt(items().lastIndex)
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

    // Waymark has already been scaled when added to List - no need to scale again
    override  fun scale(mapView: MapView): Float {
        return 1.0F
    }

    override fun drawTextOnMarker(canvas: Canvas, rect: Rect, x: Int, y: Int, index: Int) {
        super.drawTextOnMarker(canvas, rect, x, y, index)

        val boldTextBrush = Brush.createBoldTextBrush((offset(mapView.getContext()) * REDUCE_TEXT_SIZE).toInt())
        val height = rect.height()
        val width = rect.width()

        canvas.drawText(
            waymarkNumber(index),
            x - width / HORIZONTAL_TEXT_POSITION_ADJUSTMENT,
            y - height / VERTICAL_TEXT_POSITION_ADJUSTMENT,
            boldTextBrush
        )
    }

    private fun waymarkNumber(index: Int): String {
        val finishIndex = (items().size - 1)
        return when (index) {
            0 -> "S" // Starting waymark
            finishIndex -> "F" // Finishing waymark
            else -> Integer.toString(index) // Numbered intermediate waymark
        }
    }
}
