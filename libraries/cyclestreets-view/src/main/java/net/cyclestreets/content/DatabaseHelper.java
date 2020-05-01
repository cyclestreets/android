package net.cyclestreets.content;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;
import net.cyclestreets.api.client.JourneyStringTransformerKt;
import net.cyclestreets.util.Logging;
import net.cyclestreets.view.BuildConfig;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

class DatabaseHelper extends SQLiteOpenHelper {
  private static final String TAG = Logging.getTag(DatabaseHelper.class);

  static final String DATABASE_NAME = "cyclestreets.db";
  // If you're upgrading the DB version, then be sure to grab a snapshot - see the DatabaseUpgradeTest.
  static final int DATABASE_VERSION = 4;
  static final String ROUTE_TABLE = "route";
  static final String LOCATION_TABLE = "location";
  static final String ROUTE_TABLE_OLD = "_" + ROUTE_TABLE + "_old";
  static final String LOCATION_TABLE_OLD = "_" + LOCATION_TABLE + "_old";

  private static final String ROUTE_TABLE_CREATE =
        "CREATE TABLE route (" + BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                             " journey INTEGER, " +
                             " last_used DATE, " +
                             " name TEXT, " +
                             " plan TEXT, " +
                             " distance INTEGER, " +
                             " waypoints TEXT, " +
                             " journey_json TEXT " +
                             " ) ";

  private static final String LOCATION_TABLE_CREATE =
        "CREATE TABLE location (" + BaseColumns._ID + " INTEGER PRIMARY KEY, "  +
                                " name TEXT, " +
                                " lat TEXT, " +
                                " lon TEXT " +
                                " ) ";

  DatabaseHelper(final Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

    // From Android 9 (API version 28, SQLite version 3.22.0) Compatibility WAL is enabled by default.
    // This breaks our testing mechanism of pasting over a new version of the database, so disable
    // it when we're in debug mode.  See:
    // -  https://source.android.com/devices/tech/perf/compatibility-wal
    // -  https://www.sqlite.org/wal.html
    // -  https://www.sqlite.org/tempfiles.html
    if (BuildConfig.DEBUG) {
      this.setWriteAheadLoggingEnabled(false);
    }
  }

  @Override
  public void onCreate(final SQLiteDatabase db) {
    db.execSQL(ROUTE_TABLE_CREATE);
    db.execSQL(LOCATION_TABLE_CREATE);
  }

  @Override
  public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    Log.d(TAG, "onUpgrade - from " + oldVersion + " to " + newVersion);
    if (oldVersion < 2)
      upgradeTo2(db);
    if (oldVersion < 3)
      upgradeTo3(db);
    if (oldVersion < 4)
      upgradeTo4(db);
  }

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
      if (cursor.moveToFirst())
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

      if (!cursor.isClosed())
        cursor.close();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private void upgradeTo3(final SQLiteDatabase db) {
    db.execSQL(LOCATION_TABLE_CREATE);
  }

  private void upgradeTo4(final SQLiteDatabase db) {
    db.execSQL("ALTER TABLE " + ROUTE_TABLE + " RENAME TO " + ROUTE_TABLE_OLD);
    db.execSQL(ROUTE_TABLE_CREATE);
    db.execSQL("INSERT INTO " + ROUTE_TABLE + " (" + BaseColumns._ID + ", journey, last_used, name, plan, distance, waypoints, journey_json)\n" +
        "  SELECT " + BaseColumns._ID + ", journey, last_used, name, plan, distance, waypoints, xml\n" +
        "  FROM " + ROUTE_TABLE_OLD + ";");

    try {
      final Cursor cursor = db.query(ROUTE_TABLE,
                                     new String[] { BaseColumns._ID, "journey_json", "waypoints"},
                                     null,
                                     null,
                                     null,
                                     null,
                                     null);
      if (cursor.moveToFirst()) {
        do {
          final String v1ApiJourneyXml = cursor.getString(1);
          final String journeyJson = JourneyStringTransformerKt.fromV1ApiXml(v1ApiJourneyXml);
          final String e6Waypoints = cursor.getString(2);
          final String newWaypoints = RouteDatabase.serializeWaypoints(deserializeE6Waypoints(e6Waypoints));

          final String updateStmt = "UPDATE " + ROUTE_TABLE + " SET journey_json = ?, waypoints = ? " +
              " WHERE " + BaseColumns._ID + " = " + cursor.getInt(0);
          final SQLiteStatement update = db.compileStatement(updateStmt);
          update.bindString(1, journeyJson);
          update.bindString(2, newWaypoints);
          update.execute();
        } while (cursor.moveToNext());
      }
      if (!cursor.isClosed())
        cursor.close();
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }

    db.execSQL("ALTER TABLE " + LOCATION_TABLE + " RENAME TO " + LOCATION_TABLE_OLD);
    db.execSQL(LOCATION_TABLE_CREATE);
    db.execSQL("INSERT INTO " + LOCATION_TABLE + " (" + BaseColumns._ID + ", name, lat, lon)\n" +
        "  SELECT " + BaseColumns._ID + ", name, lat, lon\n" +
        "  FROM " + LOCATION_TABLE_OLD + ";");

    try {
      final Cursor cursor = db.query(LOCATION_TABLE,
          new String[] { BaseColumns._ID, "lat", "lon"},
          null,
          null,
          null,
          null,
          null);
      if (cursor.moveToFirst()) {
        do {
          double lat = Long.parseLong(cursor.getString(1)) / 1E6;
          double lon = Long.parseLong(cursor.getString(2)) / 1E6;

          final String updateStmt = "UPDATE " + LOCATION_TABLE + " SET lat = ?, lon = ? " +
              " WHERE " + BaseColumns._ID + " = " + cursor.getInt(0);
          final SQLiteStatement update = db.compileStatement(updateStmt);
          update.bindString(1, String.valueOf(lat));
          update.bindString(2, String.valueOf(lon));
          update.execute();
        } while (cursor.moveToNext());
      }
      if (!cursor.isClosed())
        cursor.close();
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Helper function that parses a given table into a string
   * and returns it for easy printing. The string consists of
   * the table name and then each row is iterated through with
   * column_name: value pairs printed out.
   *
   * Courtesy of https://stackoverflow.com/a/27003490/2108057
   *
   * @param db the database to get the table from
   * @param tableName the the name of the table to parse
   */
  static void logTableContents(SQLiteDatabase db, String tableName) {
    Log.i(TAG, String.format("Table %s:\n", tableName));
    Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
    if (allRows.moveToFirst() ){
      String[] columnNames = allRows.getColumnNames();
      do {
        StringBuilder rowSb = new StringBuilder();
        for (String name: columnNames) {
          rowSb.append(String.format("%s: %s\n", name, allRows.getString(allRows.getColumnIndex(name))));
        }
        Log.d(TAG, rowSb.toString());
      } while (allRows.moveToNext());
    }
    allRows.close();
  }

  private static List<IGeoPoint> deserializeE6Waypoints(String serializedWaypoints) {
    List<IGeoPoint> waypoints = new ArrayList<>();
    for (final String coords : serializedWaypoints.split("\\|")) {
      final String[] latlon = coords.split(",", 2);
      double lat = Long.parseLong(latlon[0]) / 1E6;
      double lon = Long.parseLong(latlon[1]) / 1E6;
      Log.d(TAG, "dWE6: lat=" + lat + ", lon=" + lon);
      waypoints.add(new GeoPoint(lat, lon));
    }
    return waypoints;
  }
}
