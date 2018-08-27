package net.cyclestreets.itinerary

import android.view.View
import android.widget.TextView
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.api.DistanceFormatter
import net.cyclestreets.fragments.R
import net.cyclestreets.routing.ElevationFormatter
import net.cyclestreets.routing.Journey
import java.util.*

internal fun fillInOverview(journey: Journey, parent: View, routeString: String) {
    val start = journey.segments().first()

    setText(parent, R.id.title, journey.name())
    setText(parent, R.id.journeyid, String.format(Locale.getDefault(), "#%,d", journey.itinerary()))
    setText(parent, R.id.routetype, "${journey.plan().capitalize()} $routeString:")
    setText(parent, R.id.distance, distanceFormatter().total_distance(journey.total_distance()))
    setText(parent, R.id.journeytime, start.totalTime())
    setText(parent, R.id.calories, start.calories())
    setText(parent, R.id.carbondioxide, start.co2())
    setText(parent, R.id.elevation_gain, elevationFormatter().height(journey.elevation().totalElevationGain()))
    setText(parent, R.id.elevation_loss, elevationFormatter().height(journey.elevation().totalElevationLoss()))
}

internal fun elevationFormatter(): ElevationFormatter {
    return ElevationFormatter.formatter(CycleStreetsPreferences.units())
}

private fun distanceFormatter(): DistanceFormatter {
    return DistanceFormatter.formatter(CycleStreetsPreferences.units())
}

private fun setText(parent: View, id: Int, text: String) {
    parent.findViewById<TextView>(id)!!.text = text
}
