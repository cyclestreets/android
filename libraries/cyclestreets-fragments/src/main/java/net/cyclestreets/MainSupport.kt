package net.cyclestreets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

import net.cyclestreets.routing.Route
import net.cyclestreets.util.Logging
import net.cyclestreets.util.MapPack

private val TAG = Logging.getTag(MainSupport::class.java)

object MainSupport {

    fun switchMapFile(launchIntent: Intent): Boolean {
        val mapPackage = launchIntent.getStringExtra("mapfile") ?: return false
        val pack = MapPack.findByPackage(mapPackage) ?: return false
        CycleStreetsPreferences.enableMapFile(pack.path())
        return true
    }

    fun handleLaunchIntent(launchIntent: Intent, context: Context): Boolean {
        val launchUri = launchIntent.data ?: return false

        val itinerary = findItinerary(launchUri)
        if (itinerary == -1)
            return false

        Route.FetchRoute(CycleStreetsPreferences.routeType(),
                         itinerary.toLong(),
                         CycleStreetsPreferences.speed(),
                         context)
        return true
    }

    private fun findItinerary(launchUri: Uri): Int {
        return try {
            val itinerary = extractItinerary(launchUri)
            Integer.parseInt(itinerary)
        } catch (whatever: Exception) {
            Log.w(TAG, "Failed to extract itinerary number from $launchUri")
            -1
        }
    }

    private fun extractItinerary(launchUri: Uri): String {
        val host = launchUri.host

        if ("cycle.st" == host) {
            // e.g. http://cycle.st/j61207326
            return launchUri.path.substring(2)
        }

        if ("m.cyclestreets.net" == host) {
            // e.g. https://m.cyclestreets.net/journey/#57201887/balanced
            val frag = launchUri.fragment
            return frag.substring(0, frag.indexOf('/'))
        }

        // e.g. http://(www.)cyclestreets.net/journey/61207326(/#balanced)
        val path = launchUri.path.substring(8)
        return path.replace("/", "")
    }

    // Helper classes
    internal enum class LaunchIntentType {
        JOURNEY, LOCATION;

        fun withId(id: Int): LaunchIntent {
            return LaunchIntent(this, id)
        }
    }

    internal class LaunchIntent(val type: LaunchIntentType, val id: Int)
}
