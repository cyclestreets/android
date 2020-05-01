package net.cyclestreets

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

import net.cyclestreets.routing.Route
import net.cyclestreets.util.Logging
import net.cyclestreets.util.MapPack
import net.cyclestreets.LaunchIntent.Type.*
import net.cyclestreets.photos.IndividualPhoto

private val TAG = Logging.getTag(MainSupport::class.java)

object MainSupport {

    fun switchMapFile(intent: Intent, context: Context): Boolean {
        val mapPackage = intent.getStringExtra("mapfile") ?: return false
        val pack = MapPack.findByPackage(context, mapPackage) ?: return false
        CycleStreetsPreferences.enableMapFile(pack.path())
        return true
    }

    fun handleLaunchIntent(intent: Intent, activity: Activity): Boolean {
        val launchUri = intent.data ?: return false
        Log.d(TAG, "Handling launch intent with URI: $launchUri")

        val launchIntent = determineLaunchIntent(launchUri) ?: return false

        when(launchIntent.type) {
            JOURNEY -> {
                Log.d(TAG, "Loading journey #${launchIntent.id}")
                (activity as RouteMapActivity).showRouteMap()
                Route.FetchRoute(CycleStreetsPreferences.routeType(),
                                 launchIntent.id,
                                 CycleStreetsPreferences.speed(),
                                 activity as Context)
            }
            LOCATION -> {
                Log.d(TAG, "Loading location #${launchIntent.id}")
                (activity as PhotoMapActivity).showPhotoMap()
                IndividualPhoto.fetchPhoto(launchIntent.id)
            }
        }
        return true
    }
}

internal fun determineLaunchIntent(launchUri: Uri): LaunchIntent? {
    return try {
        return extractLaunchIntent(launchUri)
    } catch (whatever: Exception) {
        Log.w(TAG, "Failed to extract itinerary number from $launchUri")
        null
    }
}

private fun extractLaunchIntent(launchUri: Uri): LaunchIntent {
    val host = launchUri.host!!
    val path = launchUri.path!!.substring(1) // Drop the leading '/'
    val intentType: LaunchIntent.Type
    val id: Long

    when(host) {
        "cycle.st" -> {
            // e.g. https://cycle.st/j61207326 or https://cycle.st/p93348
            intentType = if (path.startsWith("j")) JOURNEY else LOCATION
            id = path.drop(1).toLong()
        }
        "m.cyclestreets.net" -> {
            // e.g. https://m.cyclestreets.net/journey/#57201887/balanced or https://m.cyclestreets.net/location/#4444
            val frag = launchUri.fragment!! // everything after the #
            if (path.startsWith("journey")) {
                intentType = JOURNEY
                id = frag.substring(0, frag.indexOf('/')).toLong()
            } else {
                intentType = LOCATION
                id = frag.toLong()
            }
        }
        "cyclestreets.net", "www.cyclestreets.net" -> {
            // e.g. http(s)://(www.)cyclestreets.net/journey/61207326(/#balanced) or .../location/93348
            if (path.startsWith("journey")) {
                intentType = JOURNEY
                id = path.drop(7).replace("/", "").toLong()
            } else {
                intentType = LOCATION
                id = path.drop(8).replace("/", "").toLong()
            }
        }
        else -> throw IllegalStateException("Unrecognised host pattern '$host' in '$launchUri'")
    }
    return intentType.withId(id)
}

// Helper classes
internal class LaunchIntent private constructor(val type: Type, val id: Long) {
    internal enum class Type {
        JOURNEY, LOCATION;

        fun withId(id: Long): LaunchIntent {
            return LaunchIntent(this, id)
        }
    }
}
