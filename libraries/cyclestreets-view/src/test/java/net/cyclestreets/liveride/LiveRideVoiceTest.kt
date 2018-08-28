package net.cyclestreets.liveride

import android.speech.tts.TextToSpeech
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.TestUtils
import net.cyclestreets.content.RouteData
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.util.TurnIcons
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
class LiveRideVoiceTest {

    private lateinit var liveRideState: LiveRideState
    private lateinit var journey: Journey

    private val roboContext = ShadowApplication.getInstance().applicationContext
    private val mockTts = mock(TextToSpeech::class.java)
    private val inOrder = Mockito.inOrder(mockTts)

    @Before
    fun setUp() {
        CycleStreetsPreferences.initialise(roboContext, -1)
        TurnIcons.initialise(roboContext)

        liveRideState = LiveRideStart(roboContext, mockTts)
        liveRideState = OnTheMove(liveRideState)
        verify("Starting Lyve Ride")

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
    fun arrivee() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(66)
        assertThat(journey.activeSegment().street()).isEqualTo("Thoday Street")

        move(0.14748, 52.19967)
        verify("You are approaching the arreev eh")
        move(0.14744, 52.19962)
        verify("Destination Thoday+Street")
        move(0.14744, 52.19962)
        verify("arreev eh")
    }

    @Test
    fun rightThenLeft() {
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

    @Test
    fun overBridge() {
        loadJourneyFrom("journey-overbridge-domain.json")
        journey.setActiveSegmentIndex(1)
        assertThat(journey.activeSegment().street()).isEqualTo("Link with B3390")

        move(-2.26018, 50.74097)
        verify("Get ready to Straight on")
        move(-2.26058, 50.74058)
        verify("Straight on into Bridge. Continue 20m")
        move(-2.26058, 50.74058)
        move(-2.26058, 50.74058)
        verify("Get ready to Straight on")
        move(-2.26085, 50.74045)
        verify("Straight on into Short unnamed link. Continue 70m")
        move(-2.2612, 50.74025)
        move(-2.2612, 50.74025)
        verify("Get ready to Bear left")
        move(-2.26148, 50.73993)
        verify("Bear left into Bridge. Continue 60m")
        move(-2.26146, 50.7396)
        move(-2.26146, 50.7396)
        verify("Get ready to Straight on")
        move(-2.26138, 50.73938)
        verify("Straight on into Link with The Hollow. Continue 130m")
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
