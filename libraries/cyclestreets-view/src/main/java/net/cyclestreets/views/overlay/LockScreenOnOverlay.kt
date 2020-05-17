package net.cyclestreets.views.overlay

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.util.Theme.highlightColor
import net.cyclestreets.util.Theme.lowlightColor
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay


class LockScreenOnOverlay(private val mapView: CycleMapView) : Overlay(), PauseResumeListener {

    companion object {
        private const val LOCK_PREF = "lockScreen"
    }

    private val screenLockButton: FloatingActionButton
    private val onIcon: Drawable
    private val offIcon: Drawable

    init {
        val context = mapView.context

        onIcon = materialIcon(context, GoogleMaterial.Icon.gmd_phonelink_lock, highlightColor(context))
        offIcon = materialIcon(context, GoogleMaterial.Icon.gmd_phonelink_lock, lowlightColor(context))

        val liveRideButtonView = LayoutInflater.from(context).inflate(R.layout.liveride_buttons, null)
        screenLockButton = liveRideButtonView.findViewById<FloatingActionButton>(R.id.liveride_screenlock_button).apply {
            setOnClickListener { _ -> screenLockButtonTapped() }
            setImageDrawable(offIcon)
        }
        mapView.addView(liveRideButtonView)
        mapView.keepScreenOn = false
    }

    private fun screenLockButtonTapped() {
        setScreenLockState(!mapView.keepScreenOn)
    }

    private fun setScreenLockState(state: Boolean) {
        Log.d("LiveRide", "Setting keepScreenOn state to $state")
        screenLockButton.setImageDrawable(if (state) onIcon else offIcon)
        val message = if (state) R.string.liveride_keep_screen_on_enabled else R.string.liveride_keep_screen_on_disabled
        Toast.makeText(mapView.context, message, Toast.LENGTH_LONG).show()
        mapView.keepScreenOn = state
    }

    override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {}

    /////////////////////////////////////////
    override fun onResume(prefs: SharedPreferences) {
        mapView.keepScreenOn = prefs.getBoolean(LOCK_PREF, false)
    }

    override fun onPause(prefs: SharedPreferences.Editor) {
        prefs.putBoolean(LOCK_PREF, mapView.keepScreenOn)
    }

}
