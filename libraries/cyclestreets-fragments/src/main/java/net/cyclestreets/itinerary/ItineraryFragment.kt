package net.cyclestreets.itinerary

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.View
import android.widget.ListView
import net.cyclestreets.RouteMapActivity
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints

class ItineraryFragment : ListFragment(), Route.Listener {
    internal var journey = Journey.NULL_JOURNEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = SegmentAdapter(activity!!)
    }

    override fun onResume() {
        super.onResume()
        Route.onResume()
        Route.registerListener(this)
    }

    override fun onPause() {
        Route.unregisterListener(this)
        super.onPause()
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        if (journey.isEmpty)
            return

        journey.setActiveSegmentIndex(position)
        try {
            (activity as RouteMapActivity).showMap()
        } catch (e: Exception) {}
    }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        this.journey = journey
        setSelection(this.journey.activeSegmentIndex())
    }

    override fun onResetJourney() {
        journey = Journey.NULL_JOURNEY
    }
}
