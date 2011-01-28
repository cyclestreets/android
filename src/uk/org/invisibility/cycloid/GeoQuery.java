package uk.org.invisibility.cycloid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.cyclestreets.CycleStreetsConstants;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.SAXException;
import android.net.Uri;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;
import uk.org.invisibility.cycloid.GeoResults;

/* http://cambridge.cyclestreets.net/api/geocoder.xml?key=120175c44303728f&w=0.1139374&s=52.2019365&e=0.1219626&n=52.2086693&z=16&street=thoday%20street */

public class GeoQuery
{  
	BoundingBoxE6 bounds;
	
    public GeoQuery(BoundingBoxE6 b)
    {
    	bounds = b;
    }
     
    private String type;
    private String name;
    private String near;
    private String longitude;
    private String latitude;

    public GeoResults query(String q)
    {
    	final GeoResults results = new GeoResults();
    	
        URL url;
		try
		{
			Uri.Builder builder = new Uri.Builder()
				.scheme("http")
				.path("//www.cyclestreets.net/api/geocoder.xml")
				.appendQueryParameter("key", CycleStreetsConstants.API_KEY)
				.appendQueryParameter("street", q);
			if (bounds != null)
			{
				builder.appendQueryParameter("n", "" + bounds.getLatNorthE6() / 1E6)
					.appendQueryParameter("s", "" + bounds.getLatSouthE6() / 1E6)
					.appendQueryParameter("e", "" + bounds.getLonEastE6() / 1E6)
					.appendQueryParameter("w", "" + bounds.getLonWestE6() / 1E6);									
			}
			url = new URL(builder.build().toString());	
			Log.w("GeoQuery", url.toString());
		}
		catch (MalformedURLException e1)
		{
			return results.setError("Error constructing query");
		}
		
    	InputStream is;
    	try
    	{
			is = url.openConnection().getInputStream();
		}
    	catch (IOException e)
    	{
			return results.setError("Error connecting to geocoder: " + e);
		}

		final RootElement root =  new RootElement("sayt");
        final Element el = root.getChild("results").getChild("result");

        
        el.getChild("type").setEndTextElementListener(new EndTextElementListener()
        {
			@Override
			public void end(String body)
			{
				GeoQuery.this.type = body;				
			}
		});
        el.getChild("name").setEndTextElementListener(new EndTextElementListener()
        {
			@Override
			public void end(String body)
			{
				GeoQuery.this.name = body;				
			}
		});
        el.getChild("near").setEndTextElementListener(new EndTextElementListener()
        {
			@Override
			public void end(String body)
			{
				GeoQuery.this.near = body;				
			}
		});
        el.getChild("longitude").setEndTextElementListener(new EndTextElementListener()        
        {
			@Override
			public void end(String body)
			{
				GeoQuery.this.longitude = body;				
			}
		});
        el.getChild("latitude").setEndTextElementListener(new EndTextElementListener()
        {
			@Override
			public void end(String body)
			{
				GeoQuery.this.latitude = body;				
			}
		});
        
        el.setEndElementListener(new EndElementListener()
        {
			public void end()
			{
				if (type.equals("way") || type.equals("node") | type.equals("Postcode"))
				{
            		results.addResult
            		(
                        new GeoPoint
        				(
        					Double.parseDouble(latitude),
                            Double.parseDouble(longitude)
                        ),
                        name,
                        near
            		);
            		type = "";
				}
			}
		});
             
        
		try
		{
			Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
		}
		catch (SAXException e)
		{
			Log.w("GeoQuery", "sax error: " + e);
			return results.setError("XML parse error: " + e);
		}
		catch (IOException e)
		{
			Log.w("GeoQuery", "IO error: " + e);			
			return results.setError("IO error: " + e);
		}
		
		return results;
    }
}