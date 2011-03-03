package net.cyclestreets.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

class DatabaseHelper extends SQLiteOpenHelper 
{
    private static final String DATABASE_NAME = "cyclestreets.db";
    private static final int DATABASE_VERSION = 1;
    public static final String ROUTE_TABLE_NAME = "route";
    
    private static final String ROUTE_TABLE_CREATE = 
    		"CREATE TABLE route (" + BaseColumns._ID + " INTEGER PRIMARY KEY, " +
    		                     " journey INTEGER, " +
    		                     " last_used DATE, " +
    		                     " name TEXT, " +
    		                     " xml TEXT) ";
    DatabaseHelper(final Context context) 
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    } // DatabaseHelper
    
	@Override
	public void onCreate(final SQLiteDatabase db) 
	{
		db.execSQL(ROUTE_TABLE_CREATE);
	} // onCreate
	
	@Override
	public void onOpen(final SQLiteDatabase db)
	{
	} // onOpen

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) 
	{
		throw new RuntimeException("DatabaseHelper.onUpgrade - whoops!");
	} // onUpgrade
} // class DatabaseHelper
