package net.cyclestreets.liveride

private val REGEX_METRES = "(\\d+)m".toRegex()
private val REGEX_KILOMETRES = "(\\d+)km".toRegex()

private val REGEX_THREE_DIGIT_TENS = "([a-zA-Z])(\\d)([1-9]0)(\\D|\$)".toRegex()         // e.g. A230
private val REGEX_THREE_DIGIT_MIDDLE_ZERO= "([a-zA-Z])(\\d)0([1-9])(\\D|\$)".toRegex()   // e.g. A404
private val REGEX_THREE_DIGIT_OTHER= "([a-zA-Z])(\\d)([1-9])([1-9])(\\D|\$)".toRegex()  // e.g. A467

private val REGEX_FOUR_DIGIT_HUNDREDS = "([a-zA-Z])(\\d[1-9])00(\\D|\$)".toRegex()            // e.g. B1200
private val REGEX_FOUR_DIGIT_TENS_AND_TENS = "([a-zA-Z])(\\d0)([1-9]0)(\\D|\$)".toRegex()     // e.g. A4030
private val REGEX_FOUR_DIGIT_MIDDLE_ZEROES = "([a-zA-Z])(\\d)00([1-9])(\\D|\$)".toRegex()     // e.g. B5001
private val REGEX_FOUR_DIGIT_TREBLE_AT_START = "([a-zA-Z])(\\d)\\2\\2(\\d)(\\D|\$)".toRegex() // e.g. B1113
private val REGEX_FOUR_DIGIT_TREBLE_AT_END = "([a-zA-Z])(\\d)([1-9])\\3\\3(\\D|\$)".toRegex() // e.g. A2111
private val REGEX_FOUR_DIGIT_THIRD_DIGIT_0 = "([a-zA-Z])(\\d)([1-9])0([1-9])(\\D|\$)".toRegex() // e.g. A2103
private val REGEX_FOUR_DIGIT_OTHER = "([a-zA-Z])(\\d)([1-9])([1-9])([1-9])(\\D|\$)".toRegex() // e.g. A4123

fun speechify(words: String): String {

    var updatedWords = words
            .replace("LiveRide", "Live Ride")
            .replace(Arrivee.ARRIVEE, "arreev eh")

    updatedWords = REGEX_KILOMETRES.replace(updatedWords) {
        m -> "${m.groupValues[1]} kilometres"
    }
    updatedWords = REGEX_METRES.replace(updatedWords) {
        m -> "${m.groupValues[1]} metres"
    }

    updatedWords = REGEX_THREE_DIGIT_TENS.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]}${m.groupValues[4]}"
    }
    updatedWords = REGEX_THREE_DIGIT_MIDDLE_ZERO.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]}-oh ${m.groupValues[3]}${m.groupValues[4]}"
    }
    updatedWords = REGEX_THREE_DIGIT_OTHER.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]} ${m.groupValues[4]}${m.groupValues[5]}"
    }

    updatedWords = REGEX_FOUR_DIGIT_HUNDREDS.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]}-hundred${m.groupValues[3]}"
    }
    updatedWords = REGEX_FOUR_DIGIT_MIDDLE_ZEROES.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]}-double-oh ${m.groupValues[3]}${m.groupValues[4]}"
    }
    updatedWords = REGEX_FOUR_DIGIT_TENS_AND_TENS.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]}${m.groupValues[4]}"
    }
    updatedWords = REGEX_FOUR_DIGIT_TREBLE_AT_START.replace(updatedWords) {
        m -> "${m.groupValues[1]}-treble ${m.groupValues[2]} ${m.groupValues[3]}${m.groupValues[4]}"
    }
    updatedWords = REGEX_FOUR_DIGIT_TREBLE_AT_END.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]}-treble ${m.groupValues[3]}${m.groupValues[4]}"
    }
    updatedWords = REGEX_FOUR_DIGIT_THIRD_DIGIT_0.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]}-oh ${m.groupValues[4]}${m.groupValues[5]}"
    }
    updatedWords = REGEX_FOUR_DIGIT_OTHER.replace(updatedWords) {
        m -> "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[3]} ${m.groupValues[4]} ${m.groupValues[5]}${m.groupValues[6]}"
    }

    return updatedWords;
}
