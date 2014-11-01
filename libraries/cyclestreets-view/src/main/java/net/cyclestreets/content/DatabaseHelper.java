package net.cyclestreets.content;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

class DatabaseHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "cyclestreets.db";
  private static final int DATABASE_VERSION = 3;
  public static final String ROUTE_TABLE = "route";
  public static final String LOCATION_TABLE = "location";

    
  private static final String ROUTE_TABLE_CREATE = 
    		"CREATE TABLE route (" + BaseColumns._ID + " INTEGER PRIMARY KEY, " +
    		                     " journey INTEGER, " +
    		                     " last_used DATE, " +
    		                     " name TEXT, " +
    		                     " plan TEXT, " +
    		                     " distance INTEGER, " +
    		                     " waypoints TEXT, " +
    		                     " xml TEXT " +
    		                     " ) ";

  private static final String LOCATIONS_TABLE_CREATE =
        "CREATE TABLE location (" + BaseColumns._ID + " INTEGER PRIMARY KEY, "  +
                                " name TEXT, " +
                                " lat INTEGER, " +
                                " lon INTEGER " +
                                " ) ";

  DatabaseHelper(final Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  } // DatabaseHelper
    
	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL(ROUTE_TABLE_CREATE);
    db.execSQL(LOCATIONS_TABLE_CREATE);
	} // onCreate
	
	@Override
	public void onOpen(final SQLiteDatabase db)	{
	} // onOpen

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
	  if (oldVersion < 2)
	    upgradeTo2(db);
    if (oldVersion < 3)
      upgradeTo3(db);
	} // onUpgrade
	
	private void upgradeTo2(final SQLiteDatabase db) {
	  try {
  	  db.execSQL("ALTER TABLE route ADD COLUMN waypoints TEXT");
      final Cursor cursor = db.query(ROUTE_TABLE,
          new String[] { BaseColumns._ID, "start_lat", "start_long", "end_lat", "end_long" },
          null, 
          null, 
          null, 
          null, 
          null);
      if(cursor.moveToFirst()) 
        do {
          final StringBuilder sb = new StringBuilder();
          sb.append("UPDATE route SET waypoints='")
            .append(cursor.getInt(1))
            .append(',')
            .append(cursor.getInt(2))
            .append('|')
            .append(cursor.getInt(3))
            .append(',')
            .append(cursor.getInt(4))
            .append("' WHERE ")
            .append(BaseColumns._ID).append(" = ").append(cursor.getInt(0));
  
          final String updateStmt = sb.toString();
          db.compileStatement(updateStmt).execute();
        } while (cursor.moveToNext());
  
      if(!cursor.isClosed()) 
        cursor.close();
	  } catch(Exception e) {
	    System.out.println(e.getMessage());
	  }
	} // upgradeTo2

  private void upgradeTo3(final SQLiteDatabase db) {
    db.execSQL(LOCATIONS_TABLE_CREATE);
  } // upgradeTo3
} // class DatabaseHelper
