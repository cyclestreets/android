package net.cyclestreets.api.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * See https://github.com/square/okhttp/wiki/Interceptors.
 *
 * OkHttp automatically caches according to the settings specified by the Cache-Control header in
 * a server response.  Ideally, the server should be updated to return meaningful Cache-Control
 * headers.  In the absence of this change, we have to set our own in this network interceptor.
 * 
 * A half-way solution for some endpoints may be to use the `validuntil` field in the JSON
 * response, if one exists.
 */
public class RewriteCacheControlInterceptor implements Interceptor {

  private static final int SECONDS_PER_DAY = 86400;
  private final Map<String, Integer> cacheDurationMap = new HashMap<String, Integer>() {{
    put("/blog/feed/", 1 * SECONDS_PER_DAY);
    put("/v2/photomap.categories", 7 * SECONDS_PER_DAY);
    put("/v2/pois.types", 7 * SECONDS_PER_DAY);
  }};

  @Override
  public Response intercept(Chain chain) throws IOException {
    Response originalResponse = chain.proceed(chain.request());
    String urlPath = chain.request().url().encodedPath();
    if (cacheDurationMap.containsKey(urlPath)) {
      return originalResponse.newBuilder()
              .header("Cache-Control", String.format(Locale.ROOT,
                      "public, max-age=%d", cacheDurationMap.get(urlPath)))
              .build();
    } else {
      return originalResponse;
    }
  }
}
