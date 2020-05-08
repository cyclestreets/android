package net.cyclestreets.views.overlay

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import net.cyclestreets.util.Theme
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider

class RotateMapOverlay(private val mapView: CycleMapView)
    : Overlay(), PauseResumeListener, IMyLocationConsumer
{
    private val rotateButton: FloatingActionButton
    private val onIcon: Drawable
    private val offIcon: Drawable
    private val locationProvider: IMyLocationProvider
    private var rotate = false

    init {
        val context = mapView.context
        onIcon = highlightIcon(context)
        offIcon = lowlightIcon(context)

        val rotateButtonView = LayoutInflater.from(context).inflate(R.layout.compassbutton, null)
        rotateButton = rotateButtonView.findViewById(R.id.compass_button)
        rotateButton.setOnClickListener { view: View? -> setRotation(!rotate) }

        mapView.addView(rotateButtonView)
        locationProvider = UseEverythingLocationProvider(context)
    }

    private fun setRotation(state: Boolean) {
        Log.d("LiveRide", "Setting map rotation to $state")
        rotateButton.setImageDrawable(if (state) onIcon else offIcon)
        if (state) startRotate() else endRotate()
        rotate = state
    }

    private fun startRotate() {
        val yOffset = mapView.mapView().height / 3
        mapView.mapView().setMapCenterOffset(0, yOffset)
        locationProvider.startLocationProvider(this)
    }

    private fun endRotate() {
        locationProvider.stopLocationProvider()
        mapView.mapView().setMapCenterOffset(0, 0)
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        if (location == null) return

        val gpsbearing = location.bearing
        val gpsspeed = location.speed

        //use gps bearing instead of the compass
        var t: Float = 360 - gpsbearing// - this.deviceOrientation
        if (t < 0) {
            t += 360f
        }
        if (t > 360) {
            t -= 360f
        }

        //help smooth everything out
        t = (t as Float / 5) * 5

        if (gpsspeed >= 0.01) {
            mapView.mapView().setMapOrientation(t)
        }
    }


    override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {}

    /////////////////////////////////////////
    override fun onResume(prefs: SharedPreferences) {
        setRotation(prefs.getBoolean(ROTATE_PREF, false))
    }

    override fun onPause(prefs: SharedPreferences.Editor) {
        endRotate()
        prefs.putBoolean(ROTATE_PREF, rotate)
    }

    companion object {
        private const val ROTATE_PREF = "rotateMap"

        private fun highlightIcon(context: Context)= icon(context, Theme.highlightColor(context))
        private fun lowlightIcon(context: Context) = icon(context, Theme.lowlightColor(context))

        private fun icon(context: Context, themeColor: Int): Drawable {
            return IconicsDrawable(context)
                    .icon(GoogleMaterial.Icon.gmd_navigation)
                    .color(themeColor)
                    .sizeDp(24)
        }
    }

    private class UseEverythingLocationProvider(context: Context) : GpsMyLocationProvider(context) {
        init {
            val locMan = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            for (source in locMan.getProviders(true))
                addLocationSource(source)
        }
    }
}