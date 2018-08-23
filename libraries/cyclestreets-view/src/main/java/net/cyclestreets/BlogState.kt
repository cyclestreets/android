package net.cyclestreets

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

import net.cyclestreets.api.Blog
import net.cyclestreets.util.Logging

import java.util.Timer
import java.util.TimerTask

private const val BLOG_SHARED_PREFERENCES_NAME = "net.cyclestreets.blog"
private const val LAST_DATE_KEY = "lastDate"
private const val UPDATE_AVAILABLE_KEY = "updateAvailable"
private const val ONE_MINUTE_DELAY = (1000 * 60).toLong()
private const val ONE_DAY_REPEAT = (1000 * 60 * 60 * 24).toLong()
private val TAG: String = Logging.getTag(BlogState::class.java)

object BlogState {

    private val blogUpdateTimer: Timer = Timer()
    private var initialised: Boolean = false

    fun initialise(context: Context) {
        if (!initialised) {
            Log.d(TAG, "Starting blog update timer")
            blogUpdateTimer.schedule(CheckBlogTask(context.applicationContext), ONE_MINUTE_DELAY, ONE_DAY_REPEAT)
            initialised = true
        }
    }

    fun isBlogUpdateAvailable(context: Context): Boolean {
        return prefs(context).getBoolean(UPDATE_AVAILABLE_KEY, false)
    }

    fun markBlogAsRead(context: Context) {
        prefs(context).edit()
            .putBoolean(UPDATE_AVAILABLE_KEY, false)
            .apply()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(BLOG_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private class CheckBlogTask(private val checkBlogContext: Context) : TimerTask() {
        override fun run() {
            Log.d(TAG, "Checking for blog updates")
            val blog = Blog.load()

            // check for new blog entries
            if (blog.isNull || blog.mostRecent() == lastBlogUpdate(checkBlogContext))
                return

            Log.d(TAG, "New blog update found, date=${blog.mostRecent()}, title=${blog.mostRecentTitle()}")
            blogUpdated(checkBlogContext, blog.mostRecent())
        }

        private fun lastBlogUpdate(context: Context): String? {
            return prefs(context).getString(LAST_DATE_KEY, null)
        }

        private fun blogUpdated(context: Context, update: String?) {
            prefs(context).edit()
                .putString(LAST_DATE_KEY, update)
                .putBoolean(UPDATE_AVAILABLE_KEY, true)
                .apply()
        }
    }
}
