package net.cyclestreets.routing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import net.cyclestreets.TestUtils
import net.cyclestreets.content.RouteData
import org.assertj.core.api.Assertions
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SegmentsTest {

    private lateinit var journey: Journey

    private val roboContext = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        Route.initialise(roboContext)
    }

    @Test
    fun rightThenLeftWalkCycleCreatesTwoSegments() {
        loadJourneyFrom("journey-rightleft-walkcycle-domain.json")

        val seg1 = journey.segments[5]
        assertEquals(seg1.street(),"Crossing")
        assertEquals(seg1.turn().textInstruction, "turn right then turn left")
        assertFalse(seg1.walk())

        val seg2 = journey.segments[6]
        assertEquals(seg2.street(), "Crossing")
        assertEquals(seg2.turn().textInstruction, "turn left")
        assertTrue(seg2.walk())
    }
    @Test
    fun overBridgeWalkCycleCreatesTwoSegments() {
        loadJourneyFrom("journey-overbridge-walkcycle-domain.json")

        val seg1 = journey.segments[4]
        assertEquals(seg1.street(),"Odney Common")
        assertEquals(seg1.turnInstruction, "Straight on over Bridge")
        assertTrue(seg1.walk())

        val seg2 = journey.segments[5]
        assertEquals(seg2.street(),"Odney Common")
        assertEquals(seg2.turnInstruction, "Straight on")
        assertFalse(seg2.walk())
    }

    private fun loadJourneyFrom(domainJsonFile: String) {
        val rawJson = TestUtils.fromResourceFile(domainJsonFile)
        val routeData = RouteData(rawJson, null, "test route", false)
        Route.onNewJourney(routeData)
        journey = Route.journey()
    }

}