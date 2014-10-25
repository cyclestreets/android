package net.cyclestreets.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
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
import android.util.Log;
import android.util.Xml;

public class ApiClient
{
  private static SchemeRegistry schemeRegistry_;
  private static ClientConnectionManager connectionManager_;
  private static HttpParams params_;
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
  private final static String API_POST_SCHEME = "https";
  private final static String API_HOST = "www.cyclestreets.net";
  private final static String API_HOST_V2 = "api.cyclestreets.net";
  private final static int API_PORT = -1;
  private final static String API_PATH = "/api/";

  public final static String API_PATH_JOURNEY = API_PATH + "journey.xml";
  public final static String API_PATH_PHOTOS = API_PATH + "photos.xml";
  public final static String API_PATH_PHOTOMAP_CATEGORIES = API_PATH + "photomapcategories.xml";
  public final static String API_PATH_ADDPHOTO = API_PATH + "addphoto.xml";
  public final static String API_PATH_SIGNIN = API_PATH + "uservalidate.xml";
  public final static String API_PATH_REGISTER = API_PATH + "usercreate.xml";
  public final static String API_PATH_FEEDBACK = API_PATH + "feedback.xml";
  public final static String API_PATH_GEOCODER = API_PATH + "geocoder.xml";
  public final static String API_PATH_POI_CATEGORIES = API_PATH + "poitypes.xml";
  public final static String API_PATH_POIS = API_PATH + "pois.xml";

  private final static String BLOG_PATH = "/blog/";
  public final static String BLOG_PATH_FEED = BLOG_PATH + "feed/";


