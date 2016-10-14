package net.cyclestreets.content;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Waypoints;

import org.osmdroid.api.IGeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

public class RouteDatabase 
{
  private final SQLiteDatabase db_;
  
  public RouteDatabase(final Context context) 
  { 
    DatabaseHelper dh = new DatabaseHelper(context);
    db_ = dh.getWritableDatabase();
  } // RouteDatabase
  
  public int routeCount()
  {
    final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE,
        new String[] { "count(" + BaseColumns._ID +")" },
                       null, 
                       null,
                       null,
                       null,
                       null);
    int c = 0;
    if(cursor.moveToFirst()) 
      do 
      {
        c = cursor.getInt(0);
      } 
      while (cursor.moveToNext());
  
    if(!cursor.isClosed()) 
      cursor.close();
         
    return c;
  } // count
    
  public void saveRoute(final Journey journey,
                        final String xml)
  {
    if(route(journey.itinerary(), journey.plan()) == null)
      addRoute(journey, xml);
    else
      updateRoute(journey);
  } // saveRoute
  
  private void addRoute(final Journey journey,
                        final String xml)
  {
    final String ROUTE_TABLE_INSERT = 
          "INSERT INTO route (journey, name, plan, distance, waypoints, xml, last_used) " +
          "  VALUES(?, ?, ?, ?, ?, ?, datetime())";
    
    final SQLiteStatement insertRoute = db_.compileStatement(ROUTE_TABLE_INSERT);
    insertRoute.bindLong(1, journey.itinerary());
    insertRoute.bindString(2, journey.name());
    insertRoute.bindString(3, journey.plan());
    insertRoute.bindLong(4, journey.total_distance());
    insertRoute.bindString(5, flattenWaypoints(journey.waypoints()));
    insertRoute.bindString(6, xml);
    insertRoute.executeInsert();
  } // addRoute
  
  private void updateRoute(final Journey journey)
  {
    final String ROUTE_TABLE_UPDATE = 
      "UPDATE route SET last_used = datetime() WHERE journey = ? and plan = ?";
    
    final SQLiteStatement update = db_.compileStatement(ROUTE_TABLE_UPDATE);
    update.bindLong(1, journey.itinerary());
    update.bindString(2, journey.plan());
    update.execute();
  } // updateRoute
  
  public void renameRoute(final int localId, final String newName)
  {
    final String ROUTE_TABLE_RENAME =
      "UPDATE route SET name = ? WHERE " + BaseColumns._ID + " = ?";
    final SQLiteStatement update = db_.compileStatement(ROUTE_TABLE_RENAME);
    update.bindString(1, newName);
    update.bindLong(2, localId);
    update.execute();    
  } // renameRoute
  
  public void deleteRoute(final int localId)
  {
    final String ROUTE_TABLE_DELETE = 
      "DELETE FROM route WHERE " + BaseColumns._ID + " = ?";
    
    final SQLiteStatement delete = db_.compileStatement(ROUTE_TABLE_DELETE);
    delete.bindLong(1, localId);
    delete.execute();
  } // deleteRoute
  
  public List<RouteSummary> savedRoutes()
  {
    final List<RouteSummary> routes = new ArrayList<>();
    final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE,
        new String[] { BaseColumns._ID, "journey", "name", "plan", "distance" },
                        null, 
                        null, 
                        null, 
                        null, 
                        "last_used desc");       
    if(cursor.moveToFirst()) 
      do 
      {
        routes.add(new RouteSummary(cursor.getInt(0),
                                    cursor.getInt(1), 
                                    cursor.getString(2),
                                    cursor.getString(3),
                                    cursor.getInt(4)));
      } 
      while (cursor.moveToNext());
    
    if(!cursor.isClosed()) 
      cursor.close();
        
    return routes;
  } // savedRoutes
  
  public RouteData route(final int localId)
  {
    return fetchRoute(BaseColumns._ID + "=?", 
                new String[] { Integer.toString(localId) });
  } // route    
  
  public RouteData route(final int itinerary, final String plan)
  {
    return fetchRoute("journey=? and plan=?", 
                  new String[] { Integer.toString(itinerary), plan });
  } // route

  private RouteData fetchRoute(final String filter, final String[] bindParams)
  {
    RouteData r = null;
    final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE,
                  new String[] { "xml", 
                        "waypoints",
                        "name"},
                        filter, 
                        bindParams, 
                        null, 
                        null, 
                        null);
    if(cursor.moveToFirst()) 
      do 
      {
        r = new RouteData(cursor.getString(0),
                          expandWaypoints(cursor.getString(1)),
                          cursor.getString(2));
      } 
      while (cursor.moveToNext());
 
    if(!cursor.isClosed()) 
      cursor.close();
        
    return r;
  } // fetchRoute
  
  private String flattenWaypoints(final Waypoints waypoints)
  {
    final StringBuilder sb = new StringBuilder();
    for(final IGeoPoint waypoint : waypoints)
    {
      if(sb.length() != 0)
        sb.append('|');
      sb.append(waypoint.getLatitudeE6())
        .append(',')
        .append(waypoint.getLongitudeE6());
    } // for ...
    return sb.toString();
  } // flattenWaypoints

  private Waypoints expandWaypoints(final String str)
  {
    final Waypoints points = new Waypoints();
    for(final String coords : str.split("\\|"))
    {
      final String[] latlon = coords.split(",");
      final double lat = Long.parseLong(latlon[0])/1E6;
      final double lon = Long.parseLong(latlon[1])/1E6;
      
      points.add(lat, lon);
    } // for ... 
    return points;
  } // expandWaypoints
} // class RouteDatabase
