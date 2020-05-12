package net.cyclestreets.content;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;
import net.cyclestreets.CycleStreets;
import net.cyclestreets.util.Logging;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

import static net.cyclestreets.content.DatabaseHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DatabaseUpgradeTest {
  private static final String TAG = Logging.getTag(DatabaseUpgradeTest.class);

  @Rule
  public ActivityTestRule<CycleStreets> mActivityRule = new ActivityTestRule<>(CycleStreets.class);

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
  public void testDatabaseUpgrades() throws IOException, InterruptedException {
    for (int i = 3; i < DatabaseHelper.DATABASE_VERSION; i++) {
      Log.d(TAG, "Testing upgrade from version:" + i);
      copyDatabase(i);

      DatabaseHelper databaseHelper = new DatabaseHelper(InstrumentationRegistry.getTargetContext());
      Log.d(TAG, " New Database Version:" + databaseHelper.getWritableDatabase().getVersion());
      Assert.assertEquals(DatabaseHelper.DATABASE_VERSION, databaseHelper.getWritableDatabase().getVersion());
      logTableContents(databaseHelper.getReadableDatabase(), ROUTE_TABLE_OLD);
      logTableContents(databaseHelper.getReadableDatabase(), ROUTE_TABLE);
      logTableContents(databaseHelper.getReadableDatabase(), LOCATION_TABLE_OLD);
      logTableContents(databaseHelper.getReadableDatabase(), LOCATION_TABLE);
    }

    // After upgrading the DB, uncomment these lines and grab the next version so future testing can be perfornmed!
    Log.i(TAG, "Sleeping to give a chance to run: ./adb pull /data/data/net.cyclestreets/databases/cyclestreets.db");
    Thread.sleep(240000L);
  }

  private void copyDatabase(int version) throws IOException {
    String dbPath = InstrumentationRegistry.getTargetContext().getDatabasePath(DatabaseHelper.DATABASE_NAME).getAbsolutePath();

    String dbName = String.format("cyclestreets_v%d.db", version);
    InputStream mInput = InstrumentationRegistry.getContext().getAssets().open(dbName);

    File db = new File(dbPath);
    if (db.exists()) {
      Log.i(TAG, "DB file " + dbPath + " already exists and is of size " + db.length() + " bytes");
    } else {
      assertThat(db.getParentFile().mkdirs()).isTrue();
      assertThat(db.createNewFile()).isTrue();
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
    Log.i(TAG, "DB file " + dbPath + " now has size " + db.length() + " bytes");
  }
}
