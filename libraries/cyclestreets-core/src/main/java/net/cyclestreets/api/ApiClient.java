package net.cyclestreets.api;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import net.cyclestreets.api.client.RetrofitApiClient;

import java.io.IOException;
import java.util.List;

public class ApiClient
{
  private final static String API_HOST = "https://www.cyclestreets.net";
  private final static String API_HOST_V2 = "https://api.cyclestreets.net";

  private static ApiCustomiser customiser;
  private static Context context;

  private static RetrofitApiClient retrofitApiClient;

  static Context context() {
    if (context == null)
      throw new RuntimeException("ApiClient.initialise(context) has not been called");
    return context;
  }

  public static void initialise(final Context context) {
    ApiClient.context = context;

    retrofitApiClient = new RetrofitApiClient.Builder()
        .withContext(context())
        .withApiKey(findApiKey(context()))
        .withV1Host(API_HOST)
        .withV2Host(API_HOST_V2)
        .build();

    POICategories.backgroundLoad();
    PhotomapCategories.backgroundLoad();
  }

  public static void setCustomiser(ApiCustomiser customiser) {
    ApiClient.customiser = customiser;
  }

  private static String findApiKey(final Context context) {
    try {
      final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      final Bundle bundle = ai.metaData;
      return bundle.getString("CycleStreetsAPIKey");
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /////////////////////////////////////////////////////////////////////////
  private ApiClient() {}

  static String getJourneyXml(final String plan,
                              final String leaving,
                              final String arriving,
                              final int speed,
                              final double[] lonLat) throws IOException {
    final String points = itineraryPoints(lonLat);
    return retrofitApiClient.getJourneyXml(plan, points, leaving, arriving, speed);
  }

  static String getJourneyXml(final String plan,
                              final long itineraryId) throws IOException {
    return retrofitApiClient.retrievePreviousJourneyXml(plan, itineraryId);
  }

  static PhotomapCategories getPhotomapCategories() throws IOException {
    return retrofitApiClient.getPhotomapCategories();
  }

  static Photos getPhotos(final double e,
                          final double w,
                          final double n,
                          final double s) throws IOException {
    return retrofitApiClient.getPhotos(w, s, e, n);
  }

  static UserJourneys getUserJournies(final String username) throws IOException {
    return retrofitApiClient.getUserJourneys(username);
  }

  static GeoPlaces geoCoder(final String search,
                            double n,
                            double s,
                            double e,
                            double w) throws IOException {
    return retrofitApiClient.geoCoder(search, w, s, e, n);
  }

  static Result sendFeedback(final int itinerary,
                             final String comments,
                             final String name,
                             final String email) throws IOException {
    return retrofitApiClient.sendFeedback(itinerary, comments, name, email);
  }

  static Upload.Result uploadPhoto(final String filename,
                                   final String username,
                                   final String password,
                                   final double lon,
                                   final double lat,
                                   final String metaCat,
                                   final String category,
                                   final String dateTime,
                                   final String caption) throws IOException {
    return retrofitApiClient.uploadPhoto(username, password, lon, lat, Long.valueOf(dateTime),
                                         category, metaCat, caption, filename);
  }

  static Signin.Result signin(final String username,
                              final String password) throws IOException {
    return retrofitApiClient.authenticate(username, password);
  }

  static Result register(final String username,
                         final String password,
                         final String name,
                         final String email) throws IOException {
    return retrofitApiClient.register(username, password, name, email);
  }

  static POICategories getPOICategories(final int iconSize) throws IOException {
    return retrofitApiClient.getPOICategories(iconSize);
  }

  static List<POI> getPOIs(final String key,
                           final double lonE,
                           final double lonW,
                           final double latN,
                           final double latS) throws IOException {
    return retrofitApiClient.getPOIs(key, lonW, latS, lonE, latN);
  }

  static List<POI> getPOIs(final String key,
                           final double lon,
                           final double lat,
                           final int radius) throws IOException {
    return retrofitApiClient.getPOIs(key, lon, lat, radius);
  }

  static Blog getBlogEntries() throws IOException {
    return retrofitApiClient.getBlogEntries();
  }

  /////////////////////////////////////////////////////
  /////////////////////////////////////////////////////
  private static String itineraryPoints(final double... lonLat)
  {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i != lonLat.length; i += 2) {
      if (i != 0)
        sb.append("|");
      sb.append(lonLat[i]).append(",").append(lonLat[i+1]);
    }
    return sb.toString();
  }
}
