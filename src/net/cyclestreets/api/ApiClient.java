package net.cyclestreets.api;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

import com.nutiteq.components.WgsPoint;

public class ApiClient {
	protected static DefaultHttpClient httpclient = new DefaultHttpClient();
	static {
		httpclient.addRequestInterceptor(new HttpRequestInterceptor() {            
			public void process(
					final HttpRequest request, 
					final HttpContext context) throws HttpException, IOException {
				request.setHeader("Accept-Encoding", "deflate");
			}
		});
	}
	
	// http://www.cyclestreets.net/api/journey.xml?key=b26a0d6b45e00612&start_longitude=0.117950&start_latitude=52.205302&finish_longitude=0.147324&finish_latitude=52.199650&plan=quietest
	// String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><markers xmlns:cs=\"http://www.cyclestreets.net/schema/xml/\"><marker start=\"Link joining St Mary's Passage, King's Parade, NCN 11, King's Parade\" finish=\"Thoday Street\" start_longitude=\"0.117950\" start_latitude=\"52.205303\" startBearing=\"0\" startSpeed=\"0\" finish_longitude=\"0.147324\" finish_latitude=\"52.199650\" crow_fly_distance=\"2099\" event=\"depart\" whence=\"2010-08-13 16:54:18\" speed=\"20\" itinerary=\"202591\" clientRouteId=\"0\" plan=\"quietest\" note=\"\" length=\"3349\" time=\"777\" busynance=\"4937\" quietness=\"68\" walk=\"1\" signalledJunctions=\"2\" signalledCrossings=\"0\" west=\"0.117950\" south=\"52.199650\" east=\"0.147324\" north=\"52.205303\" name=\"Link joining St Mary's Passage, King's Parade, NCN 11, King's Parade to Thoday Street\" type=\"route\" leaving=\"2010-08-13 16:54:18\" arriving=\"2010-08-13 17:07:15\" coordinates=\"0.117867,52.205288 0.117872,52.205441 0.117904,52.205482 0.117978,52.205502 0.117978,52.205502 0.118032,52.205448 0.118107,52.205437 0.118507,52.205463 0.118734,52.205505 0.118734,52.205505 0.118932,52.205570 0.119160,52.205650 0.119246,52.205681 0.119653,52.205841 0.120082,52.205971 0.120563,52.206089 0.120563,52.206089 0.120631,52.206085 0.120683,52.206078 0.120730,52.206059 0.120730,52.206059 0.120959,52.205765 0.121141,52.205536 0.121251,52.205444 0.121492,52.205345 0.121741,52.205292 0.121827,52.205273 0.121827,52.205273 0.122161,52.205009 0.122373,52.204815 0.122983,52.204117 0.123057,52.204041 0.123057,52.204041 0.123242,52.203800 0.123415,52.203579 0.123415,52.203579 0.123511,52.203476 0.124111,52.202835 0.124478,52.202457 0.124478,52.202457 0.124541,52.202419 0.124565,52.202385 0.124595,52.202335 0.124595,52.202335 0.124766,52.202145 0.124766,52.202145 0.124991,52.201904 0.124991,52.201904 0.125177,52.201866 0.125177,52.201866 0.125266,52.201885 0.125266,52.201885 0.128164,52.202160 0.129550,52.202301 0.130356,52.202389 0.130356,52.202389 0.130827,52.202194 0.130827,52.202194 0.131244,52.202290 0.131654,52.202339 0.131654,52.202339 0.132280,52.202076 0.132418,52.202019 0.132928,52.201805 0.133287,52.201660 0.134104,52.201328 0.134795,52.201027 0.135064,52.200916 0.135563,52.200710 0.136058,52.200508 0.136321,52.200405 0.136742,52.200253 0.136892,52.200195 0.137123,52.200104 0.137288,52.200035 0.138386,52.199593 0.138401,52.199581 0.138580,52.199516 0.139368,52.199215 0.139387,52.199207 0.139815,52.199059 0.140267,52.198929 0.140267,52.198929 0.141393,52.198555 0.141393,52.198555 0.142005,52.198376 0.142015,52.198376 0.142917,52.198116 0.143034,52.198082 0.143084,52.198071 0.143084,52.198071 0.143176,52.198288 0.143718,52.199577 0.143734,52.199615 0.143734,52.199615 0.143814,52.199593 0.143814,52.199593 0.145211,52.199341 0.145211,52.199341 0.145323,52.199333 0.145323,52.199333 0.145412,52.199303 0.145412,52.199303 0.146076,52.199188 0.146076,52.199188 0.146166,52.199200 0.146166,52.199200 0.147430,52.201195 0.147501,52.201370 0.147463,52.201561 0.147353,52.201736 0.147199,52.201836 0.147199,52.201836 0.147000,52.201851 0.147000,52.201851 0.147050,52.201942 0.147050,52.201942 0.147017,52.202061 0.147167,52.202114 0.147386,52.202114 0.147506,52.202061 0.147506,52.202061 0.147635,52.202030 0.147788,52.202026 0.148368,52.202110 0.148448,52.202164 0.148448,52.202164 0.148630,52.202053 0.148630,52.202053 0.148542,52.202011 0.148537,52.201923 0.148601,52.201584 0.148498,52.201328 0.148237,52.200905 0.147489,52.199680\" /> <marker name=\"King's Parade\" points=\"0.117867,52.205288 0.117872,52.205441 0.117904,52.205482 0.117978,52.205502\" flow=\"\" distance=\"29\" time=\"5\" busynance=\"40\" walk=\"0\" startBearing=\"1\" turn=\"unknown\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"17,17,17,17\" distances=\"0,17,5,5\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"St Mary's Street, NCN 11\" points=\"0.117978,52.205502 0.118032,52.205448 0.118107,52.205437 0.118507,52.205463 0.118734,52.205505\" flow=\"with\" distance=\"56\" time=\"10\" busynance=\"79\" walk=\"0\" startBearing=\"148\" turn=\"turn right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"17,17,17,17,17\" distances=\"0,7,5,27,16\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Market Street, NCN 11\" points=\"0.118734,52.205505 0.118932,52.205570 0.119160,52.205650 0.119246,52.205681 0.119653,52.205841 0.120082,52.205971 0.120563,52.206089\" flow=\"with\" distance=\"145\" time=\"39\" busynance=\"299\" walk=\"0\" startBearing=\"62\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"17,18,18,18,19,19,19\" distances=\"0,15,18,7,33,33,35\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Sidney Street\" points=\"0.120563,52.206089 0.120631,52.206085 0.120683,52.206078 0.120730,52.206059\" flow=\"with\" distance=\"14\" time=\"3\" busynance=\"13\" walk=\"0\" startBearing=\"95\" turn=\"bear right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"19,19,19,19\" distances=\"0,5,4,4\" provisionName=\"Cycle Track\" color=\"#ff0000\" type=\"segment\" /> <marker name=\"Sidney Street\" points=\"0.120730,52.206059 0.120959,52.205765 0.121141,52.205536 0.121251,52.205444 0.121492,52.205345 0.121741,52.205292 0.121827,52.205273\" flow=\"\" distance=\"123\" time=\"18\" busynance=\"135\" walk=\"0\" startBearing=\"154\" turn=\"bear right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"19,19,19,18,16,16,16\" distances=\"0,36,28,13,20,18,6\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"St Andrew's Street\" points=\"0.121827,52.205273 0.122161,52.205009 0.122373,52.204815 0.122983,52.204117 0.123057,52.204041\" flow=\"\" distance=\"163\" time=\"36\" busynance=\"278\" walk=\"0\" startBearing=\"142\" turn=\"bear right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"16,16,16,17,17\" distances=\"0,37,26,88,10\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"St Andrew's Street\" points=\"0.123057,52.204041 0.123242,52.203800 0.123415,52.203579\" flow=\"\" distance=\"58\" time=\"17\" busynance=\"130\" walk=\"0\" startBearing=\"155\" turn=\"straight on\" signalledJunctions=\"1\" signalledCrossings=\"0\" elevations=\"17,17,18\" distances=\"0,30,27\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"St Andrew's Street\" points=\"0.123415,52.203579 0.123511,52.203476 0.124111,52.202835 0.124478,52.202457\" flow=\"\" distance=\"146\" time=\"53\" busynance=\"254\" walk=\"0\" startBearing=\"150\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,18,19,19\" distances=\"0,13,82,49\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Link between Regent Street and St Andrew's Street\" points=\"0.124478,52.202457 0.124541,52.202419 0.124565,52.202385 0.124595,52.202335\" flow=\"with\" distance=\"17\" time=\"9\" busynance=\"71\" walk=\"0\" startBearing=\"135\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"19,19,19,20\" distances=\"0,6,4,6\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Regent Street\" points=\"0.124595,52.202335 0.124766,52.202145\" flow=\"\" distance=\"25\" time=\"5\" busynance=\"35\" walk=\"0\" startBearing=\"151\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"20,20\" distances=\"0,24\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Regent Street\" points=\"0.124766,52.202145 0.124991,52.201904\" flow=\"\" distance=\"32\" time=\"4\" busynance=\"30\" walk=\"0\" startBearing=\"150\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"20,19\" distances=\"0,31\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Link between Regent Street and Regent Terrace\" points=\"0.124991,52.201904 0.125177,52.201866\" flow=\"with\" distance=\"14\" time=\"3\" busynance=\"17\" walk=\"0\" startBearing=\"109\" turn=\"bear left\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"19,19\" distances=\"0,13\" provisionName=\"Unsegregated Shared Use\" color=\"#ff8888\" type=\"segment\" /> <marker name=\"Link between Parkers Piece (east-west) and Regent Terrace\" points=\"0.125177,52.201866 0.125266,52.201885\" flow=\"\" distance=\"7\" time=\"2\" busynance=\"8\" walk=\"0\" startBearing=\"71\" turn=\"bear left\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"19,19\" distances=\"0,6\" provisionName=\"Unsegregated Shared Use\" color=\"#ff8888\" type=\"segment\" /> <marker name=\"Parkers Piece (east-west)\" points=\"0.125266,52.201885 0.128164,52.202160 0.129550,52.202301 0.130356,52.202389\" flow=\"\" distance=\"355\" time=\"76\" busynance=\"414\" walk=\"0\" startBearing=\"81\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"19,18,18,17\" distances=\"0,200,96,56\" provisionName=\"Unsegregated Shared Use\" color=\"#ff8888\" type=\"segment\" /> <marker name=\"Link between Parkers Piece (east-west) and Gonville Place\" points=\"0.130356,52.202389 0.130827,52.202194\" flow=\"\" distance=\"40\" time=\"24\" busynance=\"52\" walk=\"1\" startBearing=\"124\" turn=\"bear right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"17,17\" distances=\"0,39\" provisionName=\"Footpath\" color=\"#008800\" type=\"segment\" /> <marker name=\"Link between Mill Road and Gonville Place\" points=\"0.130827,52.202194 0.131244,52.202290 0.131654,52.202339\" flow=\"\" distance=\"59\" time=\"14\" busynance=\"73\" walk=\"0\" startBearing=\"70\" turn=\"bear left\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"17,17,17\" distances=\"0,30,28\" provisionName=\"Unsegregated Shared Use\" color=\"#ff8888\" type=\"segment\" /> <marker name=\"Mill Road\" points=\"0.131654,52.202339 0.132280,52.202076 0.132418,52.202019 0.132928,52.201805 0.133287,52.201660 0.134104,52.201328 0.134795,52.201027 0.135064,52.200916 0.135563,52.200710 0.136058,52.200508 0.136321,52.200405 0.136742,52.200253 0.136892,52.200195 0.137123,52.200104 0.137288,52.200035 0.138386,52.199593 0.138401,52.199581 0.138580,52.199516 0.139368,52.199215 0.139387,52.199207 0.139815,52.199059 0.140267,52.198929\" flow=\"\" distance=\"720\" time=\"178\" busynance=\"1188\" walk=\"0\" startBearing=\"124\" turn=\"bear right\" signalledJunctions=\"1\" signalledCrossings=\"0\" elevations=\"17,20,20,19,19,19,19,19,19,19,19,19,19,19,19,18,20,20,19,19,18,18\" distances=\"0,52,11,42,29,67,58,22,41,41,21,33,12,19,14,90,2,14,63,2,34,34\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Mill Road\" points=\"0.140267,52.198929 0.141393,52.198555\" flow=\"\" distance=\"88\" time=\"16\" busynance=\"125\" walk=\"0\" startBearing=\"118\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,18\" distances=\"0,87\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Mill Road\" points=\"0.141393,52.198555 0.142005,52.198376 0.142015,52.198376 0.142917,52.198116 0.143034,52.198082 0.143084,52.198071\" flow=\"\" distance=\"133\" time=\"26\" busynance=\"185\" walk=\"0\" startBearing=\"116\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,18,18,18,18,18\" distances=\"0,46,1,68,9,4\" provisionName=\"Road\" color=\"#33aa33\" type=\"segment\" /> <marker name=\"Cavendish Road\" points=\"0.143084,52.198071 0.143176,52.198288 0.143718,52.199577 0.143734,52.199615\" flow=\"\" distance=\"180\" time=\"33\" busynance=\"210\" walk=\"0\" startBearing=\"15\" turn=\"turn left\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,18,18,18\" distances=\"0,25,148,4\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"St Philip's Road\" points=\"0.143734,52.199615 0.143814,52.199593\" flow=\"with\" distance=\"7\" time=\"2\" busynance=\"8\" walk=\"0\" startBearing=\"115\" turn=\"turn right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,18\" distances=\"0,6\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"St Philip's Road\" points=\"0.143814,52.199593 0.145211,52.199341\" flow=\"\" distance=\"100\" time=\"18\" busynance=\"117\" walk=\"0\" startBearing=\"106\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,18\" distances=\"0,99\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"St Philip's Road\" points=\"0.145211,52.199341 0.145323,52.199333\" flow=\"with\" distance=\"9\" time=\"2\" busynance=\"10\" walk=\"0\" startBearing=\"96\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,18\" distances=\"0,8\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"St Philip's Road\" points=\"0.145323,52.199333 0.145412,52.199303\" flow=\"with\" distance=\"8\" time=\"2\" busynance=\"9\" walk=\"0\" startBearing=\"119\" turn=\"bear right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,18\" distances=\"0,7\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"St Philip's Road\" points=\"0.145412,52.199303 0.146076,52.199188\" flow=\"\" distance=\"48\" time=\"15\" busynance=\"96\" walk=\"0\" startBearing=\"106\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"18,19\" distances=\"0,47\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"St Philip's Road\" points=\"0.146076,52.199188 0.146166,52.199200\" flow=\"with\" distance=\"7\" time=\"2\" busynance=\"8\" walk=\"0\" startBearing=\"78\" turn=\"bear left\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"19,19\" distances=\"0,6\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"Catharine Street\" points=\"0.146166,52.199200 0.147430,52.201195 0.147501,52.201370 0.147463,52.201561 0.147353,52.201736 0.147199,52.201836\" flow=\"with\" distance=\"316\" time=\"50\" busynance=\"326\" walk=\"0\" startBearing=\"21\" turn=\"bear left\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"19,17,15,15,15,15\" distances=\"0,238,20,21,21,15\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"Catharine Street\" points=\"0.147199,52.201836 0.147000,52.201851\" flow=\"with\" distance=\"15\" time=\"9\" busynance=\"57\" walk=\"0\" startBearing=\"277\" turn=\"bear left\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"15,16\" distances=\"0,14\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"Link joining Roundabout, Cromwell Road, Catharine Street\" points=\"0.147000,52.201851 0.147050,52.201942\" flow=\"with\" distance=\"12\" time=\"2\" busynance=\"13\" walk=\"0\" startBearing=\"19\" turn=\"turn right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"16,16\" distances=\"0,11\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"Roundabout\" points=\"0.147050,52.201942 0.147017,52.202061 0.147167,52.202114 0.147386,52.202114 0.147506,52.202061\" flow=\"with\" distance=\"53\" time=\"16\" busynance=\"94\" walk=\"0\" startBearing=\"350\" turn=\"bear left\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"16,16,14,14,15\" distances=\"0,13,12,15,10\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"Fairfax Road\" points=\"0.147506,52.202061 0.147635,52.202030 0.147788,52.202026 0.148368,52.202110 0.148448,52.202164\" flow=\"\" distance=\"70\" time=\"9\" busynance=\"55\" walk=\"0\" startBearing=\"111\" turn=\"straight on\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"15,15,15,13,13\" distances=\"0,9,10,41,8\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"Fairfax Road\" points=\"0.148448,52.202164 0.148630,52.202053\" flow=\"with\" distance=\"18\" time=\"10\" busynance=\"60\" walk=\"0\" startBearing=\"135\" turn=\"turn right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"13,14\" distances=\"0,17\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> <marker name=\"Thoday Street\" points=\"0.148630,52.202053 0.148542,52.202011 0.148537,52.201923 0.148601,52.201584 0.148498,52.201328 0.148237,52.200905 0.147489,52.199680\" flow=\"with\" distance=\"282\" time=\"69\" busynance=\"448\" walk=\"0\" startBearing=\"232\" turn=\"turn right\" signalledJunctions=\"0\" signalledCrossings=\"0\" elevations=\"14,14,14,14,14,16,17\" distances=\"0,8,10,38,29,50,145\" provisionName=\"Quiet Street\" color=\"#000000\" type=\"segment\" /> </markers>";

