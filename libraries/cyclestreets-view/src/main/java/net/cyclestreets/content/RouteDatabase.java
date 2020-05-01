package net.cyclestreets.content;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Waypoints;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;
import net.cyclestreets.util.Logging;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class RouteDatabase
{
  private static final String TAG = Logging.getTag(RouteDatabase.class);
  private final SQLiteDatabase db;

  public RouteDatabase(final Context context) {
    DatabaseHelper dh = new DatabaseHelper(context);
    db = dh.getWritableDatabase();
  }

  public int routeCount() {
    final Cursor cursor = db.query(DatabaseHelper.ROUTE_TABLE,
        new String[] { "count(" + BaseColumns._ID +")" },
                       null,
                       null,
                       null,
                       null,
                       null);
    int c = 0;
    if (cursor.moveToFirst())
      do  {
        c = cursor.getInt(0);
      }
      while (cursor.moveToNext());

    if (!cursor.isClosed())
      cursor.close();

    return c;
  }

  public void saveRoute(final Journey journey,
                        final String json) {
    if (route(journey.itinerary(), journey.plan()) == null)
      addRoute(journey, json);
    else
      updateRoute(journey);
  }

  private void addRoute(final Journey journey,
                        final String json) {
    final String ROUTE_TABLE_INSERT =
          "INSERT INTO route (journey, name, plan, distance, waypoints, journey_json, last_used) " +
          "  VALUES(?, ?, ?, ?, ?, ?, datetime())";

    final SQLiteStatement insertRoute = db.compileStatement(ROUTE_TABLE_INSERT);
    insertRoute.bindLong(1, journey.itinerary());
    insertRoute.bindString(2, journey.name());
    insertRoute.bindString(3, journey.plan());
    insertRoute.bindLong(4, journey.totalDistance());
    insertRoute.bindString(5, serializeWaypoints(journey.getWaypoints()));
    insertRoute.bindString(6, json);
    insertRoute.executeInsert();
  }

  private void updateRoute(final Journey journey) {
    final String ROUTE_TABLE_UPDATE =
      "UPDATE route SET last_used = datetime() WHERE journey = ? and plan = ?";

    final SQLiteStatement update = db.compileStatement(ROUTE_TABLE_UPDATE);
    update.bindLong(1, journey.itinerary());
    update.bindString(2, journey.plan());
    update.execute();
  }

  public void renameRoute(final int localId, final String newName) {
    final String ROUTE_TABLE_RENAME =
      "UPDATE route SET name = ? WHERE " + BaseColumns._ID + " = ?";
    final SQLiteStatement update = db.compileStatement(ROUTE_TABLE_RENAME);
    update.bindString(1, newName);
    update.bindLong(2, localId);
    update.execute();
  }

  public void deleteRoute(final int localId) {
    final String ROUTE_TABLE_DELETE =
      "DELETE FROM route WHERE " + BaseColumns._ID + " = ?";

    final SQLiteStatement delete = db.compileStatement(ROUTE_TABLE_DELETE);
    delete.bindLong(1, localId);
    delete.execute();
  }

  public List<RouteSummary> savedRoutes() {
    final List<RouteSummary> routes = new ArrayList<>();
    final Cursor cursor = db.query(DatabaseHelper.ROUTE_TABLE,
        new String[] { BaseColumns._ID, "journey", "name", "plan", "distance" },
                        null,
                        null,
                        null,
                        null,
                        "last_used desc");
    if (cursor.moveToFirst())
      do  {
        routes.add(new RouteSummary(cursor.getInt(0),
                                    cursor.getInt(1),
                                    cursor.getString(2),
                                    cursor.getString(3),
                                    cursor.getInt(4)));
      }
      while (cursor.moveToNext());

    if (!cursor.isClosed())
      cursor.close();

    return routes;
  }

  public RouteData route(final int localId) {
    return fetchRoute(BaseColumns._ID + "=?",
                new String[] { Integer.toString(localId) });
  }

  public RouteData route(final int itinerary, final String plan) {
    return fetchRoute("journey=? and plan=?",
                  new String[] { Integer.toString(itinerary), plan });
  }

  private RouteData fetchRoute(final String filter, final String[] bindParams) {
    RouteData r = null;
    final Cursor cursor = db.query(DatabaseHelper.ROUTE_TABLE,
                  new String[] { "journey_json",
                        "waypoints",
                        "name"},
                        filter,
                        bindParams,
                        null,
                        null,
                        null);
    if (cursor.moveToFirst())
      do  {
        r = new RouteData(cursor.getString(0),
                          new Waypoints(deserializeWaypoints(cursor.getString(1))),
                          cursor.getString(2));
      }
      while (cursor.moveToNext());

    if (!cursor.isClosed())
      cursor.close();

    return r;
  }

  public static String serializeWaypoints(Iterable<IGeoPoint> waypoints) {
    final StringBuilder sb = new StringBuilder();
    for (final IGeoPoint waypoint : waypoints) {
      if (sb.length() != 0)
        sb.append('|');
      sb.append(waypoint.getLatitude())
              .append(",")
              .append(waypoint.getLongitude());
    }
    String wpString = sb.toString();
    Log.d(TAG, "sW: " + wpString);
    return wpString;
  }

  public static List<IGeoPoint> deserializeWaypoints(String serializedWaypoints) {
    List<IGeoPoint> waypoints = new ArrayList<>();
    for (final String coords : serializedWaypoints.split("\\|")) {
      final String[] latlon = coords.split(",", 2);
      double lat = Double.parseDouble(latlon[0]);
      double lon = Double.parseDouble(latlon[1]);
      Log.d(TAG, "dW: lat=" + lat + ", lon=" + lon);
      waypoints.add(new GeoPoint(lat, lon));
    }
    return waypoints;
  }
}
