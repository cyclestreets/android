package net.cyclestreets;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SettingsActivity extends PreferenceActivity 
							  implements SharedPreferences.OnSharedPreferenceChangeListener
							  
{
	@Override 
	public void onCreate(final Bundle savedInstanceState) 
	{ 
		super.onCreate(savedInstanceState); 

		addPreferencesFromResource(R.xml.preferences);
        setSummary(CycleStreetsPreferences.PREF_ROUTE_TYPE_KEY);
        setSummary(CycleStreetsPreferences.PREF_UNITS_KEY);
        setSummary(CycleStreetsPreferences.PREF_SPEED_KEY);
        setSummary(CycleStreetsPreferences.PREF_MAPSTYLE_KEY);
   	} // onCreate

    @Override
    protected void onResume() 
    {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
		final PreferenceScreen account = (PreferenceScreen)findPreference(CycleStreetsPreferences.PREF_ACCOUNT_KEY);
		if(CycleStreetsPreferences.accountOK())
			account.setSummary("Signed in to CycleStreets");
		else
			account.setSummary("");
    } // onResume

    @Override
    protected void onPause() 
    {
        super.onPause();

        // stop listening while paused
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    } // onPause
    
    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) 
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
	    	final String t = ((EditTextPreference)prefUI).getText();
	    	prefUI.setSummary(t);
	    }
    } // setSummary
} // class SettingsActivity
