package net.cyclestreets

import android.Manifest
import net.cyclestreets.util.GPS
import net.cyclestreets.util.MessageBox
import net.cyclestreets.views.CycleMapView
import net.cyclestreets.views.overlay.LiveRideOverlay
import net.cyclestreets.views.overlay.LockScreenOnOverlay
import net.cyclestreets.views.overlay.RouteOverlay
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RelativeLayout

import com.mikepenz.iconics.context.IconicsContextWrapper
import net.cyclestreets.util.Logging
import net.cyclestreets.util.hasPermission

private val TAG = Logging.getTag(LiveRideActivity::class.java)

class LiveRideActivity : Activity() {
    private lateinit var map: CycleMapView

    override fun attachBaseContext(newBase: Context) {
        // Allows the use of Material icon library, see https://github.com/mikepenz/Android-Iconics
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    // No need to override onCreate - the map is initialized in onResume

    public override fun onPause() {
        map.disableFollowLocation()
        map.onPause()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()

        // Map needs to be recreated, because tile provider is shut down on CycleMapView.onPause
        initializeMapView()
        map.onResume()
        map.enableAndFollowLocation()
    }

    private fun initializeMapView() {
        map = CycleMapView(this, this.javaClass.name).apply {
            overlayPushBottom(RouteOverlay())
            overlayPushTop(LockScreenOnOverlay(this@LiveRideActivity, this))
            overlayPushTop(LiveRideOverlay(this@LiveRideActivity, this))
            lockOnLocation()
            hideLocationButton()
        }
        RelativeLayout(this).apply {
            addView(map,
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                RelativeLayout.LayoutParams.MATCH_PARENT))
            this@LiveRideActivity.setContentView(this)
        }
    }

    companion object {
        fun launch(context: Context) {
            if (!hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Should be unreachable but we're being defensive
                Log.w(TAG, "Location permission is not granted.  Bail out.")
                return
            }

            if (!GPS.isOn(context)) {
                MessageBox.YesNo(context,
                    "LiveRide needs the GPS location service.\n\nWould you like to turn it on now?")
                { _, _ -> GPS.showSettings(context) }
                return
            }

            // Proceed
            context.startActivity(Intent(context, LiveRideActivity::class.java))
        }
    }
}
