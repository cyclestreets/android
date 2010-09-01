package uk.org.invisibility.cycloid;

import org.andnav.osm.util.BoundingBoxE6;

import android.content.Intent;

/*
 * Utility class for storing geocode bounding boxes in an Intent 
 */
public class GeoIntent
{
   public static BoundingBoxE6 getBoundingBoxFromExtras(Intent intent)
    {
        if
        (
        	intent.hasExtra("north")
        	&&
        	intent.hasExtra("east")
        	&& 
        	intent.hasExtra("south")
        	&&
        	intent.hasExtra("west")
        )
        {
        	return new BoundingBoxE6
        	(
    			intent.getIntExtra("north", 0),
    			intent.getIntExtra("east", 0),
    			intent.getIntExtra("south", 0),
    			intent.getIntExtra("west", 0)
        	);
        }
        else
        	return null;
    }

    public static void setBoundingBoxInExtras(Intent intent, BoundingBoxE6 bounds)
    {
      	intent.putExtra("north", bounds.getLatNorthE6());
    	intent.putExtra("south", bounds.getLatSouthE6());
    	intent.putExtra("east", bounds.getLonEastE6());
    	intent.putExtra("west", bounds.getLonWestE6());
    }	    
}
