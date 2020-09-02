package net.cyclestreets.api

import android.os.AsyncTask

class Maps(
        private val packs: Collection<VectorMap>
): Iterable<VectorMap> {
    val size get() = packs.size
    override operator fun iterator() = packs.iterator()

    companion object {
        private var loaded_: Maps? = null

        fun get(): Maps? {
            if (loaded_ == null)
                backgroundLoad()
            return loaded_
        } // get


        private fun backgroundLoad() {
            GetMapsTask().execute()
        }

        private class GetMapsTask : AsyncTask<Void?, Void?, Maps?>() {
            override fun doInBackground(vararg params: Void?): Maps? {
                return load()
            }

            override fun onPostExecute(maps: Maps?) {
                loaded_ = maps
            }
        }

        private fun load(): Maps? {
            try {
                return ApiClient.getMaps()
            } catch (e: Exception) {
                println(e.message)
            }
            return null
        }
    }
}