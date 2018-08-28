package net.cyclestreets.liveride

import android.speech.tts.TextToSpeech
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.TestUtils
import net.cyclestreets.content.RouteData
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.view.BuildConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.mockito.Mockito.mockingDetails;
import org.mockito.exceptions.verification.VerificationInOrderFailure

@Config(constants = BuildConfig::class, manifest = Config.NONE, sdk = [27])
@RunWith(RobolectricTestRunner::class)
class LiveRideTest {

    private lateinit var liveRideState: LiveRideState
    private lateinit var journey: Journey

    private val roboContext = ShadowApplication.getInstance().applicationContext
    private val mockTts = mock(TextToSpeech::class.java)
    private val inOrder = Mockito.inOrder(mockTts)

    @Before
    fun setUp() {
        CycleStreetsPreferences.initialise(roboContext, -1)

        liveRideState = LiveRideStart(roboContext, mockTts)
        liveRideState = OnTheMove(liveRideState)
        verify("Starting Live Ride")

        Route.initialise(roboContext)
    }

    @Test
    fun doubleStraightOn() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(13)
        assertThat(journey.activeSegment().street()).isEqualTo("lcn (unknown cycle network)")

        move(0.126, 52.212)
        verify("Get ready to Straight on")
        move(0.12634, 52.21207)
        verify("Straight on into lcn (unknown cycle network) continuation. Continue 5m")
        move(0.12634, 52.21207)
        // AdvanceToSegment advances the segment; we need another location update to trigger the next NearingTurn
        move(0.12634, 52.21207)
        verify("Get ready to Straight on")
        move(0.12634, 52.21207)
        verify("Straight on into lcn (unknown cycle network). Continue 85m")
    }

    @Test
    fun specificTurnIssues() {
        loadJourneyFrom("journey-rightleft-domain.json")
        journey.setActiveSegmentIndex(1)
        assertThat(journey.activeSegment().street()).isEqualTo("Link with A38")

        move(-3.33022, 50.92086)
        verify("Get ready to Turn right")
        move(-3.33019, 50.92081)
        verify("Turn right into A38. Continue 15m")
        move(-3.33019, 50.92081)
        move(-3.33019, 50.92081)
        verify("Get ready to Turn left")
        move(-3.33019, 50.92081)
        verify("Turn left into Broad Path. Continue 990m")
    }

    private fun loadJourneyFrom(domainJsonFile: String) {
        val rawJson = TestUtils.fromResourceFile(domainJsonFile)
        val routeData = RouteData(rawJson, null, "test route")
        Route.onNewJourney(routeData)
        journey = Route.journey()
    }

    private fun move(lon: Double, lat: Double) {
        liveRideState = liveRideState.update(journey, GeoPoint(lat, lon), 5)
    }

    private fun verify(expectedSpeech: String) {
        try {
            inOrder.verify(mockTts, times(1)).speak(eq(expectedSpeech), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
            inOrder.verifyNoMoreInteractions()
        } catch (e: VerificationInOrderFailure) {
            System.out.println(mockingDetails(mockTts).printInvocations())
            throw e
        }
    }
}
