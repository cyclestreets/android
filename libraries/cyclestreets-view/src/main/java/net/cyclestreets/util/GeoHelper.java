package net.cyclestreets.util;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.api.IGeoPoint;

public class GeoHelper
{
  private static final int RadiusInMetres = 6378137; // mean radius of Earth km

  static public double boxWidthKm(final BoundingBoxE6 boundingBox)
  {
    final int Radius = 6371; // mean radius of Earth km
    final double dLat = Math.toRadians(boundingBox.getLatitudeSpanE6() / 1E6);

    final double a = Math.pow(Math.sin(dLat/2.0), 2); 
    final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    final double d = Radius * c;

    return d;
  } // boxWidthKm
  
  static public int distanceBetween(final IGeoPoint p1, final IGeoPoint p2)
  {
    // uses the Haversine formula, which I only know about because 
    // http://www.movable-type.co.uk/scripts/latlong.html was the
    // first hit on my Google search
    final double lat1 = p1.getLatitudeE6() / 1E6;
    final double lon1 = p1.getLongitudeE6() / 1E6;
    final double lat2 = p2.getLatitudeE6() / 1E6;
    final double lon2 = p2.getLongitudeE6() / 1E6;

    final double dLat = Math.toRadians(lat2-lat1);
    final double dLon = Math.toRadians(lon2-lon1);
    final double rLat1 = Math.toRadians(lat1);
    final double rLat2 = Math.toRadians(lat2);

    final double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(rLat1) * Math.cos(rLat2); 
    final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    final double d = RadiusInMetres * c;
    return (int)d;
  } // distanceBetween
  
  static public double bearingTo(final IGeoPoint p1, final IGeoPoint p2)
  {
    final double rlat1 = Math.toRadians(p1.getLatitudeE6() / 1E6);
    final double rlon1 = Math.toRadians(p1.getLongitudeE6() / 1E6);
    final double rlat2 = Math.toRadians(p2.getLatitudeE6() / 1E6);
    final double rlon2 = Math.toRadians(p2.getLongitudeE6() / 1E6);

    final double deltaLon = rlon2 - rlon1;

    final double y = Math.sin(deltaLon) * Math.cos(rlat2);
    final double x = Math.cos(rlat1) * Math.sin(rlat2) -
                     Math.sin(rlat1) * Math.cos(rlat2) * Math.cos(deltaLon);
    final double bearing = Math.atan2(y, x);
    final double normalised = (Math.toDegrees(bearing)+360) % 360;
    return Math.toRadians(normalised);
  } // bearingTo
  
  static public double crossTrack(final IGeoPoint p1, final IGeoPoint p2, final IGeoPoint location)
  {
    // how far from the line defined by p1p2 is location
    // http://www.movable-type.co.uk/scripts/latlong.html
    double distanceToLoc = distanceBetween(p1, location);
    double bp1l = bearingTo(p1, location);
    double bp1p2 = bearingTo(p1, p2);
    double ct = Math.asin(
        Math.sin(distanceToLoc/RadiusInMetres) *
        Math.sin(bp1l - bp1p2)
      ) * RadiusInMetres;
    
    return ct;
  } // crossTrack

  public static class AlongTrack
  {
    public enum Position {
      ON_TRACK,
      BEFORE_START,
      OFF_END
    }
    private final int offset_;
    private Position position_ = Position.ON_TRACK;
    
    AlongTrack(final int offset) {
      offset_ = offset;
    }
    void position(final Position newPos) { position_ = newPos; }
    
    public int offset() { return offset_; }
    public boolean onTrack() { return position_ == Position.ON_TRACK; }
    public Position position() { return position_; }
  } // AlongTrack
  
  static public AlongTrack alongTrackOffset(final IGeoPoint p1, final IGeoPoint p2, final IGeoPoint location)
  {
    final double distanceToLoc = distanceBetween(p1, location);
    final double crossTrack = crossTrack(p1, p2, location);
    double offset = Math.acos(
        Math.cos(distanceToLoc/RadiusInMetres) /
        Math.cos(crossTrack/RadiusInMetres)
      ) * RadiusInMetres;

    final AlongTrack at = new AlongTrack((int)offset); 
    
    final double p1p2 = bearingTo(p1, p2);
    double p1l = bearingTo(p1, location);
    double p2l = bearingTo(p2, location);
    
    p1l -= p1p2;
    p2l -= p1p2;
    
    double angle = Math.abs(p2l - p1l);
    if(angle > Math.PI)
      angle = (Math.PI*2) - angle;
    if(angle < (Math.PI/2)) 
      at.position((Math.abs(p1l) > Math.PI/2) ? AlongTrack.Position.BEFORE_START : AlongTrack.Position.OFF_END); 

    return at;
  } // alongTrackOffset
} // class GeoHelpers
