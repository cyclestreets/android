package net.cyclestreets.views.overlay

//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial.Icon.gmd_volume_mute
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial.Icon.gmd_volume_off
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.liveride.LiveRideState
import net.cyclestreets.liveride.OnTheMove
import net.cyclestreets.util.Theme.highlightColor
import net.cyclestreets.util.Theme.lowlightColor
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import java.util.Locale
import java.util.UUID


//mport kotlin.coroutines.jvm.internal.CompletedContinuation.context


private var isAudioOn: Boolean = true
//private var mContext: Context? = null
//private lateinit var audioManager: AudioManager
//private var audioManager: AudioManager = new
//private var audioManager: AudioManager()

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



        /*tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Handle language not supported
                }
            } else {
                // Handle initialization failure
            }
        }*/
        mapView.addView(liveRideButtonView)
    }

    fun initializeTextToSpeech(context: Context) {
        //val context = context.getString(R.string.app_name)

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Handle language not supported
                }
            } else {
                // Handle initialization failure
            }
        }
    }
   /* public constructor(context: Context) : super() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Handle language not supported
                }
            } else {
                // Handle initialization failure
            }
        }
    }*/

   /* protected constructor(context: Context, tts: TextToSpeech?):
            this(context, tts, context.getString(R.string.app_name))*/

    private fun setMuteAudioState() {
       /* Log.d("LiveRide", "Setting keepScreenOn state to $state")
        screenLockButton.setImageDrawable(if (state) onIcon else offIcon)
        val message = if (state) R.string.liveride_keep_screen_on_enabled else R.string.liveride_keep_screen_on_disabled
        Toast.makeText(mapView.context, message, Toast.LENGTH_LONG).show()
        mapView.keepScreenOn = state*/
        val context = mapView.context
        //val amanager = getSystemService(context, AudioManager::class.java)  as AudioManager?

      /*  audioManager = getSystemService(context, AudioManager::class.java) as AudioManager
        val result = amanager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )*/

        //amanager!!.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)

        //getSystemService(context, AlarmManager::class.java)
       // val streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        muteAudio= sharedPreferences.getBoolean("MuteAudio", false)


        tts.speak("Text to say", TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());

        if(muteAudio){
            audioMuteButton.setImageDrawable(onIcon);
           // mapView.muteAudio = false
            editor.putBoolean("MuteAudio", false)
            editor.commit()


        } else {
            audioMuteButton.setImageDrawable(offIcon);
            editor.putBoolean("MuteAudio", true)
            editor.commit()
            //Toast.makeText(context, "Audio will stop after finished reading segment", Toast.LENGTH_SHORT).show()


/*
            if (::tts.isInitialized) {
                tts.stop()
            }*/




        }


      //  editor.putBoolean("MuteAudio", false)
       // editor.apply()






      //  if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Audio focus gained, mute your app's audio
          /*  if (mapView.muteAudio) {
                amanager?.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
               // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                audioMuteButton.setImageDrawable(onIcon);
                mapView.muteAudio = false

            } else {
                amanager?.adjustVolume(AudioManager.ADJUST_MUTE, 0)
               // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVolume, 0)
                audioMuteButton.setImageDrawable(offIcon);
                mapView.muteAudio = true

            }*/
       // amanager?.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND)




            // OR
            // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0) // Mute the audio in your app



       // } else {
            // Failed to gain audio focus
            // Handle accordingly (e.g., inform the user or try again later)
       // }





      /*  if (mapView.muteAudio) {
           // isAudioOn = false
            amanager?.setStreamMute(AudioManager.STREAM_MUSIC, false) // for unmute

           // amanager.setStreamMute(AudioManager.STREAM_MUSIC, false) // for unmute
            //val amanager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
         //   amanager!!.setStreamMute(AudioManager.STREAM_NOTIFICATION, false)

            //amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

            audioMuteButton.setImageDrawable(onIcon);
            mapView.muteAudio = false


        } else {
            //isAudioOn = true
            amanager?.setStreamMute(AudioManager.STREAM_MUSIC, true) //for mute
           // amanager!!.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
           audioMuteButton.setImageDrawable(offIcon);
            mapView.muteAudio = true
        }
*/

    }

    override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {}

    /////////////////////////////////////////


  /*  override fun onStart(prefs: SharedPreferences) {

        mapView.muteAudio = prefs.getBoolean(LOCK_PREF, mapView.muteAudio)

        if(mapView.muteAudio) {
            audioMuteButton.setImageDrawable(offIcon)
        } else {
            audioMuteButton.setImageDrawable(onIcon)
        }

    }*/



    override fun onResume(prefs: SharedPreferences) {
/*
        mapView.muteAudio = prefs.getBoolean(LOCK_PREF, mapView.muteAudio)

        if(mapView.muteAudio) {
            audioMuteButton.setImageDrawable(offIcon)
        } else {
            audioMuteButton.setImageDrawable(onIcon)
        }*/
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

     //   prefs.putBoolean(LOCK_PREF,  mapView.muteAudio)
    }

}
