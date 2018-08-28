package net.cyclestreets.routing

import java.util.LinkedList

import net.cyclestreets.util.Turn
import org.osmdroid.api.IGeoPoint

class Segments : Iterable<Segment> {
    private val segments = LinkedList<Segment>()

    fun count(): Int { return segments.size }
    fun isEmpty(): Boolean { return segments.isEmpty() }

    fun startPoint(): IGeoPoint { return segments.first.start() }
    fun finishPoint(): IGeoPoint { return segments.last.finish() }
    fun first(): Segment.Start { return segments.first as Segment.Start }
    fun last(): Segment.End { return segments.last as Segment.End }

    fun add(seg: Segment) {
        if (seg is Segment.Start) {
            segments.addFirst(seg)
            return
        }

        if (count() != 0) {
            val previous = segments[count() - 1]

            // Meld "Join Roundabout" instructions
            if (Turn.JOIN_ROUNDABOUT == previous.turn) {
                segments.remove(previous)
                segments.add(Segment.Step(previous, seg, seg.turn, seg.turnInstruction))
                return
            }

            // Meld staggered crossroads
            if (previous.distance_ < 20) {
                if (Turn.TURN_LEFT == previous.turn && Turn.TURN_RIGHT == seg.turn) {
                    segments.remove(previous)
                    segments.add(Segment.Step(previous, seg, Turn.LEFT_RIGHT, Turn.LEFT_RIGHT.textInstruction))
                    return
                }
                if (Turn.TURN_RIGHT == previous.turn && Turn.TURN_LEFT == seg.turn) {
                    segments.remove(previous)
                    segments.add(Segment.Step(previous, seg, Turn.RIGHT_LEFT, Turn.RIGHT_LEFT.textInstruction))
                    return
                }
            }

            // Meld bridges
            if (previous.distance_ < 100 && "Bridge".equals(previous.name_, ignoreCase = true) && Turn.STRAIGHT_ON === seg.turn) {
                segments.remove(previous)
                segments.add(Segment.Step(previous, seg, previous.turn, previous.turnInstruction + " over Bridge"))
                return
            }
        }

        segments.add(seg)
    }

    operator fun get(i: Int): Segment { return segments[i] }

    override fun iterator(): Iterator<Segment> { return segments.iterator() }
    fun pointsIterator(): Iterator<IGeoPoint> { return PointsIterator(this) }

    private class PointsIterator internal constructor(segments: Segments) : Iterator<IGeoPoint> {
        private val segmentIterator: Iterator<Segment> = segments.iterator()
        private var pointIterator: Iterator<IGeoPoint>? = null

        init {
            if (segmentIterator.hasNext())
                pointIterator = segmentIterator.next().points()
        }

        override fun hasNext(): Boolean {
            return pointIterator?.hasNext() ?: false
        }

        override fun next(): IGeoPoint {
            if (!hasNext())
                throw IllegalStateException()

            val p = pointIterator!!.next()

            if (!hasNext()) {
                if (segmentIterator.hasNext())
                    pointIterator = segmentIterator.next().points()
                else
                    pointIterator = null
            }

            return p
        }
    }
}
