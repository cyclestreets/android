package net.cyclestreets.liveride

import android.speech.tts.TextToSpeech
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.TestUtils
import net.cyclestreets.api.ApiClient
import net.cyclestreets.api.CycleStreetsApi
import net.cyclestreets.api.client.RetrofitApiClient
import net.cyclestreets.content.RouteData
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.view.BuildConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

@Config(constants = BuildConfig::class, manifest = Config.NONE, sdk = [27])
@RunWith(RobolectricTestRunner::class)
class ReplanFromHereTest {

    private lateinit var liveRideState: LiveRideState
    private lateinit var journey: Journey

    private val roboContext = ShadowApplication.getInstance().applicationContext
    private val mockTts = Mockito.mock(TextToSpeech::class.java)
    private val mockApiClient = Mockito.mock(CycleStreetsApi::class.java)

    @Before
    fun setUp() {
        CycleStreetsPreferences.initialise(roboContext, -1)
        ApiClient.initialiseForTests(roboContext, mockApiClient)
        Route.initialise(roboContext)

        liveRideState = LiveRideStart(roboContext, mockTts)
    }

    @Test
    fun remainingIntermediateWaypointsAreKeptWhenReplanning() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(13)
        assertThat(journey.activeSegment().street()).isEqualTo("lcn (unknown cycle network)")
        assertThat(journey.activeSegment().legNumber()).isEqualTo(1)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))

        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.13140, 52.22105, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)
    }

    @Test
    fun ifNoRemainingIntermediateWaypointsThenHeadForTheFinish() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(35)
        assertThat(journey.activeSegment().street()).isEqualTo("Pye Alley")
        assertThat(journey.activeSegment().legNumber()).isEqualTo(2)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))

        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)
    }

    @Test
    fun replanFromStartKeepsAllOriginalWaypoints() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(0)
        assertThat(journey.activeSegment().street()).isEqualTo("""test route
Quietest route : 6.25km
Journey time : 26 minutes""")
        assertThat(journey.activeSegment().legNumber()).isEqualTo(Int.MIN_VALUE)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))
        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.11783, 52.20530, 0.13140, 52.22105, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)
    }

    @Test
    fun replanFromWaymarkKeepsAllWaypointsFromTheCurrentOne() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(33)
        assertThat(journey.activeSegment().street()).isEqualTo("Waypoint 1")
        assertThat(journey.activeSegment().legNumber()).isEqualTo(1)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))
        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.13140, 52.22105, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)
    }

    @Test
    fun replanFromEndJustAimsForTheArrivee() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(journey.segments().count() - 1)
        assertThat(journey.activeSegment().street()).isEqualTo("Destination Thoday+Street")
        assertThat(journey.activeSegment().legNumber()).isEqualTo(Int.MAX_VALUE)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))
        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)
    }

    private fun loadJourneyFrom(domainJsonFile: String) {
        val rawJson = TestUtils.fromResourceFile(domainJsonFile)
        val routeData = RouteData(rawJson, null, "test route")
        Route.onNewJourney(routeData)
        journey = Route.journey()
    }
}
