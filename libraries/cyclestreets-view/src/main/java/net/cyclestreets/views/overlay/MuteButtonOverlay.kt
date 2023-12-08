package net.cyclestreets.views.overlay

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial.Icon.gmd_phonelink_lock
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial.Icon.gmd_volume_mute
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial.Icon.gmd_volume_off
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.util.Theme.highlightColor
import net.cyclestreets.util.Theme.lowlightColor
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
//mport kotlin.coroutines.jvm.internal.CompletedContinuation.context


private var isAudioOn: Boolean = true
private var mContext: Context? = null

//private var audioManager: AudioManager = new
//private var audioManager: AudioManager()

class MuteButtonOverlay(private val mapView: CycleMapView) : Overlay(), PauseResumeListener {

    companion object {
        private const val LOCK_PREF = "muteButton"
    }

    private val audioMuteButton: FloatingActionButton
    private val onIcon: Drawable
    private val offIcon: Drawable


    init {
        val context = mapView.context

       // onIcon = materialIcon(context, gmd_phonelink_lock, highlightColor(context))
        onIcon = materialIcon(context, gmd_volume_mute, highlightColor(context))
        offIcon = materialIcon(context, gmd_volume_off, lowlightColor(context))

        val liveRideButtonView = LayoutInflater.from(context).inflate(R.layout.mutebutton, null)
        audioMuteButton = liveRideButtonView.findViewById<FloatingActionButton>(R.id.mute_button).apply {
            setOnClickListener { _ -> screenLockButtonTapped() }
            //setImageDrawable(onIcon)




            /*
            if(isAudioOn == false){
                setImageDrawable(onIcon)
            } else {
                setImageDrawable(offIcon)
            }*/

        }
        //audioMuteButton.setImageDrawable(onIcon)
        mapView.addView(liveRideButtonView)
        //mapView.keepScreenOn = false
      //  mapView.muteAudio = false


    }

    private fun screenLockButtonTapped() {
        setScreenLockState(!mapView.keepScreenOn)
    }

    private fun setScreenLockState(state: Boolean) {
       /* Log.d("LiveRide", "Setting keepScreenOn state to $state")
        screenLockButton.setImageDrawable(if (state) onIcon else offIcon)
        val message = if (state) R.string.liveride_keep_screen_on_enabled else R.string.liveride_keep_screen_on_disabled
        Toast.makeText(mapView.context, message, Toast.LENGTH_LONG).show()
        mapView.keepScreenOn = state*/
        val context = mapView.context
        val amanager = getSystemService(context, AudioManager::class.java) as AudioManager?
        amanager!!.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)

        //getSystemService(context, AlarmManager::class.java)

        if (mapView.muteAudio) {
           // isAudioOn = false
            amanager!!.setStreamMute(AudioManager.STREAM_MUSIC, false) // for unmute
            audioMuteButton.setImageDrawable(onIcon);
            mapView.muteAudio = false


        } else {
            //isAudioOn = true
            amanager!!.setStreamMute(AudioManager.STREAM_MUSIC, true) //for mute
           audioMuteButton.setImageDrawable(offIcon);
            mapView.muteAudio = true
        }

        Log.e("We are here", "we are here")
    }

    override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {}

    /////////////////////////////////////////
    override fun onResume(prefs: SharedPreferences) {
       // mapView.keepScreenOn = prefs.getBoolean(LOCK_PREF, false)
        mapView.muteAudio = prefs.getBoolean(LOCK_PREF, mapView.muteAudio)
        Log.e("Is audio muted?", mapView.muteAudio.toString())
        if(mapView.muteAudio) {
            audioMuteButton.setImageDrawable(offIcon)
        } else {
            audioMuteButton.setImageDrawable(onIcon)
        }




    }

    override fun onPause(prefs: SharedPreferences.Editor) {
        //prefs.putBoolean(LOCK_PREF, mapView.keepScreenOn)
        prefs.putBoolean(LOCK_PREF,  mapView.muteAudio)
    }

}
