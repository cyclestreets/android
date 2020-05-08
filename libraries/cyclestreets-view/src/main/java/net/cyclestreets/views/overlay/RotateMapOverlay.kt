package net.cyclestreets.views.overlay

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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

class RotateMapOverlay(private val mapView: CycleMapView) : Overlay(), PauseResumeListener {
    private val rotateButton: FloatingActionButton
    private val onIcon: Drawable
    private val offIcon: Drawable
    private var rotate = false

    init {
        val context = mapView.context
        onIcon = highlightIcon(context)
        offIcon = lowlightIcon(context)

        val rotateButtonView = LayoutInflater.from(context).inflate(R.layout.compassbutton, null)
        rotateButton = rotateButtonView.findViewById(R.id.compass_button)
        rotateButton.setOnClickListener { view: View? -> setRotation(!rotate) }

        mapView.addView(rotateButtonView)
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
    }

    private fun endRotate() {
        mapView.mapView().setMapCenterOffset(0, 0)
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
}