package net.cyclestreets.api.client;

import android.content.Context;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.cyclestreets.api.Blog;
import net.cyclestreets.api.GeoPlaces;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.POICategories;
import net.cyclestreets.api.PhotomapCategories;
import net.cyclestreets.api.Photos;
import net.cyclestreets.api.Result;
import net.cyclestreets.api.Signin;
import net.cyclestreets.api.Upload;
import net.cyclestreets.api.UserJourneys;
import net.cyclestreets.api.client.dto.BlogFeedDto;
import net.cyclestreets.api.client.dto.PhotomapCategoriesDto;
import net.cyclestreets.api.client.dto.PoiTypesDto;
import net.cyclestreets.api.client.dto.SendFeedbackResponseDto;
import net.cyclestreets.api.client.dto.UploadPhotoResponseDto;
import net.cyclestreets.api.client.dto.UserAuthenticateResponseDto;
import net.cyclestreets.api.client.dto.UserCreateResponseDto;
import net.cyclestreets.api.client.dto.UserJourneysDto;
import net.cyclestreets.api.client.geojson.GeoPlacesFactory;
import net.cyclestreets.api.client.geojson.PhotosFactory;
import net.cyclestreets.api.client.geojson.PoiFactory;

import org.geojson.FeatureCollection;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class RetrofitApiClient {

  private final V1Api v1Api;
  private final V2Api v2Api;
  private final Context context;

  // ~30KB covers /blog/feed/, /v2/pois.types and /v2/photomap.categories - allow 200KB for headroom
  private static final int CACHE_MAX_SIZE_BYTES = 200 * 1024;
  private static final String CACHE_DIR_NAME = "RetrofitApiClientCache";

  public RetrofitApiClient(Builder builder) {

    context = builder.context;
    Cache cache = new Cache(new File(context.getCacheDir(), CACHE_DIR_NAME), CACHE_MAX_SIZE_BYTES);
    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new ApiKeyInterceptor(builder.apiKey))
            .addNetworkInterceptor(new RewriteCacheControlInterceptor())
            .cache(cache)
            .build();

    // Configure our ObjectMapper to globally ignore unknown properties
    // Required for e.g. getPhotos API which returns `properties` on a `FeatureCollection`, which is
    // not part of standard GeoJSON
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Retrofit retrofitV1 = new Retrofit.Builder()
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
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
    private Context context;
    private String apiKey;
    private String v1Host;
    private String v2Host;

    public Builder withContext(Context context) {
      this.context = context;
      return this;
    }

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

  private static String toBboxString(double lonW, double latS, double lonE, double latN) {
    return lonW + "," + latS + "," + lonE + "," + latN;
  }

  // --------------------------------------------------------------------------------
  // V1 APIs
  // --------------------------------------------------------------------------------
  public String getJourneyXml(final String plan,
                              final String itineraryPoints,
                              final String leaving,
                              final String arriving,
                              final int speed) throws IOException {
    Response<String> response = v1Api.getJourneyXml(plan, itineraryPoints, leaving, arriving, speed).execute();
    return response.body();
  }

  public String retrievePreviousJourneyXml(final String plan,
                                           final long itineraryId) throws IOException {
    Response<String> response = v1Api.retrievePreviousJourneyXml(plan, itineraryId).execute();
    return response.body();
  }

  public Blog getBlogEntries() throws IOException {
    Response<BlogFeedDto> response = v1Api.getBlogEntries().execute();
    return response.body().toBlog();
  }

  // --------------------------------------------------------------------------------
  // V2 APIs
  // --------------------------------------------------------------------------------

  public POICategories getPOICategories(int iconSize) throws IOException {
    Response<PoiTypesDto> response = v2Api.getPOICategories(iconSize).execute();
    return response.body().toPOICategories(context);
  }

  public List<POI> getPOIs(final String type,
                           final double lonW,
                           final double latS,
                           final double lonE,
                           final double latN) throws IOException {
    String bbox = toBboxString(lonW, latS, lonE, latN);
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

  public GeoPlaces geoCoder(final String search,
                            final double lonW,
                            final double latS,
                            final double lonE,
                            final double latN) throws IOException {
    String bbox = toBboxString(lonW, latS, lonE, latN);
    Response<FeatureCollection> response = v2Api.geoCoder(search, bbox).execute();
    return GeoPlacesFactory.toGeoPlaces(response.body());
  }

  public PhotomapCategories getPhotomapCategories() throws IOException {
    Response<PhotomapCategoriesDto> response = v2Api.getPhotomapCategories().execute();
    return response.body().toPhotomapCategories();
  }

  public Photos getPhotos(final double lonW,
                          final double latS,
                          final double lonE,
                          final double latN) throws IOException {
    String bbox = toBboxString(lonW, latS, lonE, latN);
    Response<FeatureCollection> response = v2Api.getPhotos(bbox).execute();
    return PhotosFactory.toPhotos(response.body());
  }

  public UserJourneys getUserJourneys(final String username) throws IOException {
    Response<UserJourneysDto> response = v2Api.getUserJourneys(username).execute();
    return response.body().toUserJourneys();
  }

  public Result register(final String username,
                         final String password,
                         final String name,
                         final String email) throws IOException {
    Response<UserCreateResponseDto> response = v2Api.register(username, password, name, email).execute();
    return response.body().toRegistrationResult();
  }

  public Signin.Result authenticate(final String identifier,
                                    final String password) throws IOException {
    Response<UserAuthenticateResponseDto> response = v2Api.authenticate(identifier, password).execute();
    return response.body().toSigninResult();
  }

  public Result sendFeedback(final int itinerary,
                             final String comments,
                             final String name,
                             final String email) throws IOException {
    Response<SendFeedbackResponseDto> response = v2Api.sendFeedback("routing", itinerary, comments, name, email).execute();
    return response.body().toFeedbackResult();
  }

  public Upload.Result uploadPhoto(final String username,
                                   final String password,
                                   final double lon,
                                   final double lat,
                                   final long dateTime,
                                   final String category,
                                   final String metaCat,
                                   final String caption,
                                   final String filename) throws IOException {
    MultipartBody.Part filePart = null;
    if (filename != null) {
      File file = new File(filename);
      RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
      filePart = MultipartBody.Part.createFormData("mediaupload", file.getName(), fileBody);
    }
    // Unfortunately we have to do all this faff, otherwise the JSON converter will insert quotes!
    RequestBody usernamePart = RequestBody.create(MediaType.parse("text/plain"), username);
    RequestBody passwordPart = RequestBody.create(MediaType.parse("text/plain"), password);
    RequestBody categoryPart = RequestBody.create(MediaType.parse("text/plain"), category);
    RequestBody metaCatPart = RequestBody.create(MediaType.parse("text/plain"), metaCat);
    RequestBody captionPart = RequestBody.create(MediaType.parse("text/plain"), caption);
    Response<UploadPhotoResponseDto> response = v2Api.uploadPhoto(usernamePart, passwordPart, lon, lat, dateTime, categoryPart, metaCatPart, captionPart, filePart).execute();
    return response.body().toUploadResult();
  }
}
