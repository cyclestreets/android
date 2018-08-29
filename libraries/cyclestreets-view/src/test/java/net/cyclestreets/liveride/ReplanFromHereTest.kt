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

    private fun loadJourneyFrom(domainJsonFile: String) {
        val rawJson = TestUtils.fromResourceFile(domainJsonFile)
        val routeData = RouteData(rawJson, null, "test route")
        Route.onNewJourney(routeData)
        journey = Route.journey()
    }
}
