package uk.org.invisibility.cycloid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.api.ApiClient;

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
    	
    	InputStream is;
		try
		{
			final String geo = ApiClient.geoCoder(q, 
										bounds.getLatNorthE6() / 1E6,
										bounds.getLatSouthE6() / 1E6,
										bounds.getLonEastE6() / 1E6,
										bounds.getLonWestE6() / 1E6);
		
			is = new ByteArrayInputStream(geo.getBytes("UTF-8"));
		}
    	catch (Exception e)
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