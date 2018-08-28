package net.cyclestreets.util

enum class Turn(val textInstruction: String) {
    STRAIGHT_ON("straight on"),
    BEAR_LEFT("bear left"),
    TURN_LEFT("turn left"),
    SHARP_LEFT("sharp left"),
    BEAR_RIGHT("bear right"),
    TURN_RIGHT("turn right"),
    SHARP_RIGHT("sharp right"),
    TURN_LEFT_THEN_RIGHT("turn left then turn right"),
    TURN_RIGHT_THEN_LEFT("turn right then turn left"),
    BEAR_LEFT_THEN_RIGHT("bear left then bear right"),
    BEAR_RIGHT_THEN_LEFT("bear right then bear left"),
    DOUBLE_BACK("double-back"),
    JOIN_ROUNDABOUT("join roundabout"),
    FIRST_EXIT("first exit"),
    SECOND_EXIT("second exit"),
    THIRD_EXIT("third exit"),
    WAYMARK("waymark"),
    DEFAULT("default");

    companion object {
        private val mapping: Map<String, Turn> = Turn.values().associateBy(Turn::textInstruction);

        @JvmStatic
        fun turnFor(turn: String): Turn {
            return mapping[turn.toLowerCase()] ?: DEFAULT
        }
    }
}
