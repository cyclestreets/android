package net.cyclestreets.api.client.dto

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import androidx.core.content.res.ResourcesCompat
import com.fasterxml.jackson.annotation.JsonProperty
import net.cyclestreets.api.POICategories
import net.cyclestreets.api.POICategory
import net.cyclestreets.core.R


class PoiTypesDto {
    @JsonProperty(value = "validuntil")
    private val validUntil: Long = 0

    @JsonProperty
    private val types: Map<String?, PoiTypeDto>? = null

    class PoiTypeDto {
        @JsonProperty
        private lateinit var id: String

        @JsonProperty
        private lateinit var name: String

        @JsonProperty
        private lateinit var total: String

        fun toPOICategory(context: Context): POICategory {
            val icons = poiIcons(context, id)
            return POICategory(id, name, icons.first, icons.second)
        }
    }

    fun toPOICategories(context: Context): POICategories {
        val categories: MutableList<POICategory> = ArrayList()
        for ((_, value) in types!!) {
            categories.add(value.toPOICategory(context))
        }
        return POICategories(categories)
    }

}

private fun poiIcons(context: Context, id: String): Pair<Drawable, Drawable> {
    val key = "poi_${id}"
    val res = context.resources
    val resPackage = res.getResourcePackageName(R.drawable.poi_bedsforcyclists)

    val resId = res.getIdentifier(key, "drawable", resPackage)

    val nativeIcon = ResourcesCompat.getDrawable(res, resId, null)!!
    val maxIcon = ResourcesCompat.getDrawableForDensity(res, resId, DisplayMetrics.DENSITY_XXXHIGH, null)!!

    return Pair(nativeIcon, maxIcon)
}
