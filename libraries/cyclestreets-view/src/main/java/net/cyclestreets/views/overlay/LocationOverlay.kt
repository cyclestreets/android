package net.cyclestreets.views.overlay

import android.Manifest.permission.ACCESS_FINE_LOCATION
import net.cyclestreets.util.Logging
import net.cyclestreets.util.Theme
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView

import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.location.LocationManager
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import net.cyclestreets.util.doOrRequestPermission

private val TAG = Logging.getTag(LocationOverlay::class.java)

class LocationOverlay(private val mapView: CycleMapView) :
        MyLocationNewOverlay(UseEverythingLocationProvider(mapView.context), mapView.mapView()) {

    private val button: FloatingActionButton
    private val onColor: Int = Theme.lowlightColor(mapView.context)
    private val followColor: Int = Theme.highlightColor(mapView.context) or -0x1000000
    private var lockedOn: Boolean = false

    private class UseEverythingLocationProvider(context: Context) : GpsMyLocationProvider(context) {
        init {
            val locMan = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            for (source in locMan.getProviders(true))
                addLocationSource(source)
        }
    }

    init {
        val overlayView = LayoutInflater.from(mapView.context).inflate(R.layout.locationbutton, null)
        button = overlayView.findViewById(R.id.locationbutton)
        button.setOnClickListener { _ -> enableAndFollowLocation(!isFollowLocationEnabled) }

        mapView.addView(overlayView)
    }

    fun enableLocation(enable: Boolean) {
        if (enable)
            enableMyLocation()
        else
            disableMyLocation()
    }

    fun enableAndFollowLocation(enable: Boolean) {
        Log.d(TAG, "Location button clicked, enable = $enable")
        if (enable) {
            try {
                doOrRequestPermission(mapView.context, ACCESS_FINE_LOCATION) {
                    enableMyLocation()
                    enableFollowLocation()
                    val lastFix = lastFix
                    if (lastFix != null) {
                        Log.d(TAG, "Setting map centre to $lastFix")
                        mapView.controller.setCenter(GeoPoint(lastFix))
                    }
                }
            } catch (e: RuntimeException) {
                // might not have location service
                Log.d(TAG, "Failed to enable location finding", e)
            }
        } else {
            disableFollowLocation()
            disableMyLocation()
        }

        mapView.invalidate()
    }

    fun lockOnLocation() {
        lockedOn = true
    }

    fun hideButton() {
        button.visibility = View.INVISIBLE
    }

    override fun onTouchEvent(event: MotionEvent, mapView: MapView?): Boolean {
        val handled = super.onTouchEvent(event, mapView)

        if (lockedOn && isMyLocationEnabled && event.action == MotionEvent.ACTION_MOVE)
            enableFollowLocation()

        return handled
    }

    ////////////////////////////////////////////
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        val buttonColor: Int
        if (isFollowLocationEnabled)
            buttonColor = followColor
        else if (isMyLocationEnabled)
            buttonColor = onColor
        else
            buttonColor = Color.TRANSPARENT

        button.setColorFilter(buttonColor)

        // I'm not thrilled about this but there isn't any other way (short of killing
        // and recreating the overlay) of turning off the little here-you-are man
        if (!isMyLocationEnabled)
            return

        super.draw(canvas, mapView, shadow)
    }
}
