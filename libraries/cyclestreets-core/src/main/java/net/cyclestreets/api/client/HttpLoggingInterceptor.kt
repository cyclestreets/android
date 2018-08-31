package net.cyclestreets.api.client

import android.util.Log
import net.cyclestreets.util.Logging
import java.io.IOException

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.StatusLine

private val TAG = Logging.getTag(HttpLoggingInterceptor::class.java)
private val API_KEY_REGEX = "key=\\w+".toRegex()

class HttpLoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url().toString().replace(API_KEY_REGEX, "key=redacted")

        Log.d(TAG, "Sending request: $url with headers: ${request.headers()}")
        val response = chain.proceed(request)

        Log.d(TAG, "Received ${StatusLine.get(response)} response for: $url")
        return response
    }
}
