package net.cyclestreets.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.cyclestreets.api.Maps
import net.cyclestreets.api.VectorMap
import java.io.File
import java.io.FileInputStream
import java.io.FilenameFilter
import java.io.IOException
import java.util.*

class MapPack private constructor(
        private val vectorMap: VectorMap
) {
    val id get() = vectorMap.id
    val title get() = "${vectorMap.name}${if(downloaded) "" else " (Needs download)"}"
    val path get() = "none"
    val current get() = false
    val downloaded get() = false

    private class CycleStreetsMapFilter : FilenameFilter {
        override fun accept(dir: File, name: String): Boolean {
            return name.contains("net.cyclestreets.maps")
        }
    }

    companion object {
        @JvmStatic
        fun searchGooglePlay(context: Context) {
            val play = Intent(Intent.ACTION_VIEW)
            play.data = Uri.parse("market://search?q=net.cyclestreets")
            context.startActivity(play)
        }

        fun availableMapPacks(context: Context?): List<MapPack> {
            val maps = Maps.get() ?: return emptyList()

            return maps.map {m -> MapPack(m) }
        }

        @JvmStatic
        fun findByPackage(context: Context?, packageName: String?): MapPack? {
            for (pack in availableMapPacks(context)) if (pack.path.contains(packageName!!)) return pack
            return null
        }

        private fun findMapFile(mapDir: File, prefix: String): File? {
            for (c in mapDir.listFiles()) if (c.name.startsWith(prefix)) return c
            return null
        }

        private fun mapProperties(mapDir: File): Properties {
            val details = Properties()
            try {
                val detailsFile = findMapFile(mapDir, "patch.")
                details.load(FileInputStream(detailsFile))
            } catch (e: IOException) {
            } catch (e: RuntimeException) {
            }
            return details
        }
    }
}