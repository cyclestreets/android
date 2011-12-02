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

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
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
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;

public class ApiClient 
{
  static private DefaultHttpClient httpclient;
  static private SchemeRegistry schemeRegistry;
  static 
  {
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
  
  private final static int cacheExpiryDays_ = 7;
  private static ApiCallCache cache_;
  
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

  private static Context context_;
  
  static Context context() 
  { 
    if(context_ == null)
      throw new RuntimeException("ApiClient.initialise(context) has not been called");
    return context_; 
  } // context
  
  static public void initialise(final Context context)
  {
    context_ = context;
    cache_ = new ApiCallCache(context_, cacheExpiryDays_);
    loadSslCertificates(context);
    POICategories.backgroundLoad();
    PhotomapCategories.backgroundLoad();
  } // initialise

  static private void loadSslCertificates(final Context context)
  {    
    final LoadSSLCertsTask load = new LoadSSLCertsTask(context, schemeRegistry);
    load.execute();
  } // loadSslCertificates
    
  static private class LoadSSLCertsTask extends AsyncTask<Void,Void,SSLSocketFactory>
  {
    private Context context_;
    private SchemeRegistry schemeRegistry_;
    
    public LoadSSLCertsTask(final Context context,
                            final SchemeRegistry schemeRegistry)
    {
      context_ = context;
      schemeRegistry_ = schemeRegistry;
    } // LoadSSLCertsTask
    
    protected SSLSocketFactory doInBackground(Void... params) 
    {
      // Based on code from http://blog.crazybob.org/2010/02/android-trusting-ssl-certificates.html
      // Works around Android not trusting newer GeoTrust certificates.
      try 
      {
        final KeyStore trusted = KeyStore.getInstance("BKS");
        final InputStream in = context_.getResources().openRawResource(R.raw.mykeystore);
        try 
        {
          trusted.load(in, "mysecret".toCharArray());
        } 
        finally 
        {
          in.close();
        }
        
        return new SSLSocketFactory(trusted);
      } // try
      catch (Exception e) 
      {
        // I'm not sure what we can do here, really
      } // catch
      return null;
    } // doInBackground
    
    @Override
    protected void onPostExecute(final SSLSocketFactory socketFactory) 
    {
      if(socketFactory == null)
        return;
      
      schemeRegistry_.unregister(API_POST_SCHEME);
      schemeRegistry_.register(new Scheme(API_POST_SCHEME, socketFactory, 443));
    } // onPostExecute
  } // GetPOICategoriesTask

  /////////////////////////////////////////////////////////////////////////
  private ApiClient() {}
  
  static String getJourneyXml(final String plan, 
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
  
  static String getJourneyXml(final String plan, 
                              final long itinerary) 
    throws Exception
  {
    final byte[] xml =callApiRaw(API_PATH_JOURNEY,
                                 "plan", plan,
                                 "itinerary", Long.toString(itinerary));
    return new String(xml, "UTF-8");
  } // getJourneyXml
    
  static PhotomapCategories getPhotomapCategories() 
    throws Exception 
  {
    return callApiWithCache(PhotomapCategories.factory(), API_PATH_PHOTOMAP_CATEGORIES);
  } // getPhotomapCategories
  
  static Photos getPhotos(final double latitude, 
                          final double longitude,
                          int zoom, 
                          double n, 
                          double s, 
                          double e, 
                          double w) 
    throws Exception 
  {
    return callApi(Photos.factory(), 
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
                                   final double lat,
                                   final double lon,
                                   final String metaCat,
                                   final String category,
                                   final String dateTime,
                                   final String caption)
    throws Exception
  {

    return postApi(Upload.factory(), 
             API_PATH_ADDPHOTO,
             "username", username,
             "password", password,
             "latitude", Double.toString(lat),
             "longitude", Double.toString(lon),
             "datetime", dateTime,
             "category", category,
             "metacategory", metaCat,
             "caption", caption,
             "mediaupload", new FileBody(new File(filename)));
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
                           final double latN,
                           final double latS,
                           final double lonE,
                           final double lonW)
    throws Exception
  {
    return callApi(POICategory.factory(),
                   API_PATH_POIS,
                   "type", key, 
                   "n", Double.toString(latN),
                   "s", Double.toString(latS),
                   "e", Double.toString(lonE),
                   "w", Double.toString(lonW));
  } // getPOIs
  
  static List<POI> getPOIs(final String key,
                           final double lat,
                           final double lon,
                           final int radius)
    throws Exception
  {
    return callApi(POICategory.factory(),
        API_PATH_POIS,
        "type", key, 
        "latitude", Double.toString(lat),
        "longitude", Double.toString(lon),
        "radius", Integer.toString(radius),
        "limit", "150");
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
  
  static private <T> T callApi(final Factory<T> factory, final String path, String... args) throws Exception
  {
    final byte[] xml = callApiRaw(path, args);
    return loadRaw(factory, xml);
  } // callApi

  static private <T> T callApiWithCache(final Factory<T> factory, final String path, String... args) throws Exception
  {
    final String name = cacheName(path, args);
    byte[] xml = cache_.fetch(name);
    
    if(xml == null)
    {
      xml = callApiRaw(path, args);
      cache_.store(name, xml);
    } // if ...
    
    return loadRaw(factory, xml);
  } // callApiWithCache
  
  static private <T> T postApi(final Factory<T> factory, final String path, Object...args)
    throws Exception
  {
    final byte[] xml = postApiRaw(path, args);
    return loadRaw(factory, xml);
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
  
  static private String cacheName(final String path, final String... args)
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
  
  static private <T> T loadRaw(final Factory<T> factory, final byte[] xml) throws Exception
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
} // ApiClient
