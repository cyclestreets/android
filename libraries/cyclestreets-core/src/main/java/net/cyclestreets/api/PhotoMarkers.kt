package net.cyclestreets.api

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log

import net.cyclestreets.util.Logging

import java.util.HashMap

private val TAG = Logging.getTag(PhotoMarkers::class.java)

private const val GOOD = "good"
private const val BAD = "bad"
private const val NEUTRAL = "neutral"

private const val GENERAL = "general"

class PhotoMarkers(private val res: Resources) {
    private val defaultMarkers: Map<String, Drawable>
    private val markers = HashMap<String, Drawable>()
    private val bfo: BitmapFactory.Options

    init {
        defaultMarkers = mapOf(
            GOOD to getMarker(GENERAL, GOOD),
            BAD to getMarker(GENERAL, BAD),
            NEUTRAL to getMarker(GENERAL, NEUTRAL)
        )
        bfo = BitmapFactory.Options()
        bfo.inTargetDensity = 240
    }

    fun getMarker(photo: Photo): Drawable {
        return getMarker(photo.category(), mapMetaCat(photo.metacategory()))
    }

    private fun getMarker(category: String, metaCategory: String): Drawable {
        val key = String.format("photomarkers/%s_%s.png", category, metaCategory)

        if (!markers.containsKey(key)) {
            try {
                val asset = res.assets.open(key)
                val bmp = BitmapFactory.decodeStream(asset)
                val marker = BitmapDrawable(res, bmp)
                asset.close()
                markers[key] = marker
                return marker
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
