package net.cyclestreets.routing

import java.util.*
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import kotlin.collections.ArrayList

val NULL_WAYPOINTS = Waypoints.none()

class Waypoints(points: List<IGeoPoint>) : Iterable<IGeoPoint> {
    private val waypoints = LinkedList<IGeoPoint>(points)
    // Allows us to track that the first waypoint is ephemeral, i.e.:
    // -  it was created as part of a LiveRide on-the-fly replan
    // -  it shouldn't be included in any subsequent replans
    var firstWaypointEphemeral: Boolean = false

    fun count(): Int { return waypoints.size }
    fun isEmpty(): Boolean { return waypoints.isEmpty() }

    fun first(): IGeoPoint? { return if (waypoints.isEmpty()) null else waypoints.first }
    fun last(): IGeoPoint { return waypoints.last }

    fun add(lat: Double, lon: Double): Waypoints { return add(GeoPoint(lat, lon)) }
    fun add(geoPoint: IGeoPoint): Waypoints {
        waypoints.add(geoPoint)
        return this
    }

    operator fun get(i: Int): IGeoPoint { return waypoints[i] }

    override fun iterator(): Iterator<IGeoPoint> {
        return waypoints.iterator()
    }

    fun reversed(): Waypoints {
        val points = ArrayList(waypoints)
        points.reverse()
        return Waypoints(points)
    }

    fun fromLeg(legNumber: Int): Waypoints {
        return Waypoints(waypoints.subList(legNumber, count()))
    }

    fun startingWith(geoPoint: IGeoPoint): Waypoints {
        val newWp = listOf(geoPoint) + if (firstWaypointEphemeral) waypoints.drop(1) else waypoints
        return Waypoints(newWp)
    }

    companion object {
        fun none(): Waypoints {
            return Waypoints(emptyList())
        }

        fun fromTo(start: IGeoPoint, end: IGeoPoint): Waypoints {
            return Waypoints(listOf(start, end))
        }
    }
}
