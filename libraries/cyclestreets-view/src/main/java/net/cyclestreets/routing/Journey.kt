package net.cyclestreets.routing

import java.io.IOException
import java.util.Arrays

import android.text.TextUtils
import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.cyclestreets.CycleStreetsPreferences

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
    private var activeSegment: Int = 0

    companion object {
        val NULL_JOURNEY: Journey = Journey()
        init { NULL_JOURNEY.activeSegment = -1 }

        fun loadFromJson(domainJson: String, waypoints: Waypoints?, name: String?): Journey {
            return JourneyFactory(waypoints, name).parse(domainJson)
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

        internal fun parse(domainJson: String): Journey {
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
            generateStartAndFinishSegments(jdo)

            return journey
        }

        private fun populateWaypoints(jdo: JourneyDomainObject) {
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
            val to = journey.waypoints.last()

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
                Arrays.asList(pD(from, pStart), pStart)
            )
            val endSeg = Segment.End(
                jdo.route.finish,
                totalTime,
                totalDistance,
                Arrays.asList(pEnd, pD(to, pEnd))
            )

            journey.segments.add(startSeg)
            journey.segments.add(endSeg)
        }

        private fun pD(a1: IGeoPoint?, a2: IGeoPoint): IGeoPoint {
            return a1 ?: a2
        }
    }
}
