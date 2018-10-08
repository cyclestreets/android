package net.cyclestreets.offline

import android.os.AsyncTask
import okio.Okio
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class OfflineMapDownloadTask(private val url: String, private val destFile: File) : AsyncTask<Void, Int, Boolean>() {

    override fun doInBackground(vararg params: Void?): Boolean {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val contentLength = response.body()!!.contentLength()
        val source = response.body()!!.source()

        val sink = Okio.buffer(Okio.sink(destFile))
        val sinkBuffer = sink.buffer()

        var totalBytesRead = 0L
        val bufferSize = 8 * 1024L
        var bytesRead: Long = source.read(sinkBuffer, bufferSize)

        while (bytesRead != -1L && !isCancelled) {
            sink.emit()
            totalBytesRead += bytesRead
            publishProgress((totalBytesRead * 100 / contentLength).toInt())
            bytesRead = source.read(sinkBuffer, bufferSize)
        }

        sink.flush()
        sink.close()
        source.close()
        return true
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val percentDone = values[0]
    }

    override fun onPostExecute(result: Boolean?) {
        // TODO: mark successful in all ways
    }

    override fun onCancelled(result: Boolean?) {
        if (result == false)
            destFile.delete()
    }

    companion object {
        val client: OkHttpClient = OkHttpClient.Builder().build()
    }
}