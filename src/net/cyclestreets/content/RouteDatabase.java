package net.cyclestreets.content;

import android.content.Context;

public class RouteDatabase 
{
	private final DatabaseHelper db_;
	
	public RouteDatabase(final Context context) 
	{ 
		db_ = new DatabaseHelper(context);
	} // RouteDatabase
} // class RouteDatabase
