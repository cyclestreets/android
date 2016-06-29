package net.cyclestreets.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.cyclestreets.api.GeoPlaces;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.Photos;
import net.cyclestreets.api.Registration;
import net.cyclestreets.api.UserJourneys;
import net.cyclestreets.api.client.dto.GeoPlacesDto;
import net.cyclestreets.api.client.dto.ApiResponseDto;
import net.cyclestreets.api.client.dto.UserJourneysDto;
import net.cyclestreets.api.client.geojson.PhotosFactory;
import net.cyclestreets.api.client.geojson.PoiFactory;

import org.geojson.FeatureCollection;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class RetrofitApiClient {

  private final V1Api v1Api;
  private final V2Api v2Api;

  public RetrofitApiClient(Builder builder) {
    Interceptor apiKeyInterceptor = new ApiKeyInterceptor(builder.apiKey);
    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(apiKeyInterceptor).build();

    // Configure our ObjectMapper to globally ignore unknown properties
    // Required for e.g. getPhotos API which returns `properties` on a `FeatureCollection`, which is
    // not part of standard GeoJSON
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Retrofit retrofitV1 = new Retrofit.Builder()
        .client(client)
        .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
        .baseUrl(builder.v1Host)
        .build();
    v1Api = retrofitV1.create(V1Api.class);

    Retrofit retrofitV2 = new Retrofit.Builder()
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .baseUrl(builder.v2Host)
        .build();
    v2Api = retrofitV2.create(V2Api.class);
  }

  public static class Builder {
    private String apiKey;
    private String v1Host;
    private String v2Host;

    public Builder withApiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }
    public Builder withV1Host(String v1Host) {
      this.v1Host = v1Host;
      return this;
    }

    public Builder withV2Host(String v2Host) {
      this.v2Host = v2Host;
      return this;
    }

    public RetrofitApiClient build() {
      return new RetrofitApiClient(this);
    }
  }

  // --------------------------------------------------------------------------------
  // V1 APIs
  // --------------------------------------------------------------------------------
  public GeoPlaces geoCoder(final String search,
                            final double n,
                            final double s,
                            final double e,
                            final double w) throws IOException {
    Response<GeoPlacesDto> response = v1Api.geoCoder(search, n, s, e, w).execute();
    return response.body().toGeoPlaces();
  }

  // --------------------------------------------------------------------------------
  // V2 APIs
  // --------------------------------------------------------------------------------

  public List<POI> getPOIs(final String type,
                           final double lonE,
                           final double lonW,
                           final double latN,
                           final double latS) throws IOException {
    String bbox = lonW + "," + latS + "," + lonE + "," + latN;
    Response<FeatureCollection> response = v2Api.getPOIs(type, bbox).execute();
    return PoiFactory.toPoiList(response.body());
  }

  public List<POI> getPOIs(final String type,
                           final double lon,
                           final double lat,
                           final int radius) throws IOException {
    Response<FeatureCollection> response = v2Api.getPOIs(type, lon, lat, radius).execute();
    return PoiFactory.toPoiList(response.body());
  }

  public Photos getPhotos(final double lonE,
                          final double lonW,
                          final double latN,
                          final double latS) throws IOException {
    String bbox = lonW + "," + latS + "," + lonE + "," + latN;
    Response<FeatureCollection> response = v2Api.getPhotos(bbox).execute();
    return PhotosFactory.toPhotos(response.body());
  }

  public UserJourneys getUserJourneys(String username) throws IOException {
    Response<UserJourneysDto> response = v2Api.getUserJourneys(username).execute();
    return response.body().toUserJourneys();
  }

  public Registration.Result register(String username,
                                      String password,
                                      String name,
                                      String email) throws IOException {
    Response<ApiResponseDto> response = v2Api.register(username, password, name, email).execute();
    return new Registration.Result(response.body().wasSuccessful(), response.body().getMessage());
  }
}
