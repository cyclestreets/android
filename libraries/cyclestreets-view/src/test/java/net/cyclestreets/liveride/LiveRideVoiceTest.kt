package net.cyclestreets.liveride

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.TestUtils
import net.cyclestreets.content.RouteData
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.util.TurnIcons
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.exceptions.verification.VerificationInOrderFailure
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@Config(manifest = Config.NONE, sdk = [28])
@RunWith(RobolectricTestRunner::class)
class LiveRideVoiceTest {

    private lateinit var liveRideState: LiveRideState
    private lateinit var journey: Journey

    private val roboContext = ApplicationProvider.getApplicationContext<Context>()
    private val mockTts = mock(TextToSpeech::class.java)
    private val inOrder = inOrder(mockTts)

    @Before
    fun setUp() {
        CycleStreetsPreferences.initialise(roboContext, -1)
        TurnIcons.initialise(roboContext)

        liveRideState = LiveRideStart(roboContext, mockTts)
        liveRideState = OnTheMove(liveRideState)

        Route.initialise(roboContext)
    }

    @Test
    fun doubleStraightOn() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(13)
        assertThat(journey.activeSegment()!!.street()).isEqualTo("lcn (unknown cycle network)")

        move(0.126, 52.212)
        verify("Get ready to Straight on into lcn (unknown cycle network) continuation")
        move(0.12634, 52.21207)
        verify("Straight on into lcn (unknown cycle network) continuation. Continue 5 metres")
        move(0.12634, 52.21207)
        // AdvanceToSegment advances the segment; we need another location update to trigger the next NearingTurn
        move(0.12634, 52.21207)
        verify("Get ready to Straight on into lcn (unknown cycle network)")
        move(0.12634, 52.21207)
        verify("Straight on into lcn (unknown cycle network). Continue 85 metres")
    }

    @Test
    fun arrivee() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(journey.segments.count() - 2)
        assertThat(journey.activeSegment()!!.street()).isEqualTo("Thoday Street")

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
        assertThat(journey.activeSegment()!!.street()).isEqualTo("Link with A38")

        move(-3.33022, 50.92086)
        verify("Get ready to Turn right then turn left into Broad Path")
        move(-3.33019, 50.92081)
        verify("Turn right then turn left into Broad Path. Continue 1000 metres")
    }

    @Test
    fun bearLeftThenRight() {
        loadJourneyFrom("journey-bearleftright-domain.json")
        journey.setActiveSegmentIndex(2)
        assertThat(journey.activeSegment()!!.street()).isEqualTo("London Road, A413")

        move(-0.56665, 51.63393)
        verify("Get ready to Bear left then bear right into London Road, A4 1 3")
        move(-0.56667, 51.63401)
        verify("Bear left then bear right into London Road, A4 1 3. Continue 1330 metres")
    }

    @Test
    fun overBridge() {
        loadJourneyFrom("journey-overbridge-domain.json")
        journey.setActiveSegmentIndex(1)
        assertThat(journey.activeSegment()!!.street()).isEqualTo("Link with B3390")

        move(-2.26018, 50.74097)
        verify("Get ready to Straight on over Bridge into Short unnamed link")
        move(-2.26058, 50.74058)
        verify("Straight on over Bridge into Short unnamed link. Continue 95 metres")
        move(-2.2612, 50.74025)
        move(-2.2612, 50.74025)
        verify("Get ready to Bear left over Bridge into Link with The Hollow")
        move(-2.26148, 50.73993)
        verify("Bear left over Bridge into Link with The Hollow. Continue 195 metres")
    }

    @Test
    fun longWindedRoadNameIsShortened() {
        loadJourneyFrom("journey-longname-domain.json")
        journey.setActiveSegmentIndex(14)
        // Link joining Pedestrian Area, Link between Charlton Way, B210 and Pedestrian Area, Link joining Long Pond Road, General Wolfe Road, Pedestrian Area, Charlton Way, B210"
        assertThat(journey.activeSegment()!!.street()).isEqualTo("Link joining Pedestrian Area and other streets")
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
            inOrder.verify(mockTts, times(1)).speak(eq(expectedSpeech), eq(TextToSpeech.QUEUE_ADD), isNull(), anyString())
            inOrder.verifyNoMoreInteractions()
        } catch (e: VerificationInOrderFailure) {
            System.out.println(mockingDetails(mockTts).printInvocations())
            throw e
        }
    }
}