	protected final static String API_SCHEME = "http";
	protected final static String API_HOST = "www.cyclestreets.net";
	protected final static int API_PORT = -1;
	protected final static String API_PATH = "/api/";
	protected final static String API_KEY = "b26a0d6b45e00612";
	
	
	
	
	protected final static String API_PATH_JOURNEY = API_PATH + "journey.xml";
	protected final static String API_PATH_PHOTOS = API_PATH + "photos.xml";

	protected final static int DEFAULT_SPEED = 20;

	public ApiClient() {}
	
	public Journey getJourney(String plan, WgsPoint start, WgsPoint finish) throws Exception {
		return getJourney(plan,
				start.getLon(), start.getLat(),
				finish.getLon(), finish.getLat(),
				null, null, DEFAULT_SPEED);
	}

	public Journey getJourney(String plan, WgsPoint start, WgsPoint finish, int speed) throws Exception {
		return getJourney(plan, start.getLon(), start.getLat(), finish.getLon(), finish.getLat(),
				null, null, speed);
	}

	public Journey getJourney(String plan, double startLon, double startLat, double finishLon, double finishLat,
			String leaving, String arriving, int speed) throws Exception {
		return (Journey) callApi(Journey.class, API_PATH_JOURNEY,
				"plan", plan,
				"start_longitude", Double.toString(startLon),
				"start_latitude", Double.toString(startLat),
				"finish_longitude", Double.toString(finishLon),
				"finish_latitude", Double.toString(finishLat),
				"leaving", leaving,
				"arriving", arriving,
				"speed", Integer.toString(speed)
				);
	}
	
