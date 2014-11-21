package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.routing.Route;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public final class CycleStreetsAppSupport {
  private static boolean isFirstRun_;
  private static boolean isNew_;
  private static String version_;

  public static void initialise(final Context context, final int prefsDefault) {
    CycleStreetsPreferences.initialise(context, prefsDefault);

    Route.initialise(context);
    ApiClient.initialise(context);

    version_ = version(context);

    isFirstRun_ = isFirstRun(context);
    isNew_ = isNew(context, version_);

    saveVersion(context, version_);
  } // onCreate

  public static String version() { return version_; }
  public static boolean isNewVersion() { return isNew_; }
  public static boolean isFirstRun() { return isFirstRun_; }

  private  static String version(final Context context) {
    return String.format("Version : %s/%s", context.getPackageName(), versionName(context));
  } // version

  private static String versionName(final Context context) {
    try {
      final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      return info.versionName;
    } // try
    catch(PackageManager.NameNotFoundException nnfe) {
      // like this is going to happen
      return UNKNOWN;
    } // catch
  } // versionName

  private static boolean isFirstRun(final Context context) {
    return UNKNOWN.equals(previousVersion(context));
  } // isFirstRun
  private static boolean isNew(final Context context, final String version) {
    return !version.equals(previousVersion(context));
  } // isNewVersion
  private static String previousVersion(final Context context) {
    return prefs(context).getString(VERSION_KEY, UNKNOWN);
  } // previousVersion

  private static void saveVersion(final Context context,
                                  final String version) {
    prefs(context).
        edit().
        putString(VERSION_KEY, version).
        commit();
  } // saveVersion

  private static SharedPreferences prefs(final Context context) {
    return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
  } // prefs

  private static final String VERSION_KEY = "previous-version";
  private static final String UNKNOWN = "unknown";


  private CycleStreetsAppSupport() { }
} // CycleStreetsAppSupport
