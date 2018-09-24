package net.cyclestreets.util

import android.os.AsyncTask
import android.util.Log
import java.io.File

private val TAG = Logging.getTag(AsyncDelete::class.java)

class AsyncDelete : AsyncTask<File, Void, Unit>() {
    override fun doInBackground(vararg files: File?) {
        files.filterNotNull().forEach {
            Log.i(TAG, "Deleting ${it.absolutePath}")
            it.delete()
        }
    }
}
