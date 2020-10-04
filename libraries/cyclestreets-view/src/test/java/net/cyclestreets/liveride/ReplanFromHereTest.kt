package net.cyclestreets.liveride

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.databind.ObjectMapper
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.TestUtils
import net.cyclestreets.api.ApiClient
import net.cyclestreets.api.CycleStreetsApi
import net.cyclestreets.content.RouteData
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.osmdroid.util.GeoPoint
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper.shadowMainLooper


/**
 * The `shadowMainLooper().idle()` calls cause queued background async tasks to be run.
 * Replanning is an async operation so that's why we call this after every replan we trigger.
 *
 * See http://robolectric.org/blog/2019/06/04/paused-looper/ for context.
 */
@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE, sdk = [28])
class ReplanFromHereTest {

    private lateinit var liveRideState: LiveRideState
    private lateinit var journey: Journey

    private val roboContext = ApplicationProvider.getApplicationContext<Context>()
    private val mockTts = mock(TextToSpeech::class.java)
    private val mockApiClient = mock(CycleStreetsApi::class.java)

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
        assertThat(journey.activeSegment()!!.street()).isEqualTo("lcn (unknown cycle network)")
        assertThat(journey.activeSegment()!!.legNumber()).isEqualTo(1)

        val expectedWaypoints = doubleArrayOf(0.0, 52.0, 0.13140, 52.22105, 0.14744, 52.19962)
        val expectedJson = jsonFor(expectedWaypoints)
        `when`(mockApiClient.getJourneyJson("balanced", null, null, 20, expectedWaypoints)).thenReturn(expectedJson)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))
        shadowMainLooper().idle()

        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)

        // Check multi-replans don't keep adding waypoints
        liveRideState = ReplanFromHere(liveRideState, GeoPoint(53.0, 1.0))
        shadowMainLooper().idle()

        val newExpectedWaypoints: DoubleArray = doubleArrayOf(1.0, 53.0, 0.13140, 52.22105, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, newExpectedWaypoints)
    }

    @Test
    fun ifNoRemainingIntermediateWaypointsThenHeadForTheFinish() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(34)
        assertThat(journey.activeSegment()!!.street()).isEqualTo("Pye Alley")
        assertThat(journey.activeSegment()!!.legNumber()).isEqualTo(2)

        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.14744, 52.19962)
        val expectedJson = jsonFor(expectedWaypoints)
        `when`(mockApiClient.getJourneyJson("balanced", null, null, 20, expectedWaypoints)).thenReturn(expectedJson)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))
        shadowMainLooper().idle()

        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)

        // Check multi-replans don't keep adding waypoints
        liveRideState = ReplanFromHere(liveRideState, GeoPoint(53.0, 1.0))
        shadowMainLooper().idle()

        val newExpectedWaypoints: DoubleArray = doubleArrayOf(1.0, 53.0, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, newExpectedWaypoints)
    }

    @Test
    fun replanFromStartKeepsAllOriginalWaypoints() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(0)
        assertThat(journey.activeSegment()!!.street()).isEqualTo("""test route
Quietest route : 6.25km
Journey time : 26 minutes""")
        assertThat(journey.activeSegment()!!.legNumber()).isEqualTo(Int.MIN_VALUE)

        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.11783, 52.20530, 0.13140, 52.22105, 0.14744, 52.19962)
        val expectedJson = jsonFor(expectedWaypoints)
        `when`(mockApiClient.getJourneyJson("balanced", null, null, 20, expectedWaypoints)).thenReturn(expectedJson)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))
        shadowMainLooper().idle()

        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)

        // Check multi-replans don't keep adding waypoints
        liveRideState = ReplanFromHere(liveRideState, GeoPoint(53.0, 1.0))
        shadowMainLooper().idle()

        val newExpectedWaypoints: DoubleArray = doubleArrayOf(1.0, 53.0, 0.11783, 52.20530, 0.13140, 52.22105, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, newExpectedWaypoints)
    }

    @Test
    fun replanFromWaymarkKeepsAllWaypointsFromTheCurrentOne() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(32)
        assertThat(journey.activeSegment()!!.street()).isEqualTo("Waypoint 1")
        assertThat(journey.activeSegment()!!.legNumber()).isEqualTo(1)

        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.13140, 52.22105, 0.14744, 52.19962)
        val expectedJson = jsonFor(expectedWaypoints)
        `when`(mockApiClient.getJourneyJson("balanced", null, null, 20, expectedWaypoints)).thenReturn(expectedJson)

        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))
        shadowMainLooper().idle()

        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)

        // Check multi-replans don't keep adding waypoints
        liveRideState = ReplanFromHere(liveRideState, GeoPoint(53.0, 1.0))
        shadowMainLooper().idle()

        val newExpectedWaypoints: DoubleArray = doubleArrayOf(1.0, 53.0, 0.13140, 52.22105, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, newExpectedWaypoints)
    }

    @Test
    fun replanFromEndJustAimsForTheArrivee() {
        loadJourneyFrom("journey-domain.json")
        journey.setActiveSegmentIndex(journey.segments.count() - 1)
        assertThat(journey.activeSegment()!!.street()).isEqualTo("Destination Thoday+Street")
        assertThat(journey.activeSegment()!!.legNumber()).isEqualTo(Int.MAX_VALUE)

        val expectedWaypoints: DoubleArray = doubleArrayOf(0.0, 52.0, 0.14744, 52.19962)
        val expectedJson = jsonFor(expectedWaypoints)
        `when`(mockApiClient.getJourneyJson("balanced", null, null, 20, expectedWaypoints)).thenReturn(expectedJson)
        liveRideState = ReplanFromHere(liveRideState, GeoPoint(52.0, 0.0))
        shadowMainLooper().idle()

        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, expectedWaypoints)

        // Check multi-replans don't keep adding waypoints
        liveRideState = ReplanFromHere(liveRideState, GeoPoint(53.0, 1.0))
        shadowMainLooper().idle()

        val newExpectedWaypoints: DoubleArray = doubleArrayOf(1.0, 53.0, 0.14744, 52.19962)
        verify(mockApiClient, times(1)).getJourneyJson("balanced", null, null, 20, newExpectedWaypoints)
    }

    private fun loadJourneyFrom(domainJsonFile: String) {
        val rawJson = TestUtils.fromResourceFile(domainJsonFile)
        val routeData = RouteData(rawJson, null, "test route")
        Route.onNewJourney(routeData)
        journey = Route.journey()
    }

    // Create a dummy domain object - populated just enough that the waypoints are usable
    private fun jsonFor(waypoints: DoubleArray): String {
        val wpCount = (waypoints.size / 2)
        val wp = ArrayList<Map<String, String>>(wpCount)
        for (ii: Int in 0 until wpCount) {
            val lon = waypoints[ii * 2]
            val lat = waypoints[ii * 2 + 1]
            wp.add(mapOf("latitude" to lat.toString(), "longitude" to lon.toString()))
        }
        return jsonTemplate.format(ObjectMapper().writeValueAsString(wp))
    }
}

