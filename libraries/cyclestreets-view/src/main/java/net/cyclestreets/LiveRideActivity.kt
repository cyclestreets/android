package net.cyclestreets

import android.Manifest
import net.cyclestreets.liveride.LiveRideService
import net.cyclestreets.util.GPS
import net.cyclestreets.util.MessageBox
import net.cyclestreets.views.CycleMapView

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RelativeLayout

import net.cyclestreets.util.Logging
import net.cyclestreets.util.hasPermission
import net.cyclestreets.views.overlay.*


private val TAG = Logging.getTag(LiveRideActivity::class.java)


class LiveRideActivity : Activity(), ServiceConnection, LiveRideOverlay.Locator {
    private lateinit var map: CycleMapView
    private lateinit var liveride: LiveRideService.Binding

    override fun onServiceConnected(className: ComponentName, binder: IBinder) {
        liveride = binder as LiveRideService.Binding

        if (!liveride.areRiding())
            liveride.startRiding()
    }

    override fun onServiceDisconnected(className: ComponentName) {}

    override fun lastLocation(): Location? {
        return liveride.lastLocation()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, LiveRideService::class.java)
        this.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    public override fun onDestroy() {
        if (this::liveride.isInitialized) {
            liveride.stopRiding()
        }
        this.unbindService(this)
        super.onDestroy()
    }

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
            overlayPushTop(WaymarkOverlay(this))
            overlayPushTop(LockScreenOnOverlay(this))
            overlayPushTop(RotateMapOverlay(this))
            overlayPushTop(LiveRideOverlay(this@LiveRideActivity, this@LiveRideActivity))
            lockOnLocation()
            hideLocationButton()
            shiftAttribution()
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
