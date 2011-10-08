package net.cyclestreets.util;

import org.osmdroid.util.BoundingBoxE6;

public class GeoHelper
{
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
} // class GeoHelpers
