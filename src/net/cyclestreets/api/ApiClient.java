package net.cyclestreets.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.R;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
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
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.util.Xml;

public class ApiClient {
  static private DefaultHttpClient httpclient;
  static private SchemeRegistry schemeRegistry;
  static {
    schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(
            new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    schemeRegistry.register(
            new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

    final HttpParams params = new BasicHttpParams();
    final ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
    httpclient = new DefaultHttpClient(cm, params);
    
    httpclient.addRequestInterceptor(new HttpRequestInterceptor() {            
      public void process(
          final HttpRequest request, 
          final HttpContext context) throws HttpException, IOException {
        request.setHeader("Accept-Encoding", "deflate");
      }
    });
  } // static
  
  private final static String API_SCHEME = "http";
  private final static String API_POST_SCHEME = "https";
  private final static String API_HOST = "www.cyclestreets.net";
  private final static int API_PORT = -1;
  private final static String API_PATH = "/api/";
  private final static String API_KEY = "b26a0d6b45e00612";
  
  private final static String API_PATH_JOURNEY = API_PATH + "journey.xml";
  private final static String API_PATH_PHOTOS = API_PATH + "photos.xml";
  private final static String API_PATH_PHOTOMAP_CATEGORIES = API_PATH + "photomapcategories.xml";
  private final static String API_PATH_ADDPHOTO = API_PATH + "addphoto.xml";
  private final static String API_PATH_SIGNIN = API_PATH + "uservalidate.xml";
  private final static String API_PATH_REGISTER = API_PATH + "usercreate.xml";
  private final static String API_PATH_FEEDBACK = API_PATH + "feedback.xml";
  private final static String API_PATH_GEOCODER = API_PATH + "geocoder.xml";
  private final static String API_PATH_POI_CATEGORIES = API_PATH + "poitypes.xml";
  private final static String API_PATH_POIS = API_PATH + "pois.xml";

  private final static int DEFAULT_SPEED = 20;

  static public void loadSslCertificates(final Context context)
  {
    // Based on code from http://blog.crazybob.org/2010/02/android-trusting-ssl-certificates.html
    // Works around Android not trusting newer GeoTrust certificates.
    try 
    {
      final KeyStore trusted = KeyStore.getInstance("BKS");
      final InputStream in = context.getResources().openRawResource(R.raw.mykeystore);
      try 
      {
        trusted.load(in, "mysecret".toCharArray());
      } 
      finally 
      {
        in.close();
      }
    
      schemeRegistry.unregister(API_POST_SCHEME);
      schemeRegistry.register(new Scheme(API_POST_SCHEME, new SSLSocketFactory(trusted), 443));
    } // try
    catch (Exception e) 
    {
      throw new AssertionError(e);
    } // catch
  } // loadSslCertificates
    
  private ApiClient() {}
  
  static public Journey getJourney(final String plan, final GeoPoint start, final GeoPoint finish) 
    throws Exception 
  {
    return getJourney(plan, start, finish, DEFAULT_SPEED);
  } // getJourney

  static public Journey getJourney(final String plan, final GeoPoint start, final GeoPoint finish, final int speed) 
    throws Exception 
  {
    return getJourney(plan,
                      start.getLongitudeE6() / 1E6, 
                      start.getLatitudeE6() / 1E6,
                      finish.getLongitudeE6() / 1E6, 
                      finish.getLatitudeE6() / 1E6,
                      null, 
                      null, 
                      speed);
  } // getJourney

  static public Journey getJourney(final String plan, 
                                   final double startLon, 
                                   final double startLat, 
                                   final double finishLon, 
                                   final double finishLat,
                                   final String leaving, 
                                   final String arriving, 
                                   final int speed) 
    throws Exception 
  {
    final String points = itineraryPoints(startLon, startLat, finishLon, finishLat);
    return callApi(Journey.class, 
             API_PATH_JOURNEY,
             "plan", plan,
             "itinerarypoints", points,
             "leaving", leaving,
             "arriving", arriving,
             "speed", Integer.toString(speed));
  } // getJourney
  
  static public String getJourneyXml(final String plan, 
                                     final GeoPoint start, 
                                     final GeoPoint finish) 
    throws Exception 
  {
    return getJourneyXml(plan,
                         start,
                         finish, 
                         DEFAULT_SPEED);
  } // getJourneyXml
  
  static public String getJourneyXml(final String plan, 
                                     final GeoPoint start, 
                                     final GeoPoint finish, 
                                     final int speed) 
    throws Exception 
  {
    return getJourneyXml(plan,
               start.getLongitudeE6() / 1E6, 
               start.getLatitudeE6() / 1E6,
               finish.getLongitudeE6() / 1E6, 
               finish.getLatitudeE6() / 1E6,
               null, 
               null, 
               speed);
  } // getJourneyXml
  
  static public String getJourneyXml(final String plan, 
                     final double startLon, 
                     final double startLat, 
                     final double finishLon, 
                     final double finishLat,
                     final String leaving, 
                     final String arriving, 
                     final int speed) 
    throws Exception
  {
    final String points = itineraryPoints(startLon, startLat, finishLon, finishLat);
    final byte[] xml = callApiRaw(API_PATH_JOURNEY,
              "plan", plan,
              "itinerarypoints", points,
              "leaving", leaving,
              "arriving", arriving,
              "speed", Integer.toString(speed));
    return new String(xml, "UTF-8");
  } // getJourneyXml
  
  static public String getJourneyXml(final String plan, 
                                     final long itinerary) 
    throws Exception
  {
    final byte[] xml =callApiRaw(API_PATH_JOURNEY,
                                 "plan", plan,
                                 "itinerary", Long.toString(itinerary));
    return new String(xml, "UTF-8");
  } // getJourneyXml
    
  static public PhotomapCategories getPhotomapCategories() 
    throws Exception 
  {
    return callApi(PhotomapCategories.class, API_PATH_PHOTOMAP_CATEGORIES);
  } // getPhotomapCategories
  
  static public List<Photo> getPhotos(final GeoPoint centre,
                    final int zoom, 
                    final double n, 
                    final double s, 
                    final double e, 
                    final double w) 
    throws Exception 
  {
    return getPhotos(centre.getLatitudeE6() / 1E6, 
             centre.getLongitudeE6() / 1E6, 
             zoom, 
             n, 
             s, 
             e, 
             w);
  } // getPhotos

  static public List<Photo> getPhotos(final double latitude, 
                    final double longitude,
                    int zoom, 
                    double n, 
                    double s, 
                    double e, 
                    double w) 
    throws Exception 
  {
    final Photos photos = callApi(Photos.class, 
                    API_PATH_PHOTOS,
                    "latitude", Double.toString(latitude),
                    "longitude", Double.toString(longitude),
                    "zoom", Integer.toString(zoom),
                    "n", Double.toString(n),
                    "s", Double.toString(s),
                    "e", Double.toString(e),
                    "w", Double.toString(w),
                    "suppressplaceholders", "1",
                    "minimaldata", "1",
                    "limit", "30",
                    "thumbnailsize", "250");
    return photos.photos;
  } // getPhotos
  
  static public GeoPlaces geoCoder(final String search,
                   final BoundingBoxE6 bounds)
    throws Exception
  {
    return geoCoder(search,
            bounds.getLatNorthE6() / 1E6,
            bounds.getLatSouthE6() / 1E6,
            bounds.getLonEastE6() / 1E6,
            bounds.getLonWestE6() / 1E6);
  } // geoCoder
  
  static public GeoPlaces geoCoder(final String search,
                   double n,
                   double s,
                   double e,
                   double w)
    throws Exception
  {
    return callApi(GeoPlaces.class,
             API_PATH_GEOCODER,
             "street", search,
             "n", Double.toString(n),
             "s", Double.toString(s),
             "e", Double.toString(e),
             "w", Double.toString(w));
  }
  
  static public FeedbackResult sendFeedback(final int itinerary, 
                           final String comments,
                           final String name,
                           final String email)
    throws Exception
  {
    return postApi(FeedbackResult.class, 
             API_PATH_FEEDBACK,
             "type", "routing",
             "itinerary", Integer.toString(itinerary),
             "comments", comments,
             "name", name,
             "email", email);
  } // sendFeedback
  
  static public UploadResult uploadPhoto(final String filename,
                       final String username,
                       final String password,
                       final GeoPoint location,
                       final String metaCat,
                       final String category,
                       final String dateTime,
                       final String caption)
    throws Exception
  {

    return postApi(UploadResult.class, 
             API_PATH_ADDPHOTO,
             "username", username,
             "password", password,
             "latitude", Double.toString(location.getLatitudeE6() / 1E6),
             "longitude", Double.toString(location.getLongitudeE6() / 1E6),
             "datetime", dateTime,
             "category", category,
             "metacategory", metaCat,
             "caption", caption,
             "mediaupload", new FileBody(new File(filename)));
  } // uploadPhoto
  
  static public SigninResult signin(final String username, 
                    final String password) 
    throws Exception 
  {
    return postApi(SigninResult.class,
             API_PATH_SIGNIN, 
             "username", username,
             "password", password);
    } // signin

  static public RegistrationResult register(final String username, 
                        final String password,
                        final String name,
                        final String email) 
    throws Exception 
  {
    return postApi(RegistrationResult.class,
               API_PATH_REGISTER, 
               "username", username,
               "password", password,
               "name", name,
               "email", email);
  } // register
  
  static public POICategories getPOICategories()
    throws Exception
  {
    return callApi(POICategories.factory(), API_PATH_POI_CATEGORIES);
  } // getPOICategories
  
  static public List<POI> getPOIs(final String key,
                                  final GeoPoint centre, 
                                  final int radius)
    throws Exception
  {
    return callApi(POICategory.factory(),
                   API_PATH_POIS,
                   "type", key, 
                   "latitude", Double.toString(centre.getLatitudeE6() / 1E6),
                   "longitude", Double.toString(centre.getLongitudeE6() / 1E6),
                   "radius", Integer.toString(radius));
  } // getPOIs

  /////////////////////////////////////////////////////
  /////////////////////////////////////////////////////
  static private String itineraryPoints(final double startLon, 
          final double startLat, 
          final double finishLon, 
          final double finishLat)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(startLon).append(",").append(startLat).append("|")
      .append(finishLon).append(",").append(finishLat);
    return sb.toString();
  } // itineraryPoints

  static private byte[] callApiRaw(final String path, String... args) throws Exception
  {
    final List<NameValuePair> params = createParamsList(args);
    final URI uri = createURI(API_SCHEME, path, params);   
    final HttpGet httpget = new HttpGet(uri);
    return executeRaw(httpget);
  } // callApiRaw
  
  static private <T> T callApi(final Class<T> returnClass, final String path, String... args) throws Exception 
  {
    final byte[] xml = callApiRaw(path, args);
    return loadRaw(returnClass, xml);
  } // callApi
  
  static private <T> T callApi(final Factory<T> factory, final String path, String... args) throws Exception
  {
    final byte[] xml = callApiRaw(path, args);
    return loadRaw(factory, xml);
  } // callApi
  
  static private <T> T postApi(final Class<T> returnClass, final String path, Object... args) 
    throws Exception 
  {
    final byte[] xml = postApiRaw(path, args);
    return loadRaw(returnClass, xml);
  } // postApi
  
  static private byte[] postApiRaw(final String path, Object... args) throws Exception
  {
    final List<NameValuePair> params = createParamsList();
    final URI uri = createURI(API_POST_SCHEME, path, params);
    
    final MultipartEntity entity = new MultipartEntity();
    for (int i = 0; i < args.length; i += 2) 
    {
      final String name = (String)args[i];
      final Object value = args[i+1];
      if(value instanceof String)
        entity.addPart(name, new StringBody((String)value));
      else
        entity.addPart(name, (ContentBody)value);
    } // for ...
      
    final HttpPost httppost = new HttpPost(uri);
    httppost.setEntity(entity);      
    return executeRaw(httppost);
  } // postApiRaw
  
  static private byte[] executeRaw(final HttpRequestBase method) 
      throws ClientProtocolException, IOException
  {
    method.setHeader("User-Agent", "CycleStreets Android/1.0");

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

  static private URI createURI(final String scheme,
                 final String path,
                 final List<NameValuePair> params)
    throws Exception
  {
    return URIUtils.createURI(scheme, API_HOST, API_PORT, path, URLEncodedUtils.format(params, "UTF-8"), null);
  } // createCycleStreetsURI
  
  static private List<NameValuePair> createParamsList(final String... args)
  {
    final List<NameValuePair> params = createParamsList();
      for (int i = 0; i < args.length; i += 2) {
        params.add(new BasicNameValuePair(args[i], args[i+1]));
      }
    return params;
  } // createParamsList
  
  static private List<NameValuePair> createParamsList()
  {
    final List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("key", API_KEY));
    return params;
  } // createParamsList
  
  static private class UTF8ResponseHandler implements ResponseHandler<String> 
  {
    public String handleResponse(final HttpResponse response) 
      throws ClientProtocolException, IOException 
    {
      final HttpEntity entity = response.getEntity();
      if (entity == null) 
        return null;
       
      final StatusLine statusLine = response.getStatusLine();
      if (statusLine.getStatusCode() < 300) 
        return EntityUtils.toString(entity, "UTF-8");
      
      entity.consumeContent();
      throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
    } // handleResponse
  } // class UTF8ResponseHandler 

  static public <T> T loadRaw(final Class<T> returnClass, final byte[] xml) throws Exception
  {
    final Serializer serializer = new Persister();
    final InputStream bais = new ByteArrayInputStream(xml);
    return serializer.read(returnClass, bais);
  } // loadRaw
  
  static public <T> T loadRaw(final Factory<T> factory, final byte[] xml) throws Exception
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

  static public <T> T loadString(final Class<T> returnClass, final String xml) throws Exception
  {
    final Serializer serializer = new Persister();
    return serializer.read(returnClass, xml);
  } // loadRaw
} // ApiClient
