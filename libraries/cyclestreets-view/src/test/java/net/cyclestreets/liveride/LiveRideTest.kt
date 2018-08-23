package net.cyclestreets.liveride

import android.app.Notification
import android.speech.tts.TextToSpeech
import net.cyclestreets.CycleStreetsNotifications
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

@Config(constants = BuildConfig::class, manifest = Config.NONE, sdk = [27])
@RunWith(RobolectricTestRunner::class)
class LiveRideTest {

    private lateinit var liveRideState: LiveRideState
    private lateinit var journey: Journey

    private val roboContext = ShadowApplication.getInstance().applicationContext
    private val mockTts = mock(TextToSpeech::class.java)
    private val inOrder = Mockito.inOrder(mockTts)
    private val mockCycleStreetsNotifications = mock(CycleStreetsNotifications::class.java)
    private val mockNotificationBuilder = mock(Notification.Builder::class.java)
    private val mockNotification = mock(Notification::class.java)

    @Before
    fun setUp() {
        setUpMocks()
        CycleStreetsPreferences.initialise(roboContext, -1)

        liveRideState = LiveRideStart(roboContext, mockTts, mockCycleStreetsNotifications)
        liveRideState = OnTheMove(liveRideState)
        inOrder.verify(mockTts, times(1)).speak(eq("Starting Live Ride"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())

        val rawJson = TestUtils.fromResourceFile("journey-domain.json")
        //journey = Journey.loadFromJson(rawJson, null, "test route")
        val routeData: RouteData = RouteData(rawJson, null, "test route")
        Route.initialise(roboContext)
        Route.onNewJourney(routeData)
        journey = Route.journey()
    }

    private fun setUpMocks() {
        `when`(mockCycleStreetsNotifications.builder).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setSmallIcon(anyInt())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setTicker(anyString())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setWhen(anyLong())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setAutoCancel(anyBoolean())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setOngoing(anyBoolean())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setContentTitle(any())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setContentText(any())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.setContentIntent(any())).thenReturn(mockNotificationBuilder)
        `when`(mockNotificationBuilder.build()).thenReturn(mockNotification)
    }

    @Test
    fun doubleStraightOn() {
        journey.setActiveSegmentIndex(13)
        assertThat(journey.activeSegment().street()).isEqualTo("lcn (unknown cycle network)")
        move(0.126, 52.212)
        inOrder.verify(mockTts, times(1)).speak(eq("Get ready to Straight on"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
        move(0.12634, 52.21207)
        inOrder.verify(mockTts, times(1)).speak(eq("Straight on into lcn (unknown cycle network) continuation. Continue 5m"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
        move(0.12634, 52.21207)
        // AdvanceToSegment advances the segment; we need another location update to trigger the next NearingTurn
        move(0.12634, 52.21207)
        inOrder.verify(mockTts, times(1)).speak(eq("Get ready to Straight on"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
        move(0.12634, 52.21207)
        inOrder.verify(mockTts, times(1)).speak(eq("Straight on into lcn (unknown cycle network). Continue 85m"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
    }

    @Test
    fun turnIssues() {
        journey.setActiveSegmentIndex(16)
        assertThat(journey.activeSegment().street()).isEqualTo("NCN 11")
        move(0.1277, 52.21223)
        inOrder.verify(mockTts, times(1)).speak(eq("Get ready to Turn left"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
        move(0.12774, 52.21222)
        inOrder.verify(mockTts, times(1)).speak(eq("Turn left into Short unnamed link. Continue 0m"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
        move(0.12774, 52.21222)
        // AdvanceToSegment advances the segment; we need another location update to trigger the next NearingTurn
        move(0.12774, 52.21222)
        inOrder.verify(mockTts, times(1)).speak(eq("Get ready to Turn right"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
        move(0.12774, 52.21222)
        inOrder.verify(mockTts, times(1)).speak(eq("Turn right into Short unnamed link. Continue 30m"), eq(TextToSpeech.QUEUE_ADD), isNull(), isNull())
//        System.out.println(mockingDetails(mockTts).printInvocations());
    }

    private fun move(lon: Double, lat: Double) {
        liveRideState = liveRideState.update(journey, GeoPoint(lat, lon), 5)
    }

}
