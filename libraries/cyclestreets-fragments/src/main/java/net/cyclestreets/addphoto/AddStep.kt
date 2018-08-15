package net.cyclestreets.addphoto

internal enum class AddStep(val id: Int, val previous: AddStep?) {
    START(1, null),
    CAPTION(2, START),
    CATEGORY(3, CAPTION),
    LOCATION(4, CATEGORY),
    VIEW(5, LOCATION),
    DONE(6, VIEW);

    var next: AddStep? = null

    companion object {
        private val map: Map<Int, AddStep> = AddStep.values().associateBy(AddStep::id);
        fun fromId(type: Int) = map[type]
    }

    init {
        if (previous != null)
            previous.next = this
    }
}
