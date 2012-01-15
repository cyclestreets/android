package net.cyclestreets.util;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.api.IGeoPoint;

public class GeoHelper
{
  private static final int Radius = 6371; // mean radius of Earth km

  static public double convertToRadians(double val) 
  {
    return val * Math.PI / 180;
  } // convertToRadians
  
  static public double boxWidthKm(final BoundingBoxE6 boundingBox)
  {
    final int Radius = 6371; // mean radius of Earth km
    final double dLat = convertToRadians(boundingBox.getLatitudeSpanE6() / 1E6);

    final double a = Math.pow(Math.sin(dLat/2.0), 2); 
    final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    final double d = Radius * c;

    return d;
  } // boxWidthKm
  
  static public double distanceBetween(final IGeoPoint p1, final IGeoPoint p2)
  {
    // uses the Haversine formula, which I only know about because 
    // http://www.movable-type.co.uk/scripts/latlong.html was the
    // first hit on my Google search
    final double lat1 = p1.getLatitudeE6() / 1E6;
    final double lon1 = p1.getLongitudeE6() / 1E6;
    final double lat2 = p2.getLatitudeE6() / 1E6;
    final double lon2 = p2.getLongitudeE6() / 1E6;

    final double dLat = convertToRadians(lat2-lat1);
    final double dLon = convertToRadians(lon2-lon1);
    final double rLat1 = convertToRadians(lat1);
    final double rLat2 = convertToRadians(lat2);

    final double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(rLat1) * Math.cos(rLat2); 
    final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    final double d = Radius * c;
    return d;
  } // distanceBetween
} // class GeoHelpers
