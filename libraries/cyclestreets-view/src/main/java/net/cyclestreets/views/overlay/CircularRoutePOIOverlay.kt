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

class CircularRoutePOIOverlay(mapView: CycleMapView): PauseResumeListener, Route.Listener,
        LiveItemOverlay<POIOverlay.POIOverlayItem?>(mapView, false), Undoable {

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
            val items: MutableList<POIOverlay.POIOverlayItem> = ArrayList()
            for (poi in currentJourney!!.circularRoutePois) {
                items.add(POIOverlay.POIOverlayItem(poi))
            }
            setItems(items as List<POIOverlay.POIOverlayItem?>?)
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

    // This won't ever be called
    override fun fetchItemsInBackground(mapCentre: IGeoPoint,
                                        zoom: Int,
                                        boundingBox: BoundingBox): Boolean {
        // Return false so that "Loading" message doesn't appear
        return false
    }

    override fun onZoom(event: ZoomEvent): Boolean {
        // Don't want any of the functionality in the superclass so override and do nothing / return true
        return true
    }

    override fun onScroll(event: ScrollEvent): Boolean {
        // Don't want any of the functionality in the superclass, so override and do nothing / return true
        return true
    }

    override fun onItemSingleTap(item: POIOverlay.POIOverlayItem?): Boolean {
        Bubble.hideOrShowBubble(item, this)
        redraw()
        return true
    }

    override fun onBackPressed(): Boolean {
        Bubble.hideBubble(this)
        redraw()
        return true
    }
}