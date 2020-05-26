package net.cyclestreets.liveride

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import net.cyclestreets.util.Logging
import net.cyclestreets.util.*
import org.osmdroid.util.GeoPoint

import net.cyclestreets.routing.Route
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.ERROR
import android.speech.tts.TextToSpeech.SUCCESS

private const val UPDATE_DISTANCE = 5f  // metres
private const val UPDATE_TIME = 500L    // milliseconds
private val TAG = Logging.getTag(LiveRideService::class.java)

class LiveRideService : Service(), LocationListener, TextToSpeech.OnInitListener {
    private lateinit var binder: IBinder
    private lateinit var locationManager: LocationManager
    private var stage: LiveRideState? = null
    private var lastLocation: Location? = null

    override fun onCreate() {
        binder = Binding()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        stage = Stopped(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        stopRiding()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    @SuppressLint("MissingPermission") // We handle this with the hasPermission() check
    fun startRiding() {
        if (!stage!!.isStopped())
            return

        if (!hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Should be unreachable, but we're being defensive
            Log.w(TAG, "Location permission is not granted.  Bail out.")
            return
        }

        val tts = TextToSpeech(this, this)
        tts.setOnUtteranceProgressListener(AudioFocuser(this))
        stage = LiveRideStart(this, tts).setServiceForeground(this)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_TIME, UPDATE_DISTANCE, this)
        Log.d(TAG, "startRiding")
    }

    fun stopRiding() {
        if (stage!!.isStopped())
            return
        stage!!.tts!!.stop()
        stage!!.tts!!.shutdown()
        stage = Stopped(this)
        locationManager.removeUpdates(this)
        Log.d(TAG, "stopRiding")
    }

    inner class Binding : Binder() {
        private fun service(): LiveRideService { return this@LiveRideService }
        fun startRiding() { service().startRiding() }
        fun stopRiding() { service().stopRiding() }
        fun areRiding(): Boolean { return service().stage!!.arePedalling() }
        fun lastLocation(): Location? { return service().lastLocation }
    }

    // TextToSpeech init listener
    @SuppressLint("MissingPermission")
    override fun onInit(status: Int) {
        Log.i(TAG, "TextToSpeech init returned $status ($SUCCESS=success, $ERROR=error)")
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { onLocationChanged(it) }
    }

    // Location listener
    override fun onLocationChanged(location: Location) {
        if (!Route.routeAvailable()) {
            stopRiding()
            return
        }

        lastLocation = location

        val whereIam = GeoPoint(location)
        val accuracy: Int = if (location.hasAccuracy()) location.accuracy.toInt() else 2
        stage = stage!!.update(Route.journey(), whereIam, accuracy)
    }

    override fun onProviderDisabled(arg0: String) {}
    override fun onProviderEnabled(arg0: String) {}
    override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle) {}
}
