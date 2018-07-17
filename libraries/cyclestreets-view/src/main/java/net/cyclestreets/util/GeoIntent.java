package net.cyclestreets.util;

import java.util.List;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.routing.Waypoints;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.content.Intent;
import android.location.Location;

public class GeoIntent
{
  private static final String EXTRA_NORTH = "bounds-north";
  private static final String EXTRA_EAST = "bounds-east";
  private static final String EXTRA_SOUTH = "bounds-south";
  private static final String EXTRA_WEST = "bounds-west";

  private static final String WAYPOINT = "WP";

  private static final String GEO_LATITUDE = "latitude";
  private static final String GEO_LONGITUDE = "longitude";

  private static final String GEO_NAME = "place-name";
  private static final String GEO_NEAR = "place-near";

  public static BoundingBoxE6 getBoundingBox(final Intent intent) {
    if (intent.hasExtra(EXTRA_NORTH)  &&
       intent.hasExtra(EXTRA_EAST) &&
       intent.hasExtra(EXTRA_SOUTH) &&
       intent.hasExtra(EXTRA_WEST)) {
      return new BoundingBoxE6(
            intent.getIntExtra(EXTRA_NORTH, 0),
            intent.getIntExtra(EXTRA_EAST, 0),
            intent.getIntExtra(EXTRA_SOUTH, 0),
            intent.getIntExtra(EXTRA_WEST, 0));
    }

    return null;
  }

  public static void setBoundingBox(final Intent intent,
                                    final BoundingBoxE6 bounds) {
    intent.putExtra(EXTRA_NORTH, bounds.getLatNorthE6());
    intent.putExtra(EXTRA_EAST, bounds.getLonEastE6());
    intent.putExtra(EXTRA_SOUTH, bounds.getLatSouthE6());
    intent.putExtra(EXTRA_WEST, bounds.getLonWestE6());
  }

  //////////////////////////////////////////////
  public static GeoPoint getGeoPoint(final Intent intent) {
    return getGeoPoint(intent, "");
  }

  public static GeoPoint getGeoPoint(final Intent intent, final String prefix) {
    if (intent.hasExtra(prefix+GEO_LATITUDE) &&
       intent.hasExtra(prefix+GEO_LONGITUDE)) {
      int lat = intent.getIntExtra(prefix+GEO_LATITUDE, 0);
      int lon = intent.getIntExtra(prefix+GEO_LONGITUDE, 0);

      return new GeoPoint(lat, lon);
    }

    return null;
  }

  public static void setGeoPoint(final Intent intent, final IGeoPoint point) {
    setGeoPoint(intent, "", point);
  }
  public static void setGeoPoint(final Intent intent,
                                 final String prefix,
                                 final IGeoPoint point) {
    if (point == null)
      return;
    intent.putExtra(prefix+GEO_LATITUDE, point.getLatitudeE6());
    intent.putExtra(prefix+GEO_LONGITUDE, point.getLongitudeE6());
  }

  public static void setLocation(final Intent intent,
                                 final Location location) {
    if (location == null)
      return;
    intent.putExtra(GEO_LATITUDE, (int)(location.getLatitude() * 1E6));
     intent.putExtra(GEO_LONGITUDE, (int)(location.getLongitude() * 1E6));
  }

  public static Waypoints getWaypoints(final Intent intent) {
    final Waypoints points = new Waypoints();
    for (int waypoints = 0; ; ++waypoints) {
      final GeoPoint wp = getWaypoint(intent, waypoints);
      if (wp == null)
        break;
      points.add(wp);
    }
    return points;
  }

  public static void setWaypoints(final Intent intent, final Waypoints points) {
    for (int i = 0; i != points.count(); ++i)
      setWaypoint(intent, i, points.get(i));
  }

  public static void setWaypointsFromPlaces(final Intent intent, final List<GeoPlace> places) {
    for (int i = 0; i != places.size(); ++i)
      setWaypoint(intent, i, places.get(i).coord());
  }

  private static GeoPoint getWaypoint(final Intent intent, final int index) {
    return getGeoPoint(intent, WAYPOINT + index);
  }

  private static void setWaypoint(final Intent intent,
                                 final int index,
                                 final IGeoPoint point) {
    setGeoPoint(intent, WAYPOINT + index, point);
  }

  //////////////////////////////////////////////
  public static GeoPlace getGeoPlace(final Intent intent) {
    final GeoPoint point = getGeoPoint(intent);
     final String name = intent.getStringExtra(GEO_NAME);
     final String near = intent.getStringExtra(GEO_NEAR);

     if ((point == null) || (name == null) || (near == null))
       return null;
     return new GeoPlace(point, name, near);
  }

  public static void setGeoPlace(final Intent intent, final GeoPlace place) {
     setGeoPoint(intent, place.coord());
     intent.putExtra(GEO_NAME, place.name());
     intent.putExtra(GEO_NEAR, place.near());
  }
}