	public List<Photo> getPhotos(WgsPoint center,
			int zoom, double n, double s, double e, double w) throws Exception {
		return getPhotos(center.getLat(), center.getLon(), zoom, n, s, e, w);
	}

	public List<Photo> getPhotos(double latitude, double longitude,
			int zoom, double n, double s, double e, double w) throws Exception {
		Photos photos = (Photos) callApi(Photos.class, API_PATH_PHOTOS,
				"latitude", Double.toString(latitude),
				"longitude", Double.toString(longitude),
				"zoom", Integer.toString(zoom),
				"n", Double.toString(n),
				"s", Double.toString(s),
				"e", Double.toString(e),
				"w", Double.toString(w)
				);
		return photos.photos;
	}
	
	public Object callApi(Class<?> returnClass, String path, String... args) throws Exception {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
    	params.add(new BasicNameValuePair("key", API_KEY));
    	for (int i = 0; i < args.length; i += 2) {
    		params.add(new BasicNameValuePair(args[i], args[i+1]));
    	}
    	URI uri = URIUtils.createURI(API_SCHEME, API_HOST, API_PORT, path,
    			URLEncodedUtils.format(params, "UTF-8"), null);
    	Log.d(getClass().getSimpleName(), "Fetching: " + uri.toString());
    	
    	HttpGet httpget = new HttpGet(uri);
    	String response = httpclient.execute(httpget, new BasicResponseHandler());
    	Log.d(getClass().getSimpleName(), "Response: " + response);
    	
    	Serializer serializer = new Persister();
    	return serializer.read(returnClass, response);
	}
}