private val jsonTemplate = """{
  "waypoints": %s,
  "route": {
    "start": "City+Centre",
    "finish": "Thoday+Street",
    "startBearing": "0",
    "startSpeed": "0",
    "start_longitude": "0.11783",
    "start_latitude": "52.20530",
    "finish_longitude": "0.14744",
    "finish_latitude": "52.19962",
    "crow_fly_distance": "4603",
    "event": "depart",
    "whence": "1533118269",
    "speed": "24",
    "itinerary": "62909947",
    "clientRouteId": "0",
    "plan": "quietest",
    "note": "",
    "length": "6257",
    "time": "1584",
    "busynance": "8471",
    "quietness": "74",
    "signalledJunctions": "3",
    "signalledCrossings": "1",
    "west": "0.11781",
    "south": "52.19962",
    "east": "0.15055",
    "north": "52.22105",
    "name": "City+Centre to Thoday+Street",
    "walk": "1",
    "leaving": "2018-08-01 11:11:09",
    "arriving": "2018-08-01 11:37:33",
    "coordinates": "0.11783,52.20530 0.11781,52.20545 0.11786,52.20549 0.11793,52.20550 0.11844,52.20551 0.11858,52.20553 0.11871,52.20556 0.11890,52.20561 0.11909,52.20570 0.11923,52.20575 0.11960,52.20590 0.12000,52.20603 0.12030,52.20611 0.12044,52.20612 0.12055,52.20614 0.12061,52.20616 0.12062,52.20619 0.12060,52.20628 0.12048,52.20650 0.12047,52.20651 0.12004,52.20699 0.11953,52.20752 0.11936,52.20776 0.11932,52.20776 0.11928,52.20778 0.11918,52.20785 0.11864,52.20833 0.11857,52.20838 0.11851,52.20841 0.11848,52.20844 0.11848,52.20846 0.11849,52.20848 0.11852,52.20849 0.11859,52.20852 0.11871,52.20858 0.11892,52.20869 0.11916,52.20884 0.11927,52.20889 0.11956,52.20901 0.11970,52.20917 0.11963,52.20928 0.11959,52.20940 0.11957,52.20953 0.11956,52.20964 0.11957,52.20970 0.11958,52.20974 0.11975,52.20981 0.11989,52.20984 0.12008,52.20988 0.12024,52.20991 0.12051,52.20997 0.12070,52.21004 0.12097,52.21018 0.12104,52.21021 0.12105,52.21022 0.12155,52.21057 0.12193,52.21086 0.12219,52.21098 0.12360,52.21128 0.12448,52.21152 0.12515,52.21172 0.12634,52.21207 0.12643,52.21210 0.12769,52.21224 0.12774,52.21222 0.12777,52.21224 0.12785,52.21221 0.12802,52.21215 0.12814,52.21224 0.12859,52.21259 0.12867,52.21270 0.12869,52.21275 0.12870,52.21279 0.12871,52.21290 0.12872,52.21312 0.12870,52.21431 0.12841,52.21428 0.12819,52.21427 0.12807,52.21427 0.12806,52.21436 0.12808,52.21468 0.12807,52.21476 0.12801,52.21485 0.12839,52.21494 0.12836,52.21498 0.12872,52.21506 0.13059,52.21547 0.13055,52.21556 0.13036,52.21597 0.13030,52.21618 0.13029,52.21627 0.13031,52.21638 0.13034,52.21659 0.13031,52.21675 0.13027,52.21689 0.13013,52.21705 0.12954,52.21750 0.12946,52.21751 0.12919,52.21772 0.12935,52.21780 0.12927,52.21787 0.12921,52.21792 0.12951,52.21807 0.13028,52.21846 0.13036,52.21850 0.13065,52.21864 0.13145,52.21900 0.13157,52.21909 0.13160,52.21937 0.13173,52.21942 0.13185,52.21946 0.13217,52.21944 0.13234,52.21945 0.13247,52.21941 0.13261,52.21948 0.13284,52.21958 0.13313,52.21975 0.13324,52.21981 0.13281,52.22013 0.13261,52.22024 0.13217,52.22059 0.13197,52.22071 0.13140,52.22105 0.13197,52.22071 0.13217,52.22059 0.13261,52.22024 0.13281,52.22013 0.13324,52.21981 0.13356,52.22009 0.13365,52.22006 0.13374,52.22003 0.13398,52.21991 0.13416,52.21978 0.13472,52.21939 0.13473,52.21937 0.13496,52.21921 0.13507,52.21913 0.13508,52.21910 0.13545,52.21909 0.13556,52.21908 0.13559,52.21906 0.13565,52.21901 0.13567,52.21895 0.13572,52.21890 0.13583,52.21886 0.13592,52.21884 0.13631,52.21866 0.13647,52.21878 0.13680,52.21894 0.13691,52.21887 0.13707,52.21876 0.13739,52.21859 0.13747,52.21855 0.13756,52.21850 0.13773,52.21842 0.13810,52.21827 0.13841,52.21814 0.13868,52.21800 0.13935,52.21756 0.13958,52.21761 0.13977,52.21765 0.14022,52.21729 0.14052,52.21702 0.14059,52.21692 0.14058,52.21685 0.14052,52.21663 0.14047,52.21651 0.14042,52.21641 0.14025,52.21621 0.14010,52.21606 0.13986,52.21581 0.14013,52.21565 0.14044,52.21545 0.14048,52.21543 0.14056,52.21538 0.14062,52.21532 0.14067,52.21522 0.14069,52.21513 0.14067,52.21505 0.14097,52.21504 0.14154,52.21472 0.14158,52.21467 0.14159,52.21464 0.14159,52.21458 0.14158,52.21452 0.14159,52.21445 0.14163,52.21441 0.14194,52.21419 0.14205,52.21412 0.14256,52.21388 0.14268,52.21381 0.14281,52.21377 0.14286,52.21376 0.14295,52.21372 0.14303,52.21366 0.14306,52.21361 0.14308,52.21355 0.14307,52.21349 0.14301,52.21339 0.14294,52.21326 0.14285,52.21310 0.14279,52.21298 0.14270,52.21282 0.14269,52.21274 0.14253,52.21251 0.14258,52.21247 0.14260,52.21238 0.14268,52.21238 0.14275,52.21236 0.14291,52.21231 0.14311,52.21224 0.14327,52.21221 0.14357,52.21216 0.14251,52.21125 0.14319,52.21094 0.14337,52.21085 0.14339,52.21082 0.14341,52.21079 0.14340,52.21074 0.14341,52.21067 0.14350,52.21050 0.14363,52.21044 0.14382,52.21035 0.14412,52.21024 0.14456,52.20990 0.14472,52.20997 0.14481,52.20990 0.14500,52.20976 0.14515,52.20966 0.14524,52.20962 0.14531,52.20959 0.14546,52.20955 0.14578,52.20948 0.14590,52.20945 0.14598,52.20943 0.14595,52.20934 0.14592,52.20924 0.14585,52.20913 0.14577,52.20900 0.14569,52.20886 0.14565,52.20880 0.14561,52.20873 0.14557,52.20866 0.14556,52.20863 0.14553,52.20858 0.14545,52.20845 0.14541,52.20839 0.14539,52.20833 0.14536,52.20820 0.14529,52.20808 0.14512,52.20794 0.14520,52.20791 0.14518,52.20786 0.14521,52.20782 0.14527,52.20777 0.14578,52.20760 0.14574,52.20757 0.14569,52.20753 0.14589,52.20744 0.14619,52.20733 0.14637,52.20727 0.14784,52.20686 0.14807,52.20680 0.14845,52.20674 0.14873,52.20671 0.14906,52.20668 0.14926,52.20664 0.14955,52.20654 0.14960,52.20649 0.14963,52.20637 0.14970,52.20636 0.14980,52.20636 0.14983,52.20634 0.14989,52.20636 0.14998,52.20628 0.15003,52.20620 0.15055,52.20526 0.15046,52.20525 0.15028,52.20521 0.15009,52.20516 0.14999,52.20509 0.14991,52.20502 0.14987,52.20497 0.14840,52.20293 0.14830,52.20277 0.14827,52.20272 0.14826,52.20264 0.14828,52.20255 0.14833,52.20240 0.14839,52.20219 0.14837,52.20212 0.14858,52.20203 0.14852,52.20198 0.14849,52.20193 0.14849,52.20187 0.14856,52.20163 0.14856,52.20148 0.14854,52.20140 0.14850,52.20133 0.14824,52.20091 0.14812,52.20072 0.14786,52.20029 0.14771,52.20004 0.14748,52.19967 0.14744,52.19962",
    "elevations": "9,9,9,9,9,9,9,9,9,9,9,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,9,8,8,8,8,8,8,8,8,8,8,8,8,7,7,7,7,7,7,7,7,6,6,6,6,6,6,5,5,5,5,5,5,5,5,5,5,4,4,4,4,4,4,5,5,5,5,5,5,7,6,6,6,6,7,7,7,7,7,7,7,8,8,8,8,8,8,9,9,9,9,9,10,10,10,10,10,10,10,10,9,9,10,10,10,10,9,9,9,9,9,9,10,10,10,10,10,10,10,10,10,9,9,9,9,9,9,8,8,8,8,8,8,8,8,8,8,8,8,8,7,7,8,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,6,6,6,6,6,6,6,5,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,6,6,6,8,9,9,11,10,11,11,11,11,11,11,11,11,11,11,11,11,11,10,10,10,9,9,9,9,8,8,8,9,8,8,8,8,8,8,8,9,9,9,9,9,10,10,10,10,10,9,10,10,10,10,10,9,8,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,10,10,10,10,10,11,11,11,11,11,11,11,11,11,11,11,12,12,13,13,13,14",
    "distances": "17,6,5,35,10,9,14,16,11,30,31,22,10,8,5,3,10,26,1,61,68,29,3,4,10,65,7,5,4,2,2,2,6,11,19,23,9,24,20,13,14,15,12,7,5,14,10,14,11,20,15,24,6,1,52,41,22,102,66,51,90,7,87,4,3,6,13,13,50,13,6,5,12,24,132,20,15,8,10,36,9,11,28,5,26,135,10,47,24,10,12,23,18,16,20,64,6,30,14,10,7,26,68,7,25,68,13,31,10,9,22,12,10,12,19,27,10,46,18,49,19,54,54,19,49,18,46,38,7,7,21,19,58,2,24,12,3,25,8,3,7,7,7,9,7,33,17,29,11,16,29,7,8,15,30,26,24,67,17,14,50,36,12,8,25,14,12,25,20,32,26,31,4,8,8,12,10,9,20,53,6,3,7,7,8,5,32,11,44,11,10,4,8,9,6,7,7,12,15,19,14,19,9,28,6,10,5,5,12,16,11,21,124,58,16,4,4,6,8,20,11,16,24,48,13,10,20,15,8,6,11,23,9,6,10,11,13,15,16,7,8,8,3,6,15,7,7,15,14,19,6,6,5,7,40,4,6,17,24,14,110,17,27,19,23,14,23,7,14,5,7,3,5,11,10,110,6,13,14,10,10,6,248,19,6,9,10,17,24,8,17,7,6,7,27,17,9,8,50,23,51,30,44,6",
    "grammesCO2saved": "1166",
    "calories": "116",
    "edition": "routing180716",
    "type": "route"
  },
  "segments": [
    {
      "name": "St Mary's Street, NCN 11",
      "legNumber": "1",
      "distance": "54",
      "time": "52",
      "busynance": "206",
      "flow": "against",
      "walk": "1",
      "signalledJunctions": "0",
      "signalledCrossings": "0",
      "turn": "straight on",
      "startBearing": "88",
      "color": "#aaaacc",
      "points": "0.11793,52.20550 0.11844,52.20551 0.11858,52.20553 0.11871,52.20556",
      "distances": "0,35,10,9",
      "elevations": "9,9,9,9",
      "provisionName": "Pedestrianized area",
      "type": "segment"
    }
  ]
}""".trimIndent()
