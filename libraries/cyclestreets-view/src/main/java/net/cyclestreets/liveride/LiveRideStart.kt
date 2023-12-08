package net.cyclestreets.liveride

import android.app.Service
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import net.cyclestreets.routing.Journey
import org.osmdroid.util.GeoPoint

internal class LiveRideStart(context: Context, tts: TextToSpeech?) : LiveRideState(context, tts) {

    fun setServiceForeground(liveRideService: Service): LiveRideStart {
        notifyAndSetServiceForeground(liveRideService, "Starting LiveRide")
        return this
    }

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        journey.setActiveSegmentIndex(0)
        Log.d("importantTest", "LiveRideStart Update: ${journey.activeSegment()!!.toString()}")
        notify(journey.activeSegment()!!, true)


        return HuntForSegment(this)


    }

    override fun isStopped(): Boolean { return false }
    override fun arePedalling(): Boolean { return false }
}
