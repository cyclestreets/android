package net.cyclestreets.util

import android.app.DownloadManager
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
        private val vectorMap: VectorMap,
        context: Context
) {
    val id get() = vectorMap.id
    val title get() = "${vectorMap.name}${if(downloaded) "" else " (Needs download)"}"
    val path = File(context.getExternalFilesDir(null), "${vectorMap.id}.map").absolutePath
    val downloaded get() = File(path).exists()

    fun download(context: Context) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(
                Uri.parse(vectorMap.url)
        )
        request.setTitle("${vectorMap.name} Map Pack")
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setDestinationInExternalFilesDir(context, null, "${vectorMap.id}.map")

        dm.enqueue(request)
    } // download

    companion object {
        fun availableMapPacks(context: Context): List<MapPack> {
            val maps = Maps.get() ?: return emptyList()

            return maps.map { m -> MapPack(m, context) }
        }

        @JvmStatic
        fun findById(context: Context, packId: String): MapPack? {
            return availableMapPacks(context).find { it.id == packId }
        }
    }
}