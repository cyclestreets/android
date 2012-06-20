package net.cyclestreets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.cyclestreets.api.POICategories;
import net.cyclestreets.util.MessageBox;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
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
  
  private class CycleStreetsMapFilter implements FilenameFilter
  {
    public boolean accept(final File dir, final String name)
    {
      return name.contains("net.cyclestreets.maps");
    } // accept
  } // class CycleStreetsMapFilter
  
  private void setupMapFileList()
  {
    final ListPreference mapfilePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
    populateMapFileList(mapfilePref);
  } // setupMapFileList
  
  private void populateMapFileList(final ListPreference mapfilePref)
  {
    final File obbDir = new File(Environment.getExternalStorageDirectory(), "Android/obb");
    if(!obbDir.exists())
      return;
    
    final List<String> names = new ArrayList<String>();
    final List<String> files = new ArrayList<String>();
    for(final File mapDir : obbDir.listFiles(new CycleStreetsMapFilter()))
    {
      final File map = findMapFile(mapDir, "main.");
      final String name = mapName(mapDir);
      if(map == null || name == null)
        continue;
      
      names.add(name);
      files.add(map.getAbsolutePath());      
    } // for
    
    mapfilePref.setEntries(names.toArray(new String[] { }));
    mapfilePref.setEntryValues(files.toArray(new String[] { }));
  } // populateMapFileList
  
  private File findMapFile(final File mapDir, final String prefix)
  {
    for(final File c : mapDir.listFiles())
      if(c.getName().startsWith(prefix))
        return c;
    return null;
  } // findMapFile
  
  private String mapName(final File mapDir)
  {
    try {
      final File detailsFile = findMapFile(mapDir, "patch.");
      final Properties details = new Properties();
      details.load(new FileInputStream(detailsFile));
      return details.getProperty("title");
    } // try
    catch(IOException e) {
      return null;
    }
  } // mapName

  @Override
  protected void onResume() 
  {
    super.onResume();

    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
    final PreferenceScreen account = (PreferenceScreen)findPreference(CycleStreetsPreferences.PREF_ACCOUNT_KEY);
    if(CycleStreetsPreferences.accountOK())
      account.setSummary("Signed in to CycleStreets");
    else if(CycleStreetsPreferences.accountPending())
      account.setSummary("Account registration awaiting verification");
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
