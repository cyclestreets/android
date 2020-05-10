package net.cyclestreets.liveride

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class SpeechFixerTest {

    @Test
    fun testLiveRideAndArrivee() {
        assertThat(speechify("LiveRide will take us efficiently to the Arrivée"))
                .isEqualTo("Live Ride will take us efficiently to the arreev eh")
    }

    @Test
    fun testSimpleMetres() {
        assertThat(speechify("Straight on into lcn (unknown cycle network) continuation. Continue 5m"))
                .isEqualTo("Straight on into lcn (unknown cycle network) continuation. Continue 5 metres")
    }

    @Test
    fun testLotsOfMetresAndKilometres() {
        assertThat(speechify("Continue 600m, then 300km, another 10m, and finally 42km"))
                .isEqualTo("Continue 600 metres, then 300 kilometres, another 10 metres, and finally 42 kilometres")
    }

    @Test
    fun testNonIntegerKilometres() {
        assertThat(speechify("Continue 3.69km"))
                .isEqualTo("Continue 3.69 kilometres")
    }

}