  private static ApiCustomiser customiser_;
  private static Context context_;

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
    POICategories.backgroundLoad();
    PhotomapCategories.backgroundLoad();
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
                              final double[] lonLat)
    throws Exception
  {
    final String points = itineraryPoints(lonLat);
    final byte[] xml = callApiRaw(API_PATH_JOURNEY,
              "plan", plan,
              "itinerarypoints", points,
              "leaving", leaving,
              "arriving", arriving,
              "speed", Integer.toString(speed));
    return new String(xml, "UTF-8");
  } // getJourneyXml

  static String getJourneyXml(final String plan,
                              final long itinerary)
    throws Exception
  {
    final byte[] xml = callApiRaw(API_PATH_JOURNEY,
                                  "plan", plan,
                                  "itinerary", Long.toString(itinerary));
    return new String(xml, "UTF-8");
  } // getJourneyXml

  static PhotomapCategories getPhotomapCategories()
    throws Exception
  {
    return callApiWithCache(PhotomapCategories.factory(), API_PATH_PHOTOMAP_CATEGORIES);
  } // getPhotomapCategories

  static Photos getPhotos(final double longitude,
                          final double latitude,
                          int zoom,
                          double e,
                          double w,
                          double n,
                          double s)
    throws Exception
  {
    return callApi(Photos.factory(),
                    API_PATH_PHOTOS,
                    "longitude", Double.toString(longitude),
                    "latitude", Double.toString(latitude),
                    "zoom", Integer.toString(zoom),
                    "e", Double.toString(e),
                    "w", Double.toString(w),
                    "n", Double.toString(n),
                    "s", Double.toString(s),
                    "suppressplaceholders", "1",
                    "minimaldata", "1",
                    "limit", "30",
                    "thumbnailsize", "250");
  } // getPhotos

  static protected GeoPlaces geoCoder(final String search,
                                      double n,
                                      double s,
                                      double e,
                                      double w)
    throws Exception
  {
    return callApi(GeoPlaces.factory(),
                   API_PATH_GEOCODER,
                   "street", search,
                   "n", Double.toString(n),
                   "s", Double.toString(s),
                   "e", Double.toString(e),
                   "w", Double.toString(w));
  } // geoCoder

  static Feedback.Result sendFeedback(final int itinerary,
                                             final String comments,
                                             final String name,
                                             final String email)
    throws Exception
  {
    return postApi(Feedback.factory(),
             API_PATH_FEEDBACK,
             "type", "routing",
             "itinerary", Integer.toString(itinerary),
             "comments", comments,
             "name", name,
             "email", email);
  } // sendFeedback

  static Upload.Result uploadPhoto(final String filename,
                                   final String username,
                                   final String password,
                                   final double lon,
                                   final double lat,
                                   final String metaCat,
                                   final String category,
                                   final String dateTime,
                                   final String caption)
    throws Exception
  {
    if (filename != null)
      return postApi(Upload.factory(),
                     API_PATH_ADDPHOTO,
                     "username", username,
                     "password", password,
                     "longitude", Double.toString(lon),
                     "latitude", Double.toString(lat),
                     "datetime", dateTime,
                     "category", category,
                     "metacategory", metaCat,
                     "caption", caption,
                     "mediaupload", new FileBody(new File(filename)));
    else
      return postApi(Upload.factory(),
                     API_PATH_ADDPHOTO,
                     "username", username,
                     "password", password,
                     "longitude", Double.toString(lon),
                     "latitude", Double.toString(lat),
                     "datetime", dateTime,
                     "category", category,
                     "metacategory", metaCat,
                     "caption", caption);
  } // uploadPhoto

  static Signin.Result signin(final String username,
                              final String password)
    throws Exception
  {
    return postApi(Signin.factory(),
                   API_PATH_SIGNIN,
                   "username", username,
                   "password", password);
    } // signin

  static Registration.Result register(final String username,
                                      final String password,
                                      final String name,
                                      final String email)
    throws Exception
  {
    return postApi(Registration.factory(),
                   API_PATH_REGISTER,
                   "username", username,
                   "password", password,
                   "name", name,
                   "email", email);
  } // register

  static POICategories getPOICategories(int iconSize)
    throws Exception
  {
    return callApiWithCache(POICategories.factory(context()),
                            API_PATH_POI_CATEGORIES,
                            "icons", Integer.toString(iconSize));
  } // getPOICategories

  static List<POI> getPOIs(final String key,
                           final double lonE,
                           final double lonW,
                           final double latN,
                           final double latS)
    throws Exception
  {
    return callApi(POICategory.factory(),
                   API_PATH_POIS,
                   "type", key,
                   "e", Double.toString(lonE),
                   "w", Double.toString(lonW),
                   "n", Double.toString(latN),
                   "s", Double.toString(latS));
  } // getPOIs

  static List<POI> getPOIs(final String key,
                           final double lon,
                           final double lat,
                           final int radius)
    throws Exception
  {
    return callApi(POICategory.factory(),
        API_PATH_POIS,
        "type", key,
        "longitude", Double.toString(lon),
        "latitude", Double.toString(lat),
        "radius", Integer.toString(radius),
        "limit", "150");
  } // getPOIs

  static Blog getBlogEntries()
    throws Exception
  {
    return callApiWithCache(1, // only cache for a day
                            Blog.factory(),
                            BLOG_PATH_FEED);
  } // getBlogEntries

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
    final URI uri = createURI(API_SCHEME, path, params);
    final HttpGet httpget = new HttpGet(uri);
    return executeRaw(httpget);
  } // callApiRaw

  private static <T> T callApi(final Factory<T> factory, final String path, String... args) throws Exception
  {
    final byte[] xml = callApiRaw(path, args);
    return loadRaw(factory, xml);
  } // callApi

  private static <T> T callApiWithCache(final Factory<T> factory, final String path, String... args) throws Exception
  {
    return callApiWithCache(cacheExpiryDays_, factory, path, args);
  } // callApiWithCache

  private static <T> T callApiWithCache(final int expiryInDays, final Factory<T> factory, final String path, String... args) throws Exception
  {
    final String name = cacheName(path, args);
    byte[] xml = cache_.fetch(name, expiryInDays);

    if(xml == null)
    {
      xml = callApiRaw(path, args);
      cache_.store(name, xml);
    } // if ...

    return loadRaw(factory, xml);
  } // callApiWithCache

  private static <T> T postApi(final Factory<T> factory, final String path, Object... args)
    throws Exception
  {
    final byte[] xml = postApiRaw(path, args);
    return loadRaw(factory, xml);
  } // postApi

  public static byte[] postApiRaw(final String path, Object... arguments) throws Exception {
    Map<String, Object> args = argMap(path, arguments);
    
    final List<NameValuePair> params = createParamsList();
    final URI uri = createURI(API_POST_SCHEME, path, params);

    final MultipartEntity entity = new MultipartEntity();
    for (String name : args.keySet()) {
      final Object value = args.get(name);
      if(value instanceof ContentBody)
        entity.addPart(name, (ContentBody)value);
      else
        entity.addPart(name, new StringBody(value.toString()));
    } // for ...

    final HttpPost httppost = new HttpPost(uri);
    httppost.setEntity(entity);
    return executeRaw(httppost);
  } // postApiRaw

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
    method.setHeader("User-Agent", "CycleStreets Android/1.0");

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
    final String host = path.startsWith("/v2/") ? API_HOST_V2 : API_HOST;
    return URIUtils.createURI(scheme, host, API_PORT, path, URLEncodedUtils.format(params, "UTF-8"), null);
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

  private static <T> T loadRaw(final Factory<T> factory, final byte[] xml) throws Exception
  {
    try {
      final InputStream bais = new ByteArrayInputStream(xml);
      Xml.parse(bais,
                Xml.Encoding.UTF_8,
                factory.contentHandler());
    } // try
    catch(final Exception e) {
      factory.parseException(e);
    } // catch

    return factory.get();
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
