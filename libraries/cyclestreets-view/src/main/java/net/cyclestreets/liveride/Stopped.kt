package net.cyclestreets.liveride

import android.content.Context
import net.cyclestreets.routing.Journey
import org.osmdroid.util.GeoPoint

internal class Stopped(context: Context) : LiveRideState(context, null) {

    init {
        cancelNotification()
    }

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        return this
    }

    override fun isStopped(): Boolean { return true }
    override fun arePedalling(): Boolean { return false }
}
