package net.cyclestreets.liveride

private val REGEX_METRES = "(\\d+)m".toRegex()
private val REGEX_KILOMETRES = "(\\d+)km".toRegex()


fun speechify(words: String): String {

    var updatedWords = words
            .replace("LiveRide", "Live Ride")
            .replace(Arrivee.ARRIVEE, "arreev eh")

    updatedWords = REGEX_KILOMETRES.replace(updatedWords) {
        matchResult: MatchResult -> "${matchResult.groupValues[1]} kilometres"
    }
    updatedWords = REGEX_METRES.replace(updatedWords) {
        matchResult: MatchResult -> "${matchResult.groupValues[1]} metres"
    }

    return updatedWords;
}
