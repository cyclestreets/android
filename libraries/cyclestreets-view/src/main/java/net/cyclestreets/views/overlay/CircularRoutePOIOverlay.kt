package net.cyclestreets.views.overlay

import android.content.SharedPreferences
import net.cyclestreets.Undoable
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.Logging
import net.cyclestreets.views.CycleMapView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox


private val TAG = Logging.getTag(CircularRoutePOIOverlay::class.java)

class CircularRoutePOIOverlay(mapView: CycleMapView):   PauseResumeListener,
                                                        Route.Listener,
                                                        ItemizedOverlay<POIOverlay.POIOverlayItem>(mapView.mapView(), ArrayList()),
                                                        Undoable {

    private var currentJourney: Journey? = null

    override fun onResume(prefs: SharedPreferences) {
        Route.registerListener(this)
    }

    override fun onPause(prefs: SharedPreferences.Editor) {
        Route.unregisterListener(this)
    }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
            removePois()
            currentJourney = journey
            for (poi in currentJourney!!.circularRoutePois) {
                items().add(POIOverlay.POIOverlayItem(poi))
            }
    }

    override fun onResetJourney() {
        removePois()
    }

    private fun removePois() {
        Bubble.hideBubble(this)
        // Remove Circular Route POI's from display
        if (currentJourney != null) {
            items().clear()
            currentJourney = null
        }
    }

    override fun onItemSingleTap(item: POIOverlay.POIOverlayItem?): Boolean {
        Bubble.hideOrShowBubble(item, this)
        mapView().postInvalidate()
        return true
    }

    override fun onBackPressed(): Boolean {
        Bubble.hideBubble(this)
        mapView().postInvalidate()
        return true
    }
}