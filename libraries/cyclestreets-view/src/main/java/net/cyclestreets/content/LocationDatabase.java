package net.cyclestreets.content;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

public class LocationDatabase {
  private final SQLiteDatabase db_;

  public LocationDatabase(final Context context) {
    DatabaseHelper dh = new DatabaseHelper(context);
    db_ = dh.getWritableDatabase();
  }

  public void close() {
    db_.close();
  }

  public int locationCount() {
    final Cursor cursor = db_.query(DatabaseHelper.LOCATION_TABLE,
        new String[] { "count(" + BaseColumns._ID +")" },
        null,
        null,
        null,
        null,
        null);
    int c = 0;
    if (cursor.moveToFirst())
      do {
        c = cursor.getInt(0);
      } while (cursor.moveToNext());

    if (!cursor.isClosed())
      cursor.close();

    return c;
  }

  public void addLocation(final String name, final IGeoPoint where) {
    final String LOCATION_TABLE_INSERT =
        "INSERT INTO location (name, lat, lon) " +
            "  VALUES(?, ?, ?)";

    final SQLiteStatement insert = db_.compileStatement(LOCATION_TABLE_INSERT);
    insert.bindString(1, name);
    insert.bindString(2, String.valueOf(where.getLatitude()));
    insert.bindString(3, String.valueOf(where.getLongitude()));
    insert.executeInsert();
  }

  public void updateLocation(final int localId, final String name, final IGeoPoint where) {
    final String LOCATION_TABLE_UPDATE =
        "UPDATE location SET name = ?, lat = ?, lon = ? WHERE " + BaseColumns._ID + " = ?";

    final SQLiteStatement update = db_.compileStatement(LOCATION_TABLE_UPDATE);
    update.bindString(1, name);
    update.bindString(2, String.valueOf(where.getLatitude()));
    update.bindString(3, String.valueOf(where.getLongitude()));
    update.bindLong(4, localId);
    update.execute();
  }

  public void deleteLocation(final int localId) {
    final String LOCATION_TABLE_DELETE =
        "DELETE FROM location WHERE " + BaseColumns._ID + " = ?";

    final SQLiteStatement delete = db_.compileStatement(LOCATION_TABLE_DELETE);
    delete.bindLong(1, localId);
    delete.execute();
  }

  public SavedLocation savedLocation(int localId) {
    List<SavedLocation> locs = locations(BaseColumns._ID + " = ?", new String[] { Integer.toString(localId)});
    return locs.size() != 0 ? locs.get(0) : null;
  }

  public List<SavedLocation> savedLocations() {
    return locations(null, null);
  }

  private List<SavedLocation> locations(String where, String[] whereArgs) {
    final List<SavedLocation> locations = new ArrayList<>();
    final Cursor cursor = db_.query(DatabaseHelper.LOCATION_TABLE,
        new String[] { BaseColumns._ID, "name", "lat", "lon" },
        where,
        whereArgs,
        null,
        null,
        "name");
    if (cursor.moveToFirst())
      do {
        locations.add(new SavedLocation(
            cursor.getInt(0),
            cursor.getString(1),
            Double.parseDouble(cursor.getString(2)),
            Double.parseDouble(cursor.getString(3))));
      } while (cursor.moveToNext());

    if (!cursor.isClosed())
      cursor.close();

    return locations;
  }
}
