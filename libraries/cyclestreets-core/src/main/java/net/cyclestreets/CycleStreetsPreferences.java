package net.cyclestreets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import androidx.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class CycleStreetsPreferences
{
  private static Context context_;

  public final static String NOT_SET = "not-set";

  public final static String PREF_ROUTE_TYPE_KEY = "routetype";
  public final static String PREF_UNITS_KEY = "units";
  public final static String PREF_SPEED_KEY = "speed";
  public final static String PREF_MAPSTYLE_KEY = "mapstyle";
  public final static String PREF_MAPFILE_KEY = "mapfile";
  public final static String PREF_CONFIRM_NEW_ROUTE = "confirm-new-route";
  public final static String PREF_USERNAME_KEY = "username";
  public final static String PREF_PASSWORD_KEY = "password";
  public final static String PREF_EMAIL_KEY = "email";
  public final static String PREF_NAME_KEY = "name";
  public final static String PREF_VALIDATED_KEY = "signed-in";
  public final static String PREF_PENDING_KEY = "pending";
  public final static String PREF_ACCOUNT_KEY = "cyclestreets-account";
  public final static String PREF_UPLOAD_SIZE = "uploadsize";
  public final static String PREF_BLOG_NOTIFICATIONS = "blog-notifications";
  public final static String PREF_TURN_NOW = "turn-now-distance";
  public final static String PREF_NEARING_TURN = "nearing-turn-distance";
  public final static String PREF_OFFTRACK_DISTANCE = "offtrack-distance";
  public final static String PREF_REPLAN_DISTANCE = "replan-distance";

  public final static String PREF_SHOW_REMAINING_TIME = "show-remaining-time";
  public final static String PREF_SHOW_ETA = "show-ETA";

  public static final String PREF_PERMISSION_REQUESTED_PREFIX = "permission-requested-";

  public final static String MAPSTYLE_OCM = "CycleStreets";
  public final static String MAPSTYLE_OSM = "CycleStreets-OSM";
  public final static String MAPSTYLE_OS = "CycleStreets-OS";
  public final static String MAPSTYLE_MAPSFORGE = "CycleStreets-Mapsforge";

  public static void initialise(final Context context, final int defaults) {
    context_ = context;

    if (defaults != -1)
      PreferenceManager.setDefaultValues(context_, defaults, false);

    // upgrades
    if (uploadSize().equals("320px"))
      putString(PREF_UPLOAD_SIZE, "640px");
  }

  public static String routeType() {
    return getString(PREF_ROUTE_TYPE_KEY, RoutePlans.PLAN_BALANCED);
  }

  public static String units() {
    return getString(PREF_UNITS_KEY, "km");
  }

  public static int speed() {
    return Integer.parseInt(getString(PREF_SPEED_KEY, "20"));
  }

  public static String mapstyle() {
    return getString(PREF_MAPSTYLE_KEY, NOT_SET);
  }

  public static void setMapstyle(final String name) {
    putString(PREF_MAPSTYLE_KEY, name);
  }

  public static void resetMapstyle() {
    setMapstyle(NOT_SET);
  }

  public static String mapfile() {
    return getString(PREF_MAPFILE_KEY, NOT_SET);
  }

  public static String username() {
    return getString(PREF_USERNAME_KEY, "");
  }

  public static String password() {
    return getString(PREF_PASSWORD_KEY, "");
  }

  public static String name() {
    return getString(PREF_NAME_KEY, "");
  }

  public static String email() {
    return getString(PREF_EMAIL_KEY, "");
  }

  public static boolean accountOK() {
    return getBoolean(PREF_VALIDATED_KEY, false);
  }

  public static boolean accountPending() {
    return getBoolean(PREF_PENDING_KEY, false);
  }

  public static boolean confirmNewRoute() {
    return getBoolean(PREF_CONFIRM_NEW_ROUTE, true);
  }

  public static String uploadSize() {
    return getString(PREF_UPLOAD_SIZE, "bigIfWifi");
  }

  public static boolean blogNotifications() {
    return getBoolean(PREF_BLOG_NOTIFICATIONS, true);
  }

  public static void setBlogNotifications(final boolean active) {
    putBoolean(PREF_BLOG_NOTIFICATIONS, active);
  }

  public static int turnNowDistance() {
    return Integer.parseInt(getString(PREF_TURN_NOW, "15"));
  }

  public static int nearingTurnDistance() {
    return Integer.parseInt(getString(PREF_NEARING_TURN, "50"));
  }

  public static int offtrackDistance() {
    return Integer.parseInt(getString(PREF_OFFTRACK_DISTANCE, "30"));
  }

  public static int replanDistance() {
    return Integer.parseInt(getString(PREF_REPLAN_DISTANCE, "50"));
  }

  public static boolean showRemainingTime() {
    return getBoolean(PREF_SHOW_REMAINING_TIME, true);
  }

  public static boolean showEta() {
    return getBoolean(PREF_SHOW_ETA, true);
  }

  public static boolean permissionPreviouslyRequested(String permission) {
    return getBoolean(PREF_PERMISSION_REQUESTED_PREFIX + permission, false);
  }

  public static void logPermissionAsRequested(String permission) {
    putBoolean(PREF_PERMISSION_REQUESTED_PREFIX + permission, true);
  }

  public static boolean uploadSmallImages() {
    if ("640px".equals(uploadSize()))
      return true;
    if ("big".equals(uploadSize()))
      return false;

    return !onFastConnection();
  }

  private static boolean onFastConnection() {
    final ConnectivityManager connMgr = (ConnectivityManager)context_.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      return onFastConnection28OrHigher(connMgr);
    } else {
      return onFastConnectionPre28(connMgr);
    }
  }

  private static boolean onFastConnection28OrHigher(ConnectivityManager connMgr) {
    NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
           capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
           onQuickMobileConnection(connMgr);
  }

  @SuppressWarnings("deprecation")
  private static boolean onFastConnectionPre28(ConnectivityManager connMgr) {
    final int type = connMgr.getActiveNetworkInfo().getType();
    return type == ConnectivityManager.TYPE_WIFI ||
           type == ConnectivityManager.TYPE_WIMAX ||
           onQuickMobileConnection(connMgr);
  }

  private static boolean onQuickMobileConnection(ConnectivityManager connMgr) {
    // so it's mobile, but is it still quick?
    final int subtype = connMgr.getActiveNetworkInfo().getSubtype();
    return ((subtype == TelephonyManager.NETWORK_TYPE_HSDPA) ||
            (subtype == TelephonyManager.NETWORK_TYPE_HSPA) ||
            (subtype == TelephonyManager.NETWORK_TYPE_HSUPA));
  }

  public static void clearOsmdroidCacheLocation() {
    final Editor editor = editor();
    editor.remove("osmdroid.basePath");
    editor.remove("osmdroid.cachePath");
    editor.commit();
  }

  private static String getString(final String key, final String defVal) {
    if (context_ == null) {
      // Protect against a potential race condition on resume
      return defVal;
    }
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context_);
    return prefs.getString(key, defVal);
  }

  private static void putString(final String key, final String value) {
    final Editor editor = editor();
    editor.putString(key, value);
    editor.commit();
  }

  private static boolean getBoolean(final String key, final boolean defVal) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context_);
    return prefs.getBoolean(key, defVal);
  }

  private static void putBoolean(final String key, final boolean value) {
    final Editor editor = editor();
    editor.putBoolean(key, value);
    editor.commit();
  }

  public static void setUsernamePassword(final String username,
                                         final String password,
                                         final String name,
                                         final String email,
                                         final boolean signedin) {
    final Editor editor = editor();
    editor.putString(PREF_USERNAME_KEY, username);
    editor.putString(PREF_PASSWORD_KEY, password);
    editor.putBoolean(PREF_VALIDATED_KEY, signedin);
    if (signedin) {
      editor.putString(PREF_NAME_KEY, name);
      editor.putString(PREF_EMAIL_KEY, email);
      editor.putBoolean(PREF_PENDING_KEY, false);
    }
    editor.commit();
  }

  public static void setPendingUsernamePassword(final String username,
                                                final String password,
                                                final String name,
                                                final String email,
                                                final boolean pending) {
    final Editor editor = editor();
    editor.putString(PREF_USERNAME_KEY, username);
    editor.putString(PREF_PASSWORD_KEY, password);
    editor.putString(PREF_NAME_KEY, name);
    editor.putString(PREF_EMAIL_KEY, email);
    editor.putBoolean(PREF_PENDING_KEY, pending);
    editor.putBoolean(PREF_VALIDATED_KEY, false);
    editor.commit();
  }

  public static void clearUsernamePassword() {
    final Editor editor = editor();
    editor.putString(PREF_USERNAME_KEY, "");
    editor.putString(PREF_PASSWORD_KEY, "");
    editor.putString(PREF_NAME_KEY, "");
    editor.putString(PREF_EMAIL_KEY, "");
    editor.putBoolean(PREF_PENDING_KEY, false);
    editor.putBoolean(PREF_VALIDATED_KEY, false);
    editor.commit();
  }

  public static void enableMapFile(final String filename) {
    final Editor editor = editor();
    editor.putString(PREF_MAPSTYLE_KEY, MAPSTYLE_MAPSFORGE);
    editor.putString(PREF_MAPFILE_KEY, filename);
    editor.commit();
  }

  private static Editor editor() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context_);
    return prefs.edit();
  }
}
