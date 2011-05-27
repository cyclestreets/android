package uk.org.invisibility.cycloid;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.content.Intent;
import android.location.Location;

/*
 * Utility class for storing geocode bounding boxes in an Intent 
 */
public class GeoIntent
{
	static private final String EXTRA_NORTH = "bounds-north";
	static private final String EXTRA_EAST = "bounds-east";
	static private final String EXTRA_SOUTH = "bounds-south";
	static private final String EXTRA_WEST = "bounds-west";
	
    static private final String GEO_LATITUDE = "latitude";
    static private final String GEO_LONGITUDE = "longitude";

	
	static public BoundingBoxE6 getBoundingBox(final Intent intent)
    {
        if(intent.hasExtra(EXTRA_NORTH)	&&
           intent.hasExtra(EXTRA_EAST) &&
           intent.hasExtra(EXTRA_SOUTH) &&
           intent.hasExtra(EXTRA_WEST))
        {
        	return new BoundingBoxE6(
    			intent.getIntExtra(EXTRA_NORTH, 0),
    			intent.getIntExtra(EXTRA_EAST, 0),
    			intent.getIntExtra(EXTRA_SOUTH, 0),
    			intent.getIntExtra(EXTRA_WEST, 0));
        } // if ...

        return null;
    } // getBoundingBox

    static public void setBoundingBox(final Intent intent, 
    								  final BoundingBoxE6 bounds)
    {
      	intent.putExtra(EXTRA_NORTH, bounds.getLatNorthE6());
    	intent.putExtra(EXTRA_EAST, bounds.getLonEastE6());
    	intent.putExtra(EXTRA_SOUTH, bounds.getLatSouthE6());
    	intent.putExtra(EXTRA_WEST, bounds.getLonWestE6());
    } // setBoundingBox

    //////////////////////////////////////////////
    static public GeoPoint getGeoPoint(final Intent intent)
    {
    	if(intent.hasExtra(GEO_LATITUDE) &&
    	   intent.hasExtra(GEO_LONGITUDE))
    	{
    		int lat = intent.getIntExtra(GEO_LATITUDE, 0);
    		int lon = intent.getIntExtra(GEO_LONGITUDE, 0);
    		
    		return new GeoPoint(lat, lon);
    	} // if ...
    	
    	return null;
    } // getLocation
    
    static public void setGeoPoint(final Intent intent,
    							   final GeoPoint point)
    {
    	intent.putExtra(GEO_LATITUDE, point.getLatitudeE6());
    	intent.putExtra(GEO_LONGITUDE, point.getLongitudeE6());
    } // setGeoPoint
    
    static public void setLocation(final Intent intent, 
    							   final Location location)
    {
    	if(location == null)
    		return;
    	intent.putExtra(GEO_LATITUDE, (int)(location.getLatitude() * 1E6));
    	intent.putExtra(GEO_LONGITUDE, (int)(location.getLongitude() * 1E6));
    } // setLocation

} // GeoIntent
