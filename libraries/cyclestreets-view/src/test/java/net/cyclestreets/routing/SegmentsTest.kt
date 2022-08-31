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
        Assertions.assertThat(seg1.street()).isEqualTo("Crossing")
        assertEquals(seg1.turn().textInstruction, "turn right then turn left")
        assertFalse(seg1.walk())

        val seg2 = journey.segments[6]
        Assertions.assertThat(seg2.street()).isEqualTo("Crossing")
        assertEquals(seg2.turn().textInstruction, "turn left")
        assertTrue(seg2.walk())
    }

    private fun loadJourneyFrom(domainJsonFile: String) {
        val rawJson = TestUtils.fromResourceFile(domainJsonFile)
        val routeData = RouteData(rawJson, null, "test route", false)
        Route.onNewJourney(routeData)
        journey = Route.journey()
    }

}