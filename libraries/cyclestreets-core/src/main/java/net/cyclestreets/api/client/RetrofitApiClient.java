package net.cyclestreets.api.client;

import net.cyclestreets.api.POI;
import net.cyclestreets.api.client.dto.FeatureCollection;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitApiClient {

  private final V1Api v1Api;
  private final V2Api v2Api;

  public RetrofitApiClient(String v1Host, String v2Host, String apiKey) {
    Interceptor apiKeyInterceptor = new ApiKeyInterceptor(apiKey);
    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(apiKeyInterceptor).build();

    Retrofit retrofitV1 = new Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(v1Host)
            .build();
    v1Api = retrofitV1.create(V1Api.class);

    Retrofit retrofitV2 = new Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(v2Host)
        .build();
    v2Api = retrofitV2.create(V2Api.class);
  }

  public List<POI> getPOIs(final String type,
                           final double lonE,
                           final double lonW,
                           final double latN,
                           final double latS) throws IOException {
    String bbox = lonW + "," + latS + "," + lonE + "," + latN;
    Response<FeatureCollection> response = v2Api.getPOIs(type, bbox).execute();
    return response.body().toPoiList();
  }

  public List<POI> getPOIs(final String type,
                           final double lon,
                           final double lat,
                           final int radius) throws IOException {
    Response<FeatureCollection> response = v2Api.getPOIs(type, lon, lat, radius).execute();
    return response.body().toPoiList();
  }
}
