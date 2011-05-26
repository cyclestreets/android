package uk.org.invisibility.cycloid;

import java.util.List;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;

import org.osmdroid.util.BoundingBoxE6;

public class GeoQuery
{  
	BoundingBoxE6 bounds;
	
    public GeoQuery(BoundingBoxE6 b)
    {
    	bounds = b;
    }
     
    public List<GeoPlace> query(String q)
    {
		try
		{
	    	final GeoPlaces results =  ApiClient.geoCoder(q, 
										bounds.getLatNorthE6() / 1E6,
										bounds.getLatSouthE6() / 1E6,
										bounds.getLonEastE6() / 1E6,
										bounds.getLonWestE6() / 1E6);
	    	return results.places;
		}
    	catch (Exception e)
    	{
			return null;
		}
    }
}