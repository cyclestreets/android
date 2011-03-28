package net.cyclestreets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CycleStreetsPreferences {
	private static Context context_;
    public final static String PREF_ROUTE_TYPE_KEY = "routetype";
    public final static String PREF_UNITS_KEY = "units";
    public final static String PREF_SPEED_KEY = "speed";
    public final static String PREF_MAPSTYLE_KEY = "mapstyle";
    public final static String PREF_USERNAME_KEY = "username";
    public final static String PREF_PASSWORD_KEY = "password";

    static public void initialise(final Context context) {
    	context_ = context;
	    PreferenceManager.setDefaultValues(context_, R.xml.preferences, false);
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
		return getString(PREF_MAPSTYLE_KEY, "CycleMap");	
	}
	
	static public String username() { 
		return getString(PREF_USERNAME_KEY, "");
	} 
	
	static public String password() { 
		return getString(PREF_PASSWORD_KEY, "");
	} // password
	
	static private String getString(final String key, final String defVal) {
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context_);
    	return prefs.getString(key, defVal);
	}
}
