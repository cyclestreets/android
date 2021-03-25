package net.cyclestreets.views

import androidx.lifecycle.ViewModel
import net.cyclestreets.*
import net.cyclestreets.api.POICategory
import java.util.*

class CircularRouteViewModel: ViewModel() {

    val distanceUnits: String = CycleStreetsPreferences.units()
    private val maxDistance = if (distanceUnits == "km") CIRCULAR_ROUTE_MAX_DISTANCE_KM else CIRCULAR_ROUTE_MAX_DISTANCE_MILES

    // everything in an arrays, with lookup by DURATION (0) or DISTANCE (1).
    val minValues = arrayOf(CIRCULAR_ROUTE_MIN_MINUTES, CIRCULAR_ROUTE_MIN_DISTANCE)
    val maxValues = arrayOf(CIRCULAR_ROUTE_MAX_MINUTES, maxDistance)
    val units = arrayOf("dummy", distanceUnits)

    // mutable by the user
    var currentTab = DURATION
    var values = arrayOf(CIRCULAR_ROUTE_MIN_MINUTES, CIRCULAR_ROUTE_MIN_DISTANCE)
    var activeCategories: List<POICategory> = ArrayList()

    fun durationInSeconds(): Int {
        return values[DURATION] * 60
    }

    fun distanceInMetres(): Int {
        return if (distanceUnits.toLowerCase(Locale.ROOT) == "miles") {
            (values[DISTANCE] * 8000 / 5)
        }
        else values[DISTANCE] * 1000
    }

    companion object {
        const val DURATION = 0
        const val DISTANCE = 1
    }
}