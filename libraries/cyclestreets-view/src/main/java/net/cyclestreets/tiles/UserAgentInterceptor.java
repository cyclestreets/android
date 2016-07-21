package net.cyclestreets.tiles;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

  private final String userAgent;

  public UserAgentInterceptor(String userAgent) {
    this.userAgent = userAgent;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    HttpUrl newUrl = request.url().newBuilder().addQueryParameter("CoreProtocolPNames.USER_AGENT", userAgent).build();
    Request newRequest = request.newBuilder().url(newUrl).build();
    return chain.proceed(newRequest);
  }
}
