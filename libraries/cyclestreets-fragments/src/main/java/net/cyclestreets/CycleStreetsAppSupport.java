package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.routing.Route;
import net.cyclestreets.util.Logging;
import net.cyclestreets.util.TurnIcons;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public final class CycleStreetsAppSupport {

  private static final String TAG = Logging.getTag(CycleStreetsAppSupport.class);

  private static boolean isFirstRun;
  private static boolean isNew;
  private static String version;
  private static Integer versionCode;
  private static String previousVersion;
  private static Integer previousVersionCode;

  public static void initialise(final Context context, final int prefsDefault) {
    TurnIcons.initialise(context);
    CycleStreetsPreferences.initialise(context, prefsDefault);
    CycleStreetsNotifications.INSTANCE.initialise(context);

    Route.initialise(context);
    ApiClient.INSTANCE.initialise(context);
    BlogState.INSTANCE.initialise(context);

    version = version(context);
    versionCode = code(version);
    previousVersion = previousVersion(context);
    previousVersionCode = code(previousVersion);

    isFirstRun = isFirstRun(context);
    isNew = !version.equals(previousVersion);

    saveVersion(context, version);

    migratePreferences(previousVersionCode, versionCode);
  }

  public static String version() { return version; }
  public static boolean isNewVersion() { return isNew; }
  public static boolean isFirstRun() { return isFirstRun; }
  public static void splashScreenSeen() {
    isFirstRun = false;
    isNew = false;
  }

  private static String version(final Context context) {
    return "Version : " + AppInfo.INSTANCE.version(context);
  }
  private static Integer code(String versionString) {
    if (UNKNOWN.equals(versionString)) {
      return 0;
    }
    String[] split = versionString.split("/");
    return Integer.valueOf(split[split.length - 1]);
  }

  private static boolean isFirstRun(final Context context) {
    return UNKNOWN.equals(previousVersion(context));
  }
  private static String previousVersion(final Context context) {
    return prefs(context).getString(VERSION_KEY, UNKNOWN);
  }

  private static void saveVersion(final Context context,
                                  final String version) {
    prefs(context).
        edit().
        putString(VERSION_KEY, version).
        commit();
  }

  private static SharedPreferences prefs(final Context context) {
    return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
  }

  private static final String VERSION_KEY = "previous-version";
  private static final String UNKNOWN = "unknown";

  private static void migratePreferences(Integer previousVersionCode, Integer versionCode) {
    Log.i(TAG, "Upgrading from " + previousVersion + " (" + previousVersionCode + ") to " + version + " (" + versionCode + ")");

    if (previousVersionCode < 1621 && versionCode >= 1621) {
      Log.i(TAG, "Clearing OSMDroid cache location after upgrade to target Android 10 (SDK 29) or higher changed accessible paths");
      CycleStreetsPreferences.clearOsmdroidCacheLocation();
    }
  }

  private CycleStreetsAppSupport() { }
}
