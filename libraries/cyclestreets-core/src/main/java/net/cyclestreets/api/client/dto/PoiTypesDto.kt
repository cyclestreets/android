package net.cyclestreets.api.client.dto

import android.content.Context
import android.graphics.drawable.Drawable
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
            return POICategory(id, name, poiIcon(context, id))
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


private fun poiIcon(context: Context, id: String): Drawable {
    val key = "poi_${id}"
    val res = context.resources
    val resPackage = res.getResourcePackageName(defaultResId)

    val resId = res.getIdentifier(key, "drawable", resPackage)
    val resIdOrDefault = if (resId != 0) resId else defaultResId

    return ResourcesCompat.getDrawable(res, resIdOrDefault, null)!!
}

private val defaultResId = R.drawable.poi_attractions
