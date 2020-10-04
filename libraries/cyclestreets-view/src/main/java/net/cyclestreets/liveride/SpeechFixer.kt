package net.cyclestreets.liveride

private val REGEX_METRES = "(\\d+)m".toRegex()
private val REGEX_KILOMETRES = "(\\d+)km".toRegex()

private val REGEX_METRIC_DISTANCES: Map<Regex, (MatchResult) -> String> = mapOf(
    REGEX_METRES     to { m -> "${m.groupValues[1]} metres" },
    REGEX_KILOMETRES to { m -> "${m.groupValues[1]} kilometres" }
)

private val REGEX_3_DIGIT_TENS = "([a-zA-Z])(\\d)([1-9]0)(\\D|\$)".toRegex()         // e.g. A230
private val REGEX_3_DIGIT_MIDDLE_ZERO = "([a-zA-Z])(\\d)0([1-9])(\\D|\$)".toRegex()  // e.g. A404
private val REGEX_3_DIGIT_OTHER = "([a-zA-Z])(\\d)([1-9])([1-9])(\\D|\$)".toRegex()  // e.g. A467

private val REGEX_3_DIGIT_ROADS: Map<Regex, (MatchResult) -> String> = mapOf(
    REGEX_3_DIGIT_TENS        to { m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]}${m.groupValues[4]}" },
    REGEX_3_DIGIT_MIDDLE_ZERO to { m -> "${m.groupValues[1]}${m.groupValues[2]}-oh ${m.groupValues[3]}${m.groupValues[4]}" },
    REGEX_3_DIGIT_OTHER       to { m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]} ${m.groupValues[4]}${m.groupValues[5]}" }
)

private val REGEX_4_DIGIT_HUNDREDS = "([a-zA-Z])(\\d[1-9])00(\\D|\$)".toRegex()               // e.g. B1200
private val REGEX_4_DIGIT_TENS_AND_TENS = "([a-zA-Z])(\\d0)([1-9]0)(\\D|\$)".toRegex()        // e.g. A4030
private val REGEX_4_DIGIT_DIGITS_AND_TENS = "([a-zA-Z])(\\d[1-9])([1-9]0)(\\D|\$)".toRegex()  // e.g. A4130
private val REGEX_4_DIGIT_TENS_AND_DIGITS = "([a-zA-Z])(\\d0)([1-9][1-9])(\\D|\$)".toRegex()  // e.g. A4032
private val REGEX_4_DIGIT_MIDDLE_ZEROES = "([a-zA-Z])(\\d)00([1-9])(\\D|\$)".toRegex()        // e.g. B5001
private val REGEX_4_DIGIT_TREBLE_AT_START = "([a-zA-Z])(\\d)\\2\\2(\\d)(\\D|\$)".toRegex()    // e.g. B1113
private val REGEX_4_DIGIT_TREBLE_AT_END = "([a-zA-Z])(\\d)([1-9])\\3\\3(\\D|\$)".toRegex()    // e.g. A2111
private val REGEX_4_DIGIT_THIRD_DIGIT_0 = "([a-zA-Z])(\\d)([1-9])0([1-9])(\\D|\$)".toRegex()  // e.g. A2103
private val REGEX_4_DIGIT_OTHER = "([a-zA-Z])(\\d)([1-9])([1-9])([1-9])(\\D|\$)".toRegex()    // e.g. A4123

private val REGEX_4_DIGIT_ROADS: Map<Regex, (MatchResult) -> String> = mapOf(
    REGEX_4_DIGIT_HUNDREDS        to { m -> "${m.groupValues[1]}${m.groupValues[2]}-hundred${m.groupValues[3]}" },
    REGEX_4_DIGIT_MIDDLE_ZEROES   to { m -> "${m.groupValues[1]}${m.groupValues[2]}-double-oh ${m.groupValues[3]}${m.groupValues[4]}" },
    REGEX_4_DIGIT_TENS_AND_TENS   to { m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]}${m.groupValues[4]}" },
    REGEX_4_DIGIT_DIGITS_AND_TENS to { m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]}${m.groupValues[4]}" },
    REGEX_4_DIGIT_TENS_AND_DIGITS to { m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]}${m.groupValues[4]}" },
    REGEX_4_DIGIT_TREBLE_AT_START to { m -> "${m.groupValues[1]}-treble ${m.groupValues[2]} ${m.groupValues[3]}${m.groupValues[4]}" },
    REGEX_4_DIGIT_TREBLE_AT_END   to { m -> "${m.groupValues[1]}${m.groupValues[2]}-treble ${m.groupValues[3]}${m.groupValues[4]}" },
    REGEX_4_DIGIT_THIRD_DIGIT_0   to { m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]}-oh ${m.groupValues[4]}${m.groupValues[5]}" },
    REGEX_4_DIGIT_OTHER           to { m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]} ${m.groupValues[4]} ${m.groupValues[5]}${m.groupValues[6]}" }
)

/**
 * This rather complex helper method manipulates the Android speech engine so instead of saying e.g.
 * "Bee four thousand one hundred and twenty three" it will correctly call the road "B4123", as
 * humans would.
 *
 * To live test, edit LiveRideState and substitute something like the following for the input to the `speechify()` method.
 * val testWords = "Testing... B3000 and A200 and A230 and A404 and A467 and B1200 and A4030.  New: A4130 and A4032.  Then B5001 and B1113 and A2111 and A2103 and A4123"
 */
fun speechify(words: String): String {

    var updatedWords = words
            .replace("LiveRide", "Live Ride")
            .replace(Arrivee.ARRIVEE, "arreev eh")

    for (entry in REGEX_METRIC_DISTANCES) {
        updatedWords = entry.key.replace(updatedWords, entry.value)
    }

    for (entry in REGEX_3_DIGIT_ROADS) {
        updatedWords = entry.key.replace(updatedWords, entry.value)
    }

    for (entry in REGEX_4_DIGIT_ROADS) {
        updatedWords = entry.key.replace(updatedWords, entry.value)
    }

    return updatedWords;
}


fun fixStreet(streetWords: String): String {

    // handling "un-named link" etc
    var updatedWords = streetWords
            .replace("un-", "un")
            .replace("Un-", "un")

    // some Android speech engines get this right; others don't (https://github.com/cyclestreets/android/issues/442)
    val wordsList = updatedWords.split(" ")
    if (wordsList.size > 1 && setOf("St.", "st.", "St", "st").contains(wordsList.first())) {
        updatedWords = "Saint ${wordsList.subList(1, wordsList.size).joinToString(" ")}";
    }

    return updatedWords;
}
