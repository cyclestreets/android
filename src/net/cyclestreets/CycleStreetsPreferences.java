package net.cyclestreets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CycleStreetsPreferences {
	private static Context context_;
    public final static String PREF_ROUTE_TYPE_KEY = "routetype";
    public final static String PREF_UNITS_KEY = "units";

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
	
	static private String getString(final String key, final String defVal) {
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context_);
    	return prefs.getString(key, defVal);
	}
}
