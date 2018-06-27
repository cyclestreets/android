package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.routing.Route;
import android.content.Context;
import android.content.SharedPreferences;

public final class CycleStreetsAppSupport {
  private static boolean isFirstRun_;
  private static boolean isNew_;
  private static String version_;

  static public void initialise(final Context context) {
    initialise(context, -1);
  }

  public static void initialise(final Context context, final int prefsDefault) {
    CycleStreetsPreferences.initialise(context, prefsDefault);

    Route.initialise(context);
    ApiClient.initialise(context);

    version_ = version(context);

    isFirstRun_ = isFirstRun(context);
    isNew_ = isNew(context, version_);

    saveVersion(context, version_);
  }

  public static String version() { return version_; }
  public static boolean isNewVersion() { return isNew_; }
  public static boolean isFirstRun() { return isFirstRun_; }

  private  static String version(final Context context) {
    return "Version : " + AppInfo.version(context);
  }

  private static boolean isFirstRun(final Context context) {
    return UNKNOWN.equals(previousVersion(context));
  }
  private static boolean isNew(final Context context, final String version) {
    String prev = previousVersion(context);
    return !version.equals(prev);
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

  private CycleStreetsAppSupport() { }
}
