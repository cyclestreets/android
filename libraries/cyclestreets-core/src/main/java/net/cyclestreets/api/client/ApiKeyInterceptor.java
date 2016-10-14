package net.cyclestreets.api.client;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyInterceptor implements Interceptor {

  private final String apiKey;

  public ApiKeyInterceptor(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    HttpUrl newUrl = request.url().newBuilder().addQueryParameter("key", apiKey).build();
    Request newRequest = request.newBuilder().url(newUrl).build();
    return chain.proceed(newRequest);
  }
}
