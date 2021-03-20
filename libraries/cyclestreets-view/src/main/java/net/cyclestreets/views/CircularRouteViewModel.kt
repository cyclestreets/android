package net.cyclestreets.views

import androidx.lifecycle.ViewModel
import net.cyclestreets.CycleStreetsPreferences
import java.util.*

class CircularRouteViewModel: ViewModel() {
    val CIRCULAR_ROUTE_MIN_MINUTES = 5
    val CIRCULAR_ROUTE_MAX_MINUTES = 200
    val CIRCULAR_ROUTE_MIN_MILES = 1
    val CIRCULAR_ROUTE_MAX_MILES = 30
    val CIRCULAR_ROUTE_MIN_KM = 1
    val CIRCULAR_ROUTE_MAX_KM = 50

    val units = CycleStreetsPreferences.units()
    val minKmOrMiles = if (units == "km") CIRCULAR_ROUTE_MIN_KM else CIRCULAR_ROUTE_MIN_MILES
    val maxKmOrMiles = if (units == "km") CIRCULAR_ROUTE_MAX_KM else CIRCULAR_ROUTE_MAX_MILES

    val minValues = arrayOf(CIRCULAR_ROUTE_MIN_MINUTES, minKmOrMiles)
    val maxValues = arrayOf(CIRCULAR_ROUTE_MAX_MINUTES, maxKmOrMiles)
    // Duration and distance values
    var values = arrayOf(CIRCULAR_ROUTE_MIN_MINUTES, 0)

    var storeValues = arrayOf(CIRCULAR_ROUTE_MIN_MINUTES, minKmOrMiles)
    lateinit var currentValueUnit: Array<String>

    var position = 0

    init {

    }

    fun distanceInMetres(): Int {
        // Check if distance entered
        if (values[1] == 0) {
            return 0
        }
        else {
            // Convert distance to metres
            return if (units.toLowerCase(Locale.ROOT) == "miles") {
                (values[1] * 8000 / 5)
            }
            else values[1] * 1000
        }
    }
}