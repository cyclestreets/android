package net.cyclestreets.views.overlay

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import net.cyclestreets.api.Photo
import net.cyclestreets.util.Logging
import net.cyclestreets.view.R

import java.util.HashMap


private val TAG = Logging.getTag(PhotoMarkers::class.java)

private const val GOOD = "good"
private const val BAD = "bad"
private const val NEUTRAL = "neutral"

private const val GENERAL = "general"


class PhotoMarkers(val res: Resources) {
    // The drawable identified below is arbitrary - it just puts us in the right place for deriving
    // all the photomap resource IDs from their name
    private val resPackage = res.getResourcePackageName(R.drawable.pm_bicycles_bad)
    private val markers = HashMap<String, Drawable>()
    private val defaultMarkers = mapOf(
            GOOD to getMarker(GENERAL, GOOD),
            BAD to getMarker(GENERAL, BAD),
            NEUTRAL to getMarker(GENERAL, NEUTRAL)
    )

    fun getMarker(photo: Photo): Drawable {
        return getMarker(photo.category(), mapMetaCat(photo.metacategory()))
    }

    private fun getMarker(category: String, metaCategory: String): Drawable {

        val key = "pm_${category}_${metaCategory}"

        if (!markers.containsKey(key)) {
            try {
                val resId = res.getIdentifier(key, "drawable", resPackage)
                val marker = ResourcesCompat.getDrawable(res, resId, null)!!
                Log.d(TAG, "Loaded image $key into the cache, from resId $resId")
                markers[key] = marker
            } catch (e: Exception) {
                Log.w(TAG, "Error while loading image $key into the cache")
                markers[key] = defaultMarkers[metaCategory]!!
            }
        }

        return markers[key]!!
    }

    private fun mapMetaCat(mc: String): String {
        return if (GOOD == mc || BAD == mc) mc else NEUTRAL
    }
}
