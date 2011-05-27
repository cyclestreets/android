package uk.org.invisibility.cycloid;

import net.cyclestreets.api.GeoPlace;

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

    static private final String GEO_NAME = "place-name";
    static private final String GEO_NEAR = "place-near";
	
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
    	return getGeoPoint(intent, "");
    } // getGeoPoint
    
    static public GeoPoint getGeoPoint(final Intent intent, final String prefix)
    {
    	if(intent.hasExtra(prefix+GEO_LATITUDE) &&
    	   intent.hasExtra(prefix+GEO_LONGITUDE))
    	{
    		int lat = intent.getIntExtra(prefix+GEO_LATITUDE, 0);
    		int lon = intent.getIntExtra(prefix+GEO_LONGITUDE, 0);
    		
    		return new GeoPoint(lat, lon);
    	} // if ...
    	
    	return null;
    } // getLocation
    
    static public void setGeoPoint(final Intent intent, final GeoPoint point)
    {
    	setGeoPoint(intent, "", point);
    } // setGeoPoint
    static public void setGeoPoint(final Intent intent,
    							   final String prefix,
    							   final GeoPoint point)
    {
    	intent.putExtra(prefix+GEO_LATITUDE, point.getLatitudeE6());
    	intent.putExtra(prefix+GEO_LONGITUDE, point.getLongitudeE6());
    } // setGeoPoint
    
    static public void setLocation(final Intent intent, 
    							   final Location location)
    {
    	if(location == null)
    		return;
    	intent.putExtra(GEO_LATITUDE, (int)(location.getLatitude() * 1E6));
    	intent.putExtra(GEO_LONGITUDE, (int)(location.getLongitude() * 1E6));
    } // setLocation

    //////////////////////////////////////////////
    static public GeoPlace getGeoPlace(final Intent intent)
    {
    	final GeoPoint point = getGeoPoint(intent);
    	final String name = intent.getStringExtra(GEO_NAME);
    	final String near = intent.getStringExtra(GEO_NEAR);
    	
    	if((point == null) || (name == null) || (near == null))
    		return null;
    	return new GeoPlace(point, name, near);
    } // getGeoPlace

    static public void setGeoPlace(final Intent intent, final GeoPlace place)
    {
    	setGeoPoint(intent, place.coord());
    	intent.putExtra(GEO_NAME, place.name);
    	intent.putExtra(GEO_NEAR, place.near);
    } // setGeoPlace
} // GeoIntent
