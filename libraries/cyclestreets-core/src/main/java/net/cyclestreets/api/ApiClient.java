package net.cyclestreets.api;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import net.cyclestreets.AppInfo;
import net.cyclestreets.api.client.RetrofitApiClient;

public class ApiClient
{
  private static SchemeRegistry schemeRegistry_;
  private static ClientConnectionManager connectionManager_;
  private static HttpParams params_;
  private static String userAgent_;
  static
  {
    schemeRegistry_ = new SchemeRegistry();
    schemeRegistry_.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    schemeRegistry_.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

    params_ = new BasicHttpParams();
    connectionManager_ = new ThreadSafeClientConnManager(params_, schemeRegistry_);
  } // static

  private final static int cacheExpiryDays_ = 7;
  private static ApiCallCache cache_;
  private static String apiKey_;

  private final static String API_SCHEME = "http";
  private final static String API_SCHEME_V2 = "https";
  private final static String API_HOST = "www.cyclestreets.net";
  private final static String API_HOST_V2 = "api.cyclestreets.net";
  private final static int API_PORT = -1;
  private final static String API_PATH_V2 = "/v2/";

  private static ApiCustomiser customiser_;
  private static Context context_;

  private static RetrofitApiClient retrofitApiClient;

  static Context context()
  {
    if(context_ == null)
      throw new RuntimeException("ApiClient.initialise(context) has not been called");
    return context_;
  } // context

  public static void initialise(final Context context)
  {
    context_ = context;
    apiKey_ = findApiKey(context_);
    cache_ = new ApiCallCache(context_);
    userAgent_ = AppInfo.version(context_);
    POICategories.backgroundLoad();
    PhotomapCategories.backgroundLoad();

    retrofitApiClient = new RetrofitApiClient.Builder()
        .withApiKey(apiKey_)
        .withV1Host(API_HOST)
        .withV2Host(API_HOST_V2)
        .build();
  } // initialise

  public static void setCustomiser(ApiCustomiser customiser) {
    customiser_ = customiser;
  } // setCustomiser

  private static String findApiKey(final Context context) {
    try {
      final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      final Bundle bundle = ai.metaData;
      return bundle.getString("CycleStreetsAPIKey");
    } catch(final Exception e) {
      throw new RuntimeException(e);
    } // catch
  } // apiKey

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

  static Photos getPhotos(double e, double w, double n, double s) throws IOException {
    return retrofitApiClient.getPhotos(w, s, e, n);
  }

  static UserJourneys getUserJournies(final String username) throws IOException {
    return retrofitApiClient.getUserJourneys(username);
  }

  static protected GeoPlaces geoCoder(final String search,
                                      double n,
                                      double s,
                                      double e,
                                      double w) throws IOException {
    return retrofitApiClient.geoCoder(search, w, s, e, n);
  }

  static Feedback.Result sendFeedback(final int itinerary,
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

  static Registration.Result register(final String username,
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
    for(int i = 0; i != lonLat.length; i += 2)
    {
      if(i != 0)
        sb.append("|");
      sb.append(lonLat[i]).append(",").append(lonLat[i+1]);
    } // for ...
    return sb.toString();
  } // itineraryPoints

  private static byte[] callApiRaw(final String path, String... arguments) throws Exception {
    Map<String, String> args = argMap(path, arguments);

    final List<NameValuePair> params = createParamsList(args);
    final URI uri = createURI(null, path, params);
    final HttpGet httpget = new HttpGet(uri);
    return executeRaw(httpget);
  } // callApiRaw

  private static <T> T callApiWithCache(final Factory<T> factory, final String path, String... args) throws Exception
  {
    return callApiWithCache(cacheExpiryDays_, factory, path, args);
  } // callApiWithCache

  private static <T> T callApiWithCache(final int expiryInDays, final Factory<T> factory, final String path, String... args) throws Exception
  {
    final String name = cacheName(path, args);
    byte[] results = cache_.fetch(name, expiryInDays);

    if(results == null)
    {
      results = callApiRaw(path, args);
      cache_.store(name, results);
    } // if ...

    return loadRaw(factory, results);
  } // callApiWithCache

  private static String cacheName(final String path, final String... args)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(path);
    boolean b = true;
    for(final String a : args)
    {
      sb.append(b ? "-" : "=");
      sb.append(a);
      b = !b;
    } // for ...
    return sb.toString().replace('/', '-');
  } // cacheName

  private static byte[] executeRaw(final HttpRequestBase method)
      throws IOException
  {
    method.setHeader("User-Agent", userAgent_);

    final HttpClient httpclient = new DefaultHttpClient(connectionManager_, params_);

    final HttpResponse response = httpclient.execute(method);

    final HttpEntity entity = response.getEntity();
    if (entity == null)
      return null;

    final StatusLine statusLine = response.getStatusLine();
    if (statusLine.getStatusCode() < 300)
      return EntityUtils.toByteArray(entity);

    entity.consumeContent();
    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
  } // executeRaw

  private static URI createURI(final String scheme,
                               final String path,
                               final List<NameValuePair> params)
    throws Exception
  {
    final String schemeInUse = scheme != null ? scheme : (path.startsWith("/v2/") ? API_SCHEME_V2 : API_SCHEME);
    final String host = path.startsWith(API_PATH_V2) ? API_HOST_V2 : API_HOST;
    return URIUtils.createURI(schemeInUse, host, API_PORT, path, URLEncodedUtils.format(params, "UTF-8"), null);
  } // createCycleStreetsURI

  private static List<NameValuePair> createParamsList(final Map<String, String> args)
  {
    final List<NameValuePair> params = createParamsList();
      for (String name : args.keySet())
        params.add(new BasicNameValuePair(name, args.get(name)));
    return params;
  } // createParamsList

  private static List<NameValuePair> createParamsList()
  {
    final List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("key", apiKey_));
    return params;
  } // createParamsList

  private static <T> T loadRaw(final Factory<T> factory, final byte[] result) throws Exception
  {
    return factory.read(result);
  } // loadRaw

  private static <T> Map<String, T> argMap(String path, T... args) {
    Map<String, T> params = new HashMap<>();
    for (int i = 0; i != args.length; i+=2)
      params.put((String)args[i], args[i+1]);

    if (customiser_ != null)
      customiser_.customise(path, params);

    return params;
  } // argMap
} // ApiClient
