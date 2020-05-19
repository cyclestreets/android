package net.cyclestreets.views.overlay

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.GeomagneticField
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.compass.IOrientationConsumer
import org.osmdroid.views.overlay.compass.IOrientationProvider
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider


class RotateMapOverlay(private val mapView: CycleMapView) : Overlay(), PauseResumeListener,
                                                            IMyLocationConsumer, IOrientationConsumer
{
    private val rotateButton: FloatingActionButton
    private val onIcon: Drawable
    private val offIcon: Drawable
    private val locationProvider: IMyLocationProvider
    private val compassProvider: IOrientationProvider
    private var rotate = false
    private var gpsspeed = 0f
    private var lat = 0f
    private var lon = 0f
    private var alt = 0f
    private var timeOfFix: Long = 0
    private var deviceOrientation = 0

    init {
        val context = mapView.context

        onIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.compass, null)!!
        offIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.compass_off, null)!!

        val rotateButtonView = LayoutInflater.from(context).inflate(R.layout.compassbutton, null)
        rotateButton = rotateButtonView.findViewById(R.id.compass_button)
        rotateButton.setOnClickListener { setRotation(!rotate) }

        mapView.addView(rotateButtonView)
        locationProvider = UseEverythingLocationProvider(context)
        compassProvider = InternalCompassOrientationProvider(context)
    }

    private fun setRotation(state: Boolean) {
        Log.d(TAG, "Setting map rotation to $state")
        rotateButton.setImageDrawable(if (state) onIcon else offIcon)
        if (state) startRotate() else endRotate()
        rotate = state
    }

    private fun startRotate() {
        locationProvider.startLocationProvider(this)
        compassProvider.startOrientationProvider(this)
    }

    private fun endRotate() {
        locationProvider.stopLocationProvider()
        compassProvider.stopOrientationProvider()
        resetMapOrientation()
        rotateButton.rotation = 0f
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        if (location == null) return

        gpsspeed = location.speed
        lat = location.latitude.toFloat()
        lon = location.longitude.toFloat()
        alt = location.altitude.toFloat()
        timeOfFix = location.time

        if (gpsspeed > onTheMoveThreshold)
            setMapOrientation(location.bearing)
    }

    override fun onOrientationChanged(orientationToMagneticNorth: Float, source: IOrientationProvider?) {
        if (gpsspeed > onTheMoveThreshold) return

        val gf = GeomagneticField(lat, lon, alt, timeOfFix)

        var trueNorth = orientationToMagneticNorth + gf.declination
        synchronized(trueNorth) {
            if (trueNorth > 360) trueNorth -= 360

            setMapOrientation(trueNorth)
        }
    }

    private fun setMapOrientation(orientation: Float) {
        var mapOrientation = 360 - orientation - deviceOrientation

        if (mapOrientation < 0) mapOrientation += 360
        if (mapOrientation > 360) mapOrientation -= 360

        //help smooth everything out
        mapOrientation = ((mapOrientation / 5).toInt()) * 5f

        mapView.mapView().apply {
            setMapCenterOffset(0, height / 4)
            setMapOrientation(mapOrientation)
        }

        rotateButton.rotation = mapOrientation
    }

    private fun resetMapOrientation() {
        mapView.mapView().apply {
            setMapCenterOffset(0, 0)
            setMapOrientation(0f)
        }
    }

    private fun captureDeviceOrientation() {
        val rotation = (mapView.context.getSystemService(
                Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        deviceOrientation = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            else -> 270
        }
    }

    override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {}

    /////////////////////////////////////////
    override fun onResume(prefs: SharedPreferences) {
        setRotation(prefs.getBoolean(ROTATE_PREF, false))
        captureDeviceOrientation()
    }

    override fun onPause(prefs: SharedPreferences.Editor) {
        endRotate()
        prefs.putBoolean(ROTATE_PREF, rotate)
    }

    companion object {
        private val TAG = Logging.getTag(RotateMapOverlay::class.java)

        private const val ROTATE_PREF = "rotateMap"

        private const val onTheMoveThreshold = 1
            // if speed is below this, prefer the compass for orientation
            // once we're move, prefer gps
    }

    private class UseEverythingLocationProvider(context: Context) : GpsMyLocationProvider(context) {
        init {
            val locMan = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            for (source in locMan.getProviders(true))
                addLocationSource(source)
        }
    }
}