package net.cyclestreets.api

import android.graphics.drawable.Drawable
import net.cyclestreets.api.ApiClient.getPOIs
import org.osmdroid.api.IGeoPoint

class POICategory(private val key: String,
                  private val name: String,
                  private val overlayIcon: Drawable,
                  private val listIcon: Drawable) {

    fun name(): String {
        return name
    }

    fun icon(): Drawable {
        return overlayIcon
    }

    fun pois(centre: IGeoPoint, radius: Int): List<POI> {
        try {
            val pois = getPOIs(key, centre.longitude, centre.latitude, radius)
            for (poi in pois)
                poi.setCategory(this)
            return pois
        } catch (e: Exception) {
            return emptyList()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is POICategory) return false
        return (name == other.name)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "POICategory(key='$key', name='$name')"
    }

}
