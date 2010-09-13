package uk.org.invisibility.cycloid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.cyclestreets.CycleStreetsConstants;

import org.andnav.osm.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;
import uk.org.invisibility.cycloid.RouteResult;

/* http://www.cyclestreets.net/api/journey.xml?key=120175c44303728f%20&start_longitude=-0.169687&start_latitude=51.535231&finish_longitude=-0.115356&finish_latitude=51.521935&plan=quietest */

public class RouteQuery implements CycleStreetsConstants
{
	RouteResult route;

	public RouteResult query(GeoPoint start, GeoPoint finish, String plan)
    {
		route = new RouteResult();
		
        URL url;
		try {
			url = new URL (new Uri.Builder()
				.scheme("http")
				.path("//www.cyclestreets.net/api/journey.xml")
				.appendQueryParameter("key", API_KEY)
				.appendQueryParameter("start_longitude", "" + start.getLongitudeE6() / 1E6)
				.appendQueryParameter("start_latitude", "" + start.getLatitudeE6() / 1E6)
				.appendQueryParameter("finish_longitude", "" + finish.getLongitudeE6() / 1E6)
				.appendQueryParameter("finish_latitude", "" + finish.getLatitudeE6() / 1E6)
				.appendQueryParameter("plan", plan)
				.build().toString());
		} catch (MalformedURLException e) {
			return route.setError("Error constructing route URL");
		}
		
    	InputStream is;
    	try
    	{
			is = url.openConnection().getInputStream();
		}
    	catch (IOException e)
		{
			return route.setError("Error connecting to route finder: " + e);
		}

        final RootElement root = new RootElement("markers");
        final Element marker = root.getChild("marker");
        marker.setStartElementListener
        (
       		new StartElementListener()
	        {
	            @Override
	        	public void start(final Attributes attributes)
	        	{
	        		final String type =  attributes.getValue("type");

	            	if (type.equals("route"))
	            	{
	            		route.setStart(attributes.getValue("start"));
	            		route.setFinish(attributes.getValue("finish"));
	            		final String[] coords = attributes.getValue("coordinates").split(" ");
	            		for (String coord : coords)
	            		{
	            			final String[] xy = coord.split(",");
	            			route.addCoord
	            			(
	            				new GeoPoint
	            				(
	            					Double.parseDouble(xy[1]),
	                                Double.parseDouble(xy[0])
	                            )
	            			);
	            		}
	
	            	}
	            	else if (type == "segment")
	            	{
	            		// TODO: store segments
	            	}
	            	else if (type == "error")
	            	{
	            		route.setError("Route eror: " + attributes.getValue("description"));
	            	}
	            }
	        }
       	);
        
		try
		{
			Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
		}
		catch (SAXException e)
		{
			route.setError("XML parse error: " + e);
		}
		catch (IOException e)
		{
			route.setError("IO error: " + e);
		}

		return route;
    }
}