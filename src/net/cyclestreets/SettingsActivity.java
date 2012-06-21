package net.cyclestreets;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.api.POICategories;
import net.cyclestreets.util.MapPack;
import net.cyclestreets.util.MessageBox;
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

    setupMapFileList();

    setSummary(CycleStreetsPreferences.PREF_ROUTE_TYPE_KEY);
    setSummary(CycleStreetsPreferences.PREF_UNITS_KEY);
    setSummary(CycleStreetsPreferences.PREF_SPEED_KEY);
    setSummary(CycleStreetsPreferences.PREF_MAPSTYLE_KEY);
    setSummary(CycleStreetsPreferences.PREF_MAPFILE_KEY);
    setSummary(CycleStreetsPreferences.PREF_ICON_SIZE);
    setSummary(CycleStreetsPreferences.PREF_UPLOAD_SIZE);
  } // onCreate
  
  private void setupMapFileList()
  {
    final ListPreference mapfilePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
    populateMapFileList(mapfilePref);
  } // setupMapFileList
  
  private void populateMapFileList(final ListPreference mapfilePref)
  {
    final List<String> names = new ArrayList<String>();
    final List<String> files = new ArrayList<String>();

    for(final MapPack pack : MapPack.availableMapPacks())
    {
      names.add(pack.name);
      files.add(pack.path);      
    } // for
    
    mapfilePref.setEntries(names.toArray(new String[] { }));
    mapfilePref.setEntryValues(files.toArray(new String[] { }));
  } // populateMapFileList
  

  @Override
  protected void onResume() 
  {
    super.onResume();

    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
    final PreferenceScreen account = (PreferenceScreen)findPreference(CycleStreetsPreferences.PREF_ACCOUNT_KEY);
    if(CycleStreetsPreferences.accountOK())
      account.setSummary(R.string.settings_signed_in);
    else if(CycleStreetsPreferences.accountPending())
      account.setSummary(R.string.settings_awaiting);
    else
      account.setSummary("");
  } // onResume

  @Override
  protected void onPause() 
  {
    super.onPause();

    POICategories.reload();
    
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
      prefUI.setSummary(((EditTextPreference)prefUI).getText());
    
    if(CycleStreetsPreferences.PREF_MAPSTYLE_KEY.equals(key))
    {
      final String style = ((ListPreference)prefUI).getValue();
      final ListPreference mapfilePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
      final boolean enabled = style.equals(CycleStreetsPreferences.MAPSTYLE_MAPSFORGE);
      mapfilePref.setEnabled(enabled);
      if(enabled)
      {
        final String mapfile = CycleStreetsPreferences.mapfile();
        int index = mapfilePref.findIndexOfValue(mapfile);
        if(index == -1)
        {
          if(mapfilePref.getEntryValues() == null ||
             mapfilePref.getEntryValues().length == 0) 
          {
            mapfilePref.setEnabled(false);
            MessageBox.OK(getListView(), R.string.no_map_packs);
            return;
          } // if ...
          
          mapfilePref.setValueIndex(0);
          index = 0;
        } 
        mapfilePref.setSummary(mapfilePref.getEntries()[index]);
      }
    } // if ...
  } // setSummary
} // class SettingsActivity
