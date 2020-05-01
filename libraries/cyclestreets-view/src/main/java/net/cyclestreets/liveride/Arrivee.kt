package net.cyclestreets.liveride

import net.cyclestreets.routing.Journey
import org.osmdroid.util.GeoPoint

internal class Arrivee(previous: LiveRideState) : LiveRideState(previous) {

    companion object {
        const val ARRIVEE = "Arrivée"
    }

    init {
        notify(ARRIVEE)
    }

    override fun update(journey: Journey, myLocation: GeoPoint, accuracy: Int): LiveRideState {
        return Stopped(context)
    }

    override fun isStopped(): Boolean { return false }
    override fun arePedalling(): Boolean { return false }
}