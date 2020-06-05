package net.cyclestreets.api

class Maps(
        private val packs: Collection<Map>
) {
    companion object {
        private var loaded_: Maps? = null

        fun get(): Maps? {
            if (loaded_ == null)
                loaded_ = load()
            return loaded_
        } // get

        private fun load(): Maps? {
            try {
                return ApiClient.getMaps()
            } catch (e: Exception) {
                // ah
            }
            return null
        }
    }
}