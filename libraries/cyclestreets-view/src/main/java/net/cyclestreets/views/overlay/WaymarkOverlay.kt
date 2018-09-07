package net.cyclestreets.views.overlay

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat

import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable

import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.Theme
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView

import org.osmdroid.api.IGeoPoint
import org.osmdroid.api.IProjection
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem

import java.util.ArrayList

open class WaymarkOverlay(private val mapView: CycleMapView) : Overlay(), PauseResumeListener, Route.Listener {

    private val greenWisp: Drawable?
    private val orangeWisp: Drawable?
    private val redWisp: Drawable?
    private val screenPos = Point()
    private val bitmapTransform = Matrix()
    private val bitmapPaint = Paint()

    private val shareDrawable: Drawable
    private val commentDrawable: Drawable

    private val waymarkers: MutableList<OverlayItem>

    init {
        val context = mapView.context

        val res = context.resources
        greenWisp = ResourcesCompat.getDrawable(res, R.drawable.green_wisp, null)
        orangeWisp = ResourcesCompat.getDrawable(res, R.drawable.orange_wisp, null)
        redWisp = ResourcesCompat.getDrawable(res, R.drawable.red_wisp, null)

        shareDrawable = IconicsDrawable(context)
                .icon(GoogleMaterial.Icon.gmd_share)
                .color(Theme.lowlightColorInverse(context))
                .sizeDp(24)
        commentDrawable = IconicsDrawable(context)
                .icon(GoogleMaterial.Icon.gmd_comment)
                .color(Theme.lowlightColorInverse(context))
                .sizeDp(24)

        waymarkers = ArrayList()
    }

    private fun setWaypoints(waypoints: Waypoints) {
        resetWaypoints()

        for (waypoint in waypoints) {
            addWaypoint(waypoint)
        }
    }

    private fun resetWaypoints() {
        waymarkers.clear()
    }

    protected fun waymarkersCount(): Int {
        return waymarkers.size
    }

    fun waypoints(): Waypoints {
        val p = ArrayList<IGeoPoint>()
        for (o in waymarkers)
            p.add(o.point)
        return Waypoints(p)
    }

    fun finish(): IGeoPoint {
        return waymarkers[waymarkersCount() - 1].point
    }

    protected fun addWaypoint(point: IGeoPoint?) {
        if (point == null)
            return
        when (waymarkersCount()) {
            0 -> waymarkers.add(addMarker(point, "start", greenWisp))
            1 -> waymarkers.add(addMarker(point, "finish", redWisp))
            else -> {
                val prevFinished = finish()
                waymarkers.removeAt(waymarkersCount() - 1)
                waymarkers.add(addMarker(prevFinished, "waypoint", orangeWisp))
                waymarkers.add(addMarker(point, "finish", redWisp))
            }
        }
    }

    protected fun removeWaypoint() {
        when (waymarkersCount()) {
            0 -> {
            }
            1, 2 -> waymarkers.removeAt(waymarkersCount() - 1)
            else -> {
                waymarkers.removeAt(waymarkersCount() - 1)
                val prevFinished = finish()
                waymarkers.removeAt(waymarkersCount() - 1)
                waymarkers.add(addMarker(prevFinished, "finish", redWisp))
            }
        }
    }

    private fun addMarker(point: IGeoPoint, label: String, icon: Drawable?): OverlayItem {
        val marker = OverlayItem(label, label, GeoPoint(point.latitude, point.longitude))
        marker.setMarker(icon)
        marker.markerHotspot = OverlayItem.HotspotPlace.BOTTOM_CENTER
        return marker
    }

    ////////////////////////////////////////////
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        val projection = mapView.projection
        for (waypoint in waymarkers)
            drawMarker(canvas, projection, waypoint)
    }

    private fun drawMarker(canvas: Canvas,
                           projection: IProjection,
                           marker: OverlayItem) {
        projection.toPixels(marker.point, screenPos)

        val transform = mapView.matrix
        val transformValues = FloatArray(9)
        transform.getValues(transformValues)

        val thingToDraw = marker.drawable as BitmapDrawable
        val halfWidth = thingToDraw.intrinsicWidth / 2
        val halfHeight = thingToDraw.intrinsicHeight / 2
        bitmapTransform.setTranslate((-halfWidth).toFloat(), (-halfHeight).toFloat())
        bitmapTransform.postScale(1 / transformValues[Matrix.MSCALE_X], 1 / transformValues[Matrix.MSCALE_Y])
        bitmapTransform.postTranslate(screenPos.x.toFloat(), screenPos.y.toFloat())
        canvas.drawBitmap(thingToDraw.bitmap, bitmapTransform, bitmapPaint)
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
