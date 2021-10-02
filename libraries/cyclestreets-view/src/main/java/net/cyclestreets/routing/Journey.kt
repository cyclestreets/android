package net.cyclestreets.routing

import android.content.Context
import java.io.IOException

import android.text.TextUtils
import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.api.POI
import net.cyclestreets.api.POICategories
import net.cyclestreets.api.POICategory
import net.cyclestreets.api.client.dto.poiIcon

import net.cyclestreets.routing.domain.GeoPointDeserializer
import net.cyclestreets.routing.domain.JourneyDomainObject
import net.cyclestreets.routing.domain.SegmentDomainObject
import net.cyclestreets.util.Logging
import net.cyclestreets.util.Turn
import org.osmdroid.api.IGeoPoint

private val TAG = Logging.getTag(Journey::class.java)

class Journey private constructor(wp: Waypoints? = null) {
    val waypoints: Waypoints = wp ?: Waypoints.none()
    val segments: Segments = Segments()
    val elevation: ElevationProfile = ElevationProfile()
    val circularRoutePois = mutableSetOf<POI>()
    private var activeSegment: Int = 0

    companion object {
        val NULL_JOURNEY: Journey = Journey()
        init { NULL_JOURNEY.activeSegment = -1 }

        fun loadFromJson(domainJson: String, waypoints: Waypoints?, name: String?, context: Context): Journey {
            return JourneyFactory(waypoints, name).parse(domainJson, context)
        }
    }

    fun isEmpty(): Boolean { return segments.isEmpty() }

    private fun start(): Segment.Start { return segments.first() }
    private fun end(): Segment.End { return segments.last() }

    fun url(): String { return "https://cycle.st/j" + itinerary() }
    fun itinerary(): Int { return start().itinerary() }
    fun name(): String { return start().name() }
    fun plan(): String { return start().plan() }
    fun speed(): Int { return start().speed() }
    fun totalDistance(): Int { return end().totalDistance() }
    fun totalTime(): Int { return end().totalTime() }
    fun otherRoutes(): String {return start().otherRoutes()}

    fun remainingDistance(distanceUntilTurn: Int): Int {
        val actSeg = activeSegment()

        if (actSeg != null && activeSegment > 0) {
            return (totalDistance() - actSeg.cumulativeDistance + distanceUntilTurn)
        }
        // segments[0] is a summary of the whole journey and its Cumulative distance is same as Total distance.
        // Don't try and calculate remaining distance until actual segment has been found.
        return totalDistance()
    }

    fun remainingTime(distanceUntilTurn: Int): Int {
        val actSeg = activeSegment()
        val prevSeg = previousSegment()
        val prevSegCumulativeTime : Int = if (prevSeg != null) prevSeg.cumulativeTime else 0
        val timeToEndOfSeg : Int

        if (actSeg != null && activeSegment > 0) {
            // Time to cover whole of active segment
            val segTime = actSeg.cumulativeTime - prevSegCumulativeTime

            if (actSeg.distance != 0) {
                // This is an approximation of the time to the end of the active segment from current location
                timeToEndOfSeg = ((segTime * distanceUntilTurn).toFloat() / actSeg.distance.toFloat()).toInt()
            }
            else {
                timeToEndOfSeg = 0
            }

            return (totalTime() - actSeg.cumulativeTime + timeToEndOfSeg)
        }
        // Don't try and calculate remaining time until actual segment has been found
        return (totalTime())
    }

    /////////////////////////////////////////
    fun setActiveSegmentIndex(index: Int) { activeSegment = index }
    fun setActiveSegment(seg: Segment) {
        for (i in 0 until segments.count())
            if (seg === segments[i]) {
                setActiveSegmentIndex(i)
                break
            }
    }

    fun activeSegmentIndex(): Int { return activeSegment }

    fun previousSegment(): Segment? {
        return if (activeSegment > 1) segments[activeSegment - 1] else null
    }
    fun activeSegment(): Segment? {
        return if (activeSegment >= 0) segments[activeSegment] else null
    }
    fun nextSegment(): Segment? {
        return if (atEnd()) activeSegment() else segments[activeSegment + 1]
    }

    fun atStart(): Boolean { return activeSegment <= 0 }
    fun atWaypoint(): Boolean { return activeSegment() is Segment.Waymark }
    fun atEnd(): Boolean { return activeSegment == segments.count() - 1 }

    fun regressActiveSegment() {
        if (!atStart())
            --activeSegment
    }
    fun advanceActiveSegment() {
        if (!atEnd())
            ++activeSegment
    }

    fun points(): Iterator<IGeoPoint> {
        return segments.pointsIterator()
    }

    ////////////////////////////////////////////////////////////////////////////////
    private class JourneyFactory internal constructor(waypoints: Waypoints?, private val name: String?) {
        private val objectMapper = ObjectMapper()
        private val journey: Journey = Journey(waypoints)

