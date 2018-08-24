package net.cyclestreets.api

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat

import net.cyclestreets.core.R

import java.util.HashMap

class PhotoMarkers(private val res: Resources) {
    private lateinit var defaultMarker: Drawable
    private val markers = HashMap<String, Drawable>()
    private val bfo: BitmapFactory.Options

    init {
        defaultMarker = ResourcesCompat.getDrawable(res, R.drawable.general_neutral, null)!!

        bfo = BitmapFactory.Options()
        bfo.inTargetDensity = 240
    }

    fun getMarker(photo: Photo): Drawable {
        val key = String.format("photomarkers/%s_%s.png", photo.category(), mapMetaCat(photo.metacategory()))

        if (!markers.containsKey(key)) {
            try {
                val asset = res.assets.open(key)
                val bmp = BitmapFactory.decodeStream(asset)
                val marker = BitmapDrawable(res, bmp)
                asset.close()
                markers[key] = marker
                return marker
            } catch (e: Exception) {
                markers[key] = defaultMarker
            }
        }

        return markers[key]
    }

    private fun mapMetaCat(mc: String): String {
        return if ("good" == mc || "bad" == mc) mc else "neutral"
    }
}
