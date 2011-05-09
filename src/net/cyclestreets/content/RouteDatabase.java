package net.cyclestreets.content;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

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
		final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE_NAME, 
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
		
	public void saveRoute(final int itinerary, 
						  final String name, 
						  final String plan,
						  final int distance,
						  final String xml, 
						  final GeoPoint start, 
						  final GeoPoint end)
	{
		if(route(itinerary, plan) == null)
			addRoute(itinerary, name, plan, distance, xml, start, end);
		else
			updateRoute(itinerary, plan);
	} // saveRoute
	
	private void addRoute(final int itinerary, 
						  final String name, 
						  final String plan,
						  final int distance,
						  final String xml,
						  final GeoPoint start,
						  final GeoPoint end)
	{
	    final String ROUTE_TABLE_INSERT = 
    	    "INSERT INTO route (journey, name, plan, distance, xml, start_lat, start_long, end_lat, end_long, last_used) " +
    	    "  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, datetime())";
    
    	final SQLiteStatement insertRoute = db_.compileStatement(ROUTE_TABLE_INSERT);
    	insertRoute.bindLong(1, itinerary);
    	insertRoute.bindString(2, name);
    	insertRoute.bindString(3, plan);
    	insertRoute.bindLong(4, distance);
    	insertRoute.bindString(5, xml);
    	insertRoute.bindLong(6, start.getLatitudeE6());
    	insertRoute.bindLong(7, start.getLongitudeE6());
    	insertRoute.bindLong(8, end.getLatitudeE6());
    	insertRoute.bindLong(9, end.getLongitudeE6());
		insertRoute.executeInsert();
	} // addRoute

	private void updateRoute(final int itinerary,
							 final String plan)
	{
		final String ROUTE_TABLE_UPDATE = 
			"UPDATE route SET last_used = datetime() WHERE journey = ? and plan = ?";
		
		final SQLiteStatement update = db_.compileStatement(ROUTE_TABLE_UPDATE);
		update.bindLong(1, itinerary);
		update.bindString(2, plan);
		update.execute();
	} // updateRoute
	
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
        final List<RouteSummary> routes = new ArrayList<RouteSummary>();
        final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE_NAME, 
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
        final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE_NAME, 
        								new String[] { "xml", 
        											   "start_lat", "start_long", 
        											   "end_lat", "end_long" },
        								filter, 
        								bindParams, 
        								null, 
        								null, 
        								null);
        if(cursor.moveToFirst()) 
           do 
           {
        	   r = new RouteData(cursor.getString(0),
        			   			 new GeoPoint(cursor.getInt(1), cursor.getInt(2)),
        			   			 new GeoPoint(cursor.getInt(3), cursor.getInt(4)));
           } 
           while (cursor.moveToNext());
 
        if(!cursor.isClosed()) 
           cursor.close();
        
        return r;
	} // fetchRoute
} // class RouteDatabase
