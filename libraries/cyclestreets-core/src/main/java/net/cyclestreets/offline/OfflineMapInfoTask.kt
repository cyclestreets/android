package net.cyclestreets.offline

import android.content.Context
import android.util.Log
import net.cyclestreets.util.Logging
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*

//TODO: save stuff into a DB table, or a serialized String preference?

private val TAG = Logging.getTag(OfflineMapInfoTask::class.java)

private val apacheUrls = listOf("http://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/v4/europe/")
private val regex = Regex("""<tr>.*?<a href="([^"]*?\.map)">.*?(\d{4}-\d{2}-\d{2}).*?<td align="right">\s*([\d\.]*[MGK])</td>.*?</tr>""")

internal fun parse(apacheIndexPage: String): Sequence<MatchResult.Destructured> {
    return regex.findAll(apacheIndexPage).map { r -> r.destructured }
}

internal fun offlineMapFor(apacheUrl: String, dmr: MatchResult.Destructured): OfflineMap {
    return OfflineMap(supportedMaps[dmr.component1()] ?: dmr.component1().dropLast(4).capitalize(),
                      "$apacheUrl${dmr.component1()}",
                      dmr.component2(),
                      getMb(dmr.component3()))
}

private fun getMb(size: String): Int {
    val amount = size.dropLast(1).toFloat()
    return when (size.last()) {
        'K' -> 1
        'G' -> Math.round(amount * 1024)
        'M' -> Math.round (amount)
        else -> -1
    }
}

class OfflineMapInfoTask(private val context: Context) : TimerTask() {
    override fun run() {
        val fred: List<OfflineMap> = apacheUrls.flatMap { url -> offlineMapsAt(url) }

    }

    private fun offlineMapsAt(apacheUrl: String): List<OfflineMap> {
        val apacheIndexPage = restGet(apacheUrl)
        return parse(apacheIndexPage)
                .filter { e -> e.component1() in supportedMaps }
                .map { e ->  offlineMapFor(apacheUrl, e)}
                .toList()
    }

    private fun restGet(url: String): String {
        val request = Request.Builder().url(url).build()
        return try {
            val response = client.newCall(request).execute()
            response.body()?.string() ?: ""
        } catch (e: IOException) {
            Log.w(TAG, "Failed to list offline maps at $url")
            ""
        }
    }

    companion object {
        val client: OkHttpClient = OkHttpClient.Builder().build()
    }
}