        // Variables to maintain state as we process the JourneyDomainObject
        private var leg = 1
        private var totalDistance = 0
        private var totalTime = 0

        init {
            val module = SimpleModule()
            module.addDeserializer(IGeoPoint::class.java, GeoPointDeserializer())
            objectMapper.registerModule(module)
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        internal fun parse(domainJson: String, context: Context): Journey {
            // I guess this is in case the units have changed without the app restarting
            Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units())

            val jdo: JourneyDomainObject
            try {
                jdo = objectMapper.readValue(domainJson, JourneyDomainObject::class.java)
            } catch (e: IOException) {
                throw RuntimeException("Coding error - unable to parse domain JSON", e)
            }


            populateWaypoints(jdo)
            populateSegments(jdo)
            // Currently (June 2021) the API returns POIs whether or not they were requested.
            // https://github.com/cyclestreets/android/issues/465#issuecomment-818049555
            // For simplicity's sake, Will display these, even if not requested,
            // because, in the event of opening a circular route by number
            // there is no way of telling whether POIs were requested originally
            populatePois(jdo, context)

            generateStartAndFinishSegments(jdo)

            return journey
        }

        private fun populateWaypoints(jdo: JourneyDomainObject) {
            // waypoints will have the points tapped on the screen.  But if we are retrieving an existing route
            // (e.g. by number) then it will be empty, so needs to be populated from the json - the route
            // retrieved from the server.
            if (journey.waypoints.count() == 0) {
                for (gp in jdo.waypoints) {
                    journey.waypoints.add(gp)
                }
            }
        }

        private fun populateSegments(jdo: JourneyDomainObject) {
            for (sdo: SegmentDomainObject in jdo.segments) {
                if (sdo.legNumber != leg) {
                    journey.segments.add(Segment.Waymark(leg, totalDistance, sdo.points[0]))
                    leg = sdo.legNumber
                }

                totalTime += sdo.time
                totalDistance += sdo.distance
                journey.segments.add(
                    // Format time for display in Itinerary
                    Segment.Step(
                        getStreetName(sdo.name),
                        sdo.legNumber,
                        Turn.turnFor(sdo.turn),
                        sdo.turn,
                        sdo.shouldWalk,
                        totalTime,
                        sdo.distance,
                        totalDistance,
                        sdo.points
                    )
                )
                journey.elevation.add(sdo.segmentProfile)
            }
        }
        // For circular routes.
        private fun populatePois(jdo: JourneyDomainObject, context: Context) {
            // For circular route pois, the id isn't returned in the API, so will create an artificial one.
            var id = 0
            for (poi in jdo.pois) {
                val circularRoutePoi = POI(id,
                        poi.name,
                        "",
                        poi.website,
                        "",
                        "",
                        poi.latitude.toDouble(),
                        poi.longitude.toDouble())
                // Shouldn't ever have a poi which doesn't have a type,
                // but if it does happen, don't add it to the list to be displayed:
                val type = poi.poitypeId
                if (type != null) {
                    circularRoutePoi.setCategory(POICategory(type, "", poiIcon(context, type)))
                    journey.circularRoutePois.add(circularRoutePoi)
                }
                id++
            }
        }

        private fun getStreetName(name: String): String {
            // If "Link" appears three or more times, abbreviate! (https://github.com/cyclestreets/android/issues/305)
            val split = name.split(", Link")
            val numberOfLinks = split.size + 1
            return if (numberOfLinks >= 3) {
                val abbreviatedName = "${split.first()} and other streets"
                Log.d(TAG, "Abbreviating long street name $name to $abbreviatedName")
                abbreviatedName
            } else name
        }

        private fun generateStartAndFinishSegments(jdo: JourneyDomainObject) {
            val from = journey.waypoints.first()
            val to = if (journey.waypoints.isEmpty()) null else journey.waypoints.last()

            val pStart = journey.segments.startPoint()
            val pEnd = journey.segments.finishPoint()

            val startSeg = Segment.Start(
                jdo.route.itinerary,
                if (TextUtils.isEmpty(name)) jdo.route.name else name,
                jdo.route.plan,
                jdo.route.speed,
                totalTime,
                totalDistance,
                jdo.route.calories,
                jdo.route.grammesCO2saved,
                jdo.route.otherRoutes,
                listOf(pD(from, pStart), pStart)
            )
            val endSeg = Segment.End(
                jdo.route.finish,
                totalTime,
                totalDistance,
                listOf(pEnd, pD(to, pEnd))
            )

            journey.segments.add(startSeg)
            journey.segments.add(endSeg)
        }

        private fun pD(a1: IGeoPoint?, a2: IGeoPoint): IGeoPoint {
            return a1 ?: a2
        }
    }
}
