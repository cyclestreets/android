package net.cyclestreets.views.overlay


import android.content.Context
import android.content.SharedPreferences

import android.graphics.drawable.Drawable

import android.speech.tts.TextToSpeech

import android.view.LayoutInflater

import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial.Icon.gmd_volume_mute
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial.Icon.gmd_volume_off
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.util.Theme.highlightColor
import net.cyclestreets.util.Theme.lowlightColor
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView

import org.osmdroid.views.overlay.Overlay



private var isAudioOn: Boolean = true


class MuteButtonOverlay(private val mapView: CycleMapView) : Overlay(), PauseResumeListener {

    companion object {
        private const val LOCK_PREF = "muteButton"
    }

    private val audioMuteButton: FloatingActionButton
    private val onIcon: Drawable
    private val offIcon: Drawable
    private var muteAudio: Boolean = false
    private lateinit var tts: TextToSpeech


    init {
        val context = mapView.context


        onIcon = materialIcon(context, gmd_volume_mute, highlightColor(context))
        offIcon = materialIcon(context, gmd_volume_off, lowlightColor(context))

        val liveRideButtonView = LayoutInflater.from(context).inflate(R.layout.mutebutton, null)
        audioMuteButton = liveRideButtonView.findViewById<FloatingActionButton>(R.id.mute_button).apply {
            setOnClickListener { setMuteAudioState() }

        }



        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        muteAudio= sharedPreferences.getBoolean("MuteAudio", false)

        if (muteAudio){
            audioMuteButton.setImageDrawable(offIcon);
        } else {
            audioMuteButton.setImageDrawable(onIcon);
        }


        mapView.addView(liveRideButtonView)
    }


    private fun setMuteAudioState() {

        val context = mapView.context

        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        muteAudio= sharedPreferences.getBoolean("MuteAudio", false)

        if(muteAudio){
            audioMuteButton.setImageDrawable(onIcon);

            editor.putBoolean("MuteAudio", false)
            editor.commit()


        } else {
            audioMuteButton.setImageDrawable(offIcon);
            editor.putBoolean("MuteAudio", true)
            editor.commit()
            Toast.makeText(mapView.context, "Audio will mute after playing current directions.", Toast.LENGTH_LONG).show()

        }

    }



    override fun onResume(prefs: SharedPreferences) {

        val context = mapView.context
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        muteAudio= sharedPreferences.getBoolean("MuteAudio", false)

        if (muteAudio){
            audioMuteButton.setImageDrawable(offIcon);
        } else {
            audioMuteButton.setImageDrawable(onIcon);
        }


    }

    override fun onPause(prefs: SharedPreferences.Editor) {


    }

}
