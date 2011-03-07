package net.cyclestreets.content;

import java.util.ArrayList;
import java.util.List;

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
		
	public void saveRoute(final int id, final String name, final String xml)
	{
		if(route(id) == null)
			addRoute(id, name, xml);
		else
			updateRoute(id);
	} // saveRoute
	
	private void addRoute(final int id, final String name, final String xml)
	{
	    final String ROUTE_TABLE_INSERT = 
    	    "INSERT INTO route (journey, name, xml, last_used) " +
    	    "  VALUES(?, ?, ?, datetime())";
    
    	final SQLiteStatement insertRoute = db_.compileStatement(ROUTE_TABLE_INSERT);
    	insertRoute.bindLong(1, id);
    	insertRoute.bindString(2, name);
    	insertRoute.bindString(3, xml);
		insertRoute.executeInsert();
	} // addRoute

	private void updateRoute(final int id)
	{
		final String ROUTE_TABLE_UPDATE = 
			"UPDATE route SET last_used = datetime() WHERE journey = ?";
		
		final SQLiteStatement update = db_.compileStatement(ROUTE_TABLE_UPDATE);
		update.bindLong(1, id);
		update.execute();
	} // updateRoute
	
	public void deleteRoute(final int id)
	{
		final String ROUTE_TABLE_DELETE = 
			"DELETE FROM route WHERE journey = ?";
		
		final SQLiteStatement delete = db_.compileStatement(ROUTE_TABLE_DELETE);
		delete.bindLong(1, id);
		delete.execute();
	} // deleteRoute
	
	public List<RouteSummary> savedRoutes()
	{
        final List<RouteSummary> routes = new ArrayList<RouteSummary>();
        final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE_NAME, 
        								new String[] { "journey", "name" },
        								null, 
        								null, 
        								null, 
        								null, 
        								"last_used desc");
        if(cursor.moveToFirst()) 
           do 
           {
        	   routes.add(new RouteSummary(cursor.getInt(0), cursor.getString(1)));
           } 
           while (cursor.moveToNext());
 
        if(!cursor.isClosed()) 
           cursor.close();
        
        return routes;
	} // savedRoutes

	public String route(final int routeId)
	{
        String r = null;
        final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE_NAME, 
        								new String[] { "xml" },
        								"journey=?", 
        								new String[] { Integer.toString(routeId) }, 
        								null, 
        								null, 
        								null);
        if(cursor.moveToFirst()) 
           do 
           {
        	   r = cursor.getString(0);
           } 
           while (cursor.moveToNext());
 
        if(!cursor.isClosed()) 
           cursor.close();
        
        return r;
	} // route

} // class RouteDatabase
