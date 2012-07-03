package net.cyclestreets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class CycleStreetsPreferences 
{
  private static Context context_;
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
  public final static String PREF_ICON_SIZE = "iconsize";
  
  public final static String MAPSTYLE_OCM = "CycleStreets";
  public final static String MAPSTYLE_OSM = "CycleStreets-OSM";
  public final static String MAPSTYLE_OS = "CycleStreets-OS";
  public final static String MAPSTYLE_MAPSFORGE = "CycleStreets-Mapsforge";

  static public void initialise(final Context context) {
    context_ = context;
    PreferenceManager.setDefaultValues(context_, R.xml.preferences, false);

    // upgrades
    if(uploadSize().equals("320px"))
    {
      final Editor editor = editor();
      editor.putString(PREF_UPLOAD_SIZE, "640px");
      editor.commit();
    } // if ...
  } // initialise
  
  static public String routeType() {
    return getString(PREF_ROUTE_TYPE_KEY, CycleStreetsConstants.PLAN_BALANCED);
  }

  static public String units() {
    return getString(PREF_UNITS_KEY, "km");
  } // units
  
  static public int speed() {
    return Integer.parseInt(getString(PREF_SPEED_KEY, "20"));
  }
  
  static public String mapstyle() {
    return getString(PREF_MAPSTYLE_KEY, MAPSTYLE_OCM);  
  }
  
  static public void resetMapstyle() {
	final Editor editor = editor();
	editor.putString(PREF_MAPSTYLE_KEY, MAPSTYLE_OCM);
    editor.commit();
  }
  
  static public String mapfile() {
    return getString(PREF_MAPFILE_KEY, "not-set");
  }
  
  static public int iconSize() { 
    return Integer.parseInt(getString(PREF_ICON_SIZE, "20"));
  }
  
  static public String username() { 
    return getString(PREF_USERNAME_KEY, "");
  } 
  
  static public String password() { 
    return getString(PREF_PASSWORD_KEY, "");
  } // password
  
  static public String name() { 
    return getString(PREF_NAME_KEY, "");
  } // name
  
  static public String email() { 
    return getString(PREF_EMAIL_KEY, "");
  } // email
  
  static public boolean accountOK() { 
    return getBoolean(PREF_VALIDATED_KEY, false);
  }
  
  static public boolean accountPending() { 
    return getBoolean(PREF_PENDING_KEY, false);
  }
  
  static public boolean confirmNewRoute() {
    return getBoolean(PREF_CONFIRM_NEW_ROUTE, true);
  }
  
  static public String uploadSize() {
    return getString(PREF_UPLOAD_SIZE, "bigIfWifi");
  } // uploadSize
  
  static public boolean uploadSmallImages() {
    final String resize = uploadSize();
    if("640px".equals(resize))
      return true;
    if("big".equals(resize))
      return false;   
    
    final ConnectivityManager connMgr = (ConnectivityManager)context_.getSystemService(Context.CONNECTIVITY_SERVICE);
    final NetworkInfo ni = connMgr.getActiveNetworkInfo();
    
    final int type = ni.getType();
    if((type == ConnectivityManager.TYPE_WIFI) || (type == ConnectivityManager.TYPE_WIMAX))
      return false;
    
    // so it's mobile, but is it still quick?
    final int subtype = ni.getSubtype();
    return !((subtype == TelephonyManager.NETWORK_TYPE_HSDPA) || 
             (subtype == TelephonyManager.NETWORK_TYPE_HSPA) ||
             (subtype == TelephonyManager.NETWORK_TYPE_HSUPA));
  } // uploadSmallImages
  
  static private String getString(final String key, final String defVal) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context_);
    return prefs.getString(key, defVal);
  } // getStirng
  
  static private boolean getBoolean(final String key, boolean defVal) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context_);
    return prefs.getBoolean(key, defVal);
  } // getBoolean    
  
  static public void setUsernamePassword(final String username, 
                                         final String password,
                                         final String name, 
                                         final String email,
                                         final boolean signedin) 
  {
    final Editor editor = editor();
    editor.putString(PREF_USERNAME_KEY, username);
    editor.putString(PREF_PASSWORD_KEY, password);
    editor.putBoolean(PREF_VALIDATED_KEY, signedin);
    if(signedin)
    {
      editor.putString(PREF_NAME_KEY, name);
      editor.putString(PREF_EMAIL_KEY, email);
      editor.putBoolean(PREF_PENDING_KEY, false);
    }
    editor.commit();
  } // setUsernamePassword
  
  static public void setPendingUsernamePassword(final String username, 
                                                final String password,
                                                final String name,
                                                final String email,
                                                final boolean pending) 
  {
    final Editor editor = editor();
    editor.putString(PREF_USERNAME_KEY, username);
    editor.putString(PREF_PASSWORD_KEY, password);
    editor.putString(PREF_NAME_KEY, name);
    editor.putString(PREF_EMAIL_KEY, email);
    editor.putBoolean(PREF_PENDING_KEY, pending);
    editor.putBoolean(PREF_VALIDATED_KEY, false);
    editor.commit();
  } // setPendingUsernamePassword
  
  static public void clearUsernamePassword()
  {
    final Editor editor = editor();
    editor.putString(PREF_USERNAME_KEY, "");
    editor.putString(PREF_PASSWORD_KEY, "");
    editor.putString(PREF_NAME_KEY, "");
    editor.putString(PREF_EMAIL_KEY, "");
    editor.putBoolean(PREF_PENDING_KEY, false);
    editor.putBoolean(PREF_VALIDATED_KEY, false);
    editor.commit();
  } // clearUsernamePassword
  
  static public void enableMapFile(final String filename)
  {
    final Editor editor = editor();
    editor.putString(PREF_MAPSTYLE_KEY, MAPSTYLE_MAPSFORGE);
    editor.putString(PREF_MAPFILE_KEY, filename);
    editor.commit();
  } // setMapFile
  
  static private Editor editor() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context_);
    return prefs.edit();
  } // editor
} // class CycleStreetsPreferences
