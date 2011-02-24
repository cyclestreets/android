package net.cyclestreets.content;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class RouteDatabase 
{
	private final SQLiteDatabase db_;
	
	public RouteDatabase(final Context context) 
	{ 
		DatabaseHelper dh = new DatabaseHelper(context);
		db_ = dh.getWritableDatabase();
	} // RouteDatabase
	
	public void addRoute(final int id, final String name, final String xml)
	{
	    final String ROUTE_TABLE_INSERT = 
    	    "INSERT INTO route (journey, name, xml, use_count) " +
    	    "  VALUES(?, ?, ?, 1)";
    
    	final SQLiteStatement insertRoute = db_.compileStatement(ROUTE_TABLE_INSERT);
    	insertRoute.bindLong(1, id);
    	insertRoute.bindString(2, name);
    	insertRoute.bindString(3, xml);
		insertRoute.executeInsert();
	} // addRoute
	
	public List<String> savedRoutes()
	{
        final List<String> routes = new ArrayList<String>();
        final Cursor cursor = db_.query(DatabaseHelper.ROUTE_TABLE_NAME, 
        								new String[] { "name" },
        								null, 
        								null, 
        								null, 
        								null, 
        								"use_count desc");
        if(cursor.moveToFirst()) 
           do 
           {
        	   routes.add(cursor.getString(0));
           } 
           while (cursor.moveToNext());
 
        if(cursor != null && !cursor.isClosed()) 
           cursor.close();
        
        return routes;
	} // savedRoutes
} // class RouteDatabase
