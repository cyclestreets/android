package net.cyclestreets.content;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DatabaseUpgradeTest {

  private static final String TAG = DatabaseUpgradeTest.class.getCanonicalName();

  /**
   * This test runs through all the database versions from the /androidTest/assets/ folder. It copies the old database to the file path of the application.
   * It tests that the database upgrades to the correct version.
   * If there is an issue with the upgrade, generally a SQLiteException will be thrown and the test will fail.
   * for example:
   * android.database.sqlite.SQLiteException: duplicate column name: calculated_pages_times_rating (code 1): , while compiling: ALTER TABLE book_information ADD COLUMN calculated_pages_times_rating INTEGER;
   *
   * @throws IOException if the database cannot be copied.
   */
  @Test
  public void testDatabaseUpgrades() throws IOException {
    for (int i = 3; i < DatabaseHelper.DATABASE_VERSION; i++) {
      Log.d(TAG, "Testing upgrade from version:" + i);
      copyDatabase(i);

      DatabaseHelper databaseHelper = new DatabaseHelper(InstrumentationRegistry.getTargetContext());
      Log.d(TAG, " New Database Version:" + databaseHelper.getWritableDatabase().getVersion());
      Assert.assertEquals(DatabaseHelper.DATABASE_VERSION, databaseHelper.getWritableDatabase().getVersion());
    }

  }


  private void copyDatabase(int version) throws IOException {
    String dbPath = InstrumentationRegistry.getTargetContext().getDatabasePath(DatabaseHelper.DATABASE_NAME).getAbsolutePath();

    String dbName = String.format("cyclestreets_v%d.db", version);
    InputStream mInput = InstrumentationRegistry.getContext().getAssets().open(dbName);

    File db = new File(dbPath);
    if (!db.exists()){
      db.getParentFile().mkdirs();
      db.createNewFile();
    }
    OutputStream mOutput = new FileOutputStream(dbPath);
    byte[] mBuffer = new byte[1024];
    int mLength;
    while ((mLength = mInput.read(mBuffer)) > 0) {
      mOutput.write(mBuffer, 0, mLength);
    }
    mOutput.flush();
    mOutput.close();
    mInput.close();
  }
}