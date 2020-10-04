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

    @Test
    fun testOneAndTwoDigitRoadsUnaffected() {
        assertThat(speechify("The A45, the B61 and the A3 are not roads you'd want to cycle on. Nor is the A2"))
                .isEqualTo("The A45, the B61 and the A3 are not roads you'd want to cycle on. Nor is the A2")
    }

    @Test
    fun testThreeDigitRoads_Hundreds() {
        // Leave as-is, e.g. A400 => pronounced as "Eh Four Hundred"
        assertThat(speechify("Bear right onto A400"))
                .isEqualTo("Bear right onto A400")
    }

    @Test
    fun testThreeDigitRoads_Tens() {
        // Split into single digit then double, e.g. A230 => A2-30 => pronounced as "Eh Two Thirty"
        assertThat(speechify("Turn left onto A230. I hate the B450"))
                .isEqualTo("Turn left onto A2 30. I hate the B4 50")
    }

    @Test
    fun testThreeDigitRoads_MiddleZero() {
        // Split into single digits, and replace the 0 with an "Oh"
        assertThat(speechify("Turn left onto A404. Then hit the B303"))
                .isEqualTo("Turn left onto A4-oh 4. Then hit the B3-oh 3")
    }

    @Test
    fun testThreeDigitRoads_Other() {
        // Split into single digits, e.g. B467 => B4 6 7 => pronounced "Bee Four Six Seven"
        assertThat(speechify("Straight on onto B467. Continue 5.4 miles onto D512"))
                .isEqualTo("Straight on onto B4 6 7. Continue 5.4 miles onto D5 1 2")
    }

    @Test
    fun testFourDigitRoads_Thousand() {
        // Leave as-is, e.g. A3000 => pronounced as "Eh Three Thousand"
        assertThat(speechify("Straight on onto A3000. Continue 450 yards onto E1000"))
                .isEqualTo("Straight on onto A3000. Continue 450 yards onto E1000")
    }

    @Test
    fun testFourDigitRoads_Hundred() {
        // Split off the "hundred", e.g. B1200 => B12 hundred => pronounced as "Bee Twelve Hundred"
        assertThat(speechify("Straight on onto B1200. Continue 450 yards onto B2500"))
                .isEqualTo("Straight on onto B12-hundred. Continue 450 yards onto B25-hundred")
    }

    @Test
    fun testFourDigitRoads_TensAndTens() {
        // Tokenise into two two-digits numbers,
        assertThat(speechify("Straight on onto A4030. Continue 450 yards onto B2060"))
                .isEqualTo("Straight on onto A40 30. Continue 450 yards onto B20 60")
    }

    @Test
    fun testFourDigitRoads_TensAndDigits() {
        // Tokenise into two two-digits numbers,
        assertThat(speechify("Straight on onto A4021. Continue 450 yards onto B1036"))
                .isEqualTo("Straight on onto A40 21. Continue 450 yards onto B10 36")
    }

    @Test
    fun testFourDigitRoads_DigitsAndTens() {
        // Tokenise into two two-digits numbers,
        assertThat(speechify("Straight on onto A1450. Continue 450 yards onto B5620"))
                .isEqualTo("Straight on onto A14 50. Continue 450 yards onto B56 20")
    }

    @Test
    fun testFourDigitRoads_DoubleOhInMiddle() {
        // Pronounce a "double-oh" in the middle.
        assertThat(speechify("Straight on onto A5002. Continue 450 yards onto B5003"))
                .isEqualTo("Straight on onto A5-double-oh 2. Continue 450 yards onto B5-double-oh 3")
    }

    @Test
    fun testFourDigitRoads_WithTrebleNumbers() {
        // Capture trebles, e.g. B1113 => B-treble-1-3=> pronounced as "Bee Treble One Three"
        assertThat(speechify("Straight on onto A1113. Continue 450 yards onto B4111"))
                .isEqualTo("Straight on onto A-treble 1 3. Continue 450 yards onto B4-treble 1")
    }

    @Test
    fun testFourDigitRoads_ThirdDigitOh() {
        // Pronounce each digit individually, but 0 as "oh".
        assertThat(speechify("Straight on onto A2103. Continue 450 yards onto B4204"))
                .isEqualTo("Straight on onto A2 1-oh 3. Continue 450 yards onto B4 2-oh 4")
    }

    @Test
    fun testFourDigitRoads_Other() {
        // otherwise, split into single digits, e.g. A4123 => A4 1 2 3 => pronounced as "Eh Four One Two Three"
        assertThat(speechify("Straight on onto A4123. Continue 450 yards onto B5678"))
                .isEqualTo("Straight on onto A4 1 2 3. Continue 450 yards onto B5 6 7 8")
    }

    @Test
    fun testStSomebodyStreet() {
        assertThat(fixStreet("St Cross Road, B3335"))
                .isEqualTo("Saint Cross Road, B3335")
    }

    @Test
    fun testDestinationThodayStreet() {
        assertThat(fixStreet("Destination Thoday+Street"))
                .isEqualTo("Destination Thoday+Street")
    }

}
