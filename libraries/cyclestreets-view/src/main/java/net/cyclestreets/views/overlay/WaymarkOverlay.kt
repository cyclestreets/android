package net.cyclestreets.views.overlay

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem

import java.util.ArrayList

class WaymarkOverlay(private val mapView: CycleMapView) : Overlay(), PauseResumeListener, Route.Listener {

    private val wispWpStart = makeWisp(R.drawable.wp_green_wisp)
    private val wispWpMid = makeWisp(R.drawable.wp_orange_wisp)
    private val wispWpFinish = makeWisp(R.drawable.wp_red_wisp)
    private val screenPos = Point()
    private val bitmapTransform = Matrix()
    private val bitmapPaint = Paint()

    private val waymarkers = ArrayList<OverlayItem>()

    private fun makeWisp(drawable: Int) : Drawable? {
        return ResourcesCompat.getDrawable(mapView.context.resources, drawable, null)
    }

    //////////////////////////////////////
    fun waymarkersCount(): Int {
        return waymarkers.size
    }

    fun waypoints(): Waypoints {
        return Waypoints(waymarkers.map { wp -> wp.point })
    }

    fun finish(): IGeoPoint {
        return waymarkers.last().point
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
        waymarkers.add(makeMarker(point, label, icon))
    }

    private fun popMarker() {
        waymarkers.removeAt(waymarkers.lastIndex)
    }

    private fun makeMarker(point: IGeoPoint, label: String, icon: Drawable?): OverlayItem {
        return OverlayItem(label, label, GeoPoint(point.latitude, point.longitude)).apply {
            setMarker(icon)
            markerHotspot = OverlayItem.HotspotPlace.BOTTOM_CENTER
        }
    }

    ////////////////////////////////////////////
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        val projection = mapView.projection

        waymarkers.forEach { wp -> drawMarker(canvas, projection, wp) }
    }

    private fun drawMarker(canvas: Canvas,
                           projection: Projection,
                           marker: OverlayItem) {
        projection.toPixels(marker.point, screenPos)

        val transform = mapView.matrix
        val transformValues = FloatArray(9)
        transform.getValues(transformValues)

        val bitmap = getBitmapFromDrawable(marker.drawable)

        val halfWidth = bitmap.width / 2
        val halfHeight = bitmap.height / 2

        bitmapTransform.apply {
            setTranslate((-halfWidth).toFloat(), (-halfHeight).toFloat())
            postScale(1 / transformValues[Matrix.MSCALE_X], 1 / transformValues[Matrix.MSCALE_Y])
            postTranslate(screenPos.x.toFloat(), screenPos.y.toFloat())
        }

        canvas.apply {
            save()
            rotate(-projection.orientation, screenPos.x.toFloat(), screenPos.y.toFloat())
            drawBitmap(bitmap, bitmapTransform, bitmapPaint)
            restore()
        }
    }

    ////////////////////////////////////
    private fun setWaypoints(waypoints: Waypoints) {
        resetWaypoints()

        waypoints.forEach { wp -> addWaypoint(wp) }
    }

    private fun resetWaypoints() {
        waymarkers.clear()
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
}
