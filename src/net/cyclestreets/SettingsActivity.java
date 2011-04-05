package net.cyclestreets;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity 
							  implements SharedPreferences.OnSharedPreferenceChangeListener {
	@Override 
	public void onCreate(final Bundle savedInstanceState) 
	{ 
		super.onCreate(savedInstanceState); 

		addPreferencesFromResource(R.xml.preferences);
        setSummary(CycleStreetsPreferences.PREF_ROUTE_TYPE_KEY);
        setSummary(CycleStreetsPreferences.PREF_UNITS_KEY);
        setSummary(CycleStreetsPreferences.PREF_SPEED_KEY);
        setSummary(CycleStreetsPreferences.PREF_MAPSTYLE_KEY);
        setSummary(CycleStreetsPreferences.PREF_USERNAME_KEY);
        setSummary(CycleStreetsPreferences.PREF_PASSWORD_KEY);
	} // onCreate

    @Override
    protected void onResume() 
    {
        super.onResume();

		// register self as preferences change listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    } // onResume

    @Override
    protected void onPause() 
    {
        super.onPause();

        // stop listening while paused
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    } // onPause

	// listen for preference changes
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) 
	{
		setSummary(key);
	} // onSharedPreferencesChanged

	private void setSummary(final String key) 
	{
		final Preference prefUI = findPreference(key);
	    if (prefUI instanceof ListPreference) 
			prefUI.setSummary(((ListPreference)prefUI).getEntry());
	    if (prefUI instanceof EditTextPreference)
	    {
	    	String t = ((EditTextPreference)prefUI).getText();
	    	if((key.equals(CycleStreetsPreferences.PREF_PASSWORD_KEY)) && (t.length() != 0))
	    		t = "********";
	    	prefUI.setSummary(t);
	    }
    } // setSummary
} // class SettingsActivity
