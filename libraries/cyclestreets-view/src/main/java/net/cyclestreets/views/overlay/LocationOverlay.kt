package net.cyclestreets.views.overlay

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.util.Logging
import net.cyclestreets.util.Theme
import net.cyclestreets.util.doOrRequestPermission
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


private val TAG = Logging.getTag(LocationOverlay::class.java)


class LocationOverlay(private val mapView: CycleMapView) :
        MyLocationNewOverlay(UseEverythingLocationProvider(mapView.context), mapView.mapView()) {

    private val button: FloatingActionButton
    private val onColor: Int = Theme.lowlightColor(mapView.context)
    private val followColor: Int = Theme.highlightColor(mapView.context) or -0x1000000

    private class UseEverythingLocationProvider(context: Context) : GpsMyLocationProvider(context) {
        init {
            val locMan = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            for (source in locMan.getProviders(true))
                addLocationSource(source)
        }
    }

    init {
        val context = mapView.context

        // Workaround for https://github.com/cyclestreets/android/issues/385 until the fix for
        // https://github.com/osmdroid/osmdroid/issues/1530 is available (in osmdroid 6.1.7 or above)
        // When the workaround is removed, the corresponding drawables should be also.
        val person = ResourcesCompat.getDrawable(context.resources, org.osmdroid.library.R.drawable.person, null) as BitmapDrawable
        val newDirectionArrow = ResourcesCompat.getDrawable(context.resources, R.drawable.temp_osmdroid_twotone_navigation_black_48, null) as BitmapDrawable
        setDirectionArrow(person.bitmap, newDirectionArrow.bitmap)
        // End workaround

        val overlayView = LayoutInflater.from(context).inflate(R.layout.locationbutton, null)
        button = overlayView.findViewById(R.id.locationbutton)
        button.setImageDrawable(materialIcon(context, GoogleMaterial.Icon.gmd_my_location))
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
        setEnableAutoStop(false)
    }

    fun hideButton() {
        button.visibility = View.INVISIBLE
    }

    // Allows pinch-zoom while in LiveRide
    // see https://github.com/cyclestreets/android/issues/384
    // and https://github.com/osmdroid/osmdroid/issues/1578
    override fun onTouchEvent(event: MotionEvent, mapView: MapView?): Boolean {
        val isSingleFingerDrag = (event.action == MotionEvent.ACTION_MOVE)
                && (event.pointerCount == 1)

        if (event.action == MotionEvent.ACTION_DOWN && enableAutoStop) {
            disableFollowLocation()
        } else if (isSingleFingerDrag && isFollowLocationEnabled) {
            return true // prevent the pan
        }

        return false
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
