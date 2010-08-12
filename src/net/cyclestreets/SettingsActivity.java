package net.cyclestreets;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	@Override 
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 

		// inflate preferences screen
		addPreferencesFromResource(R.xml.preferences); 
	}

    @Override
    protected void onResume() {
        super.onResume();

		// register self as preferences change listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop listening while paused
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }

	// listen for preference changes
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		// show new choice in summary field
		Preference prefUI = findPreference(key);
	    if (prefUI instanceof ListPreference) {
			prefUI.setSummary(((ListPreference) prefUI).getEntry());
	    }
	}
}
