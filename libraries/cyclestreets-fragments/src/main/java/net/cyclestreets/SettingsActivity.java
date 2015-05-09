package net.cyclestreets;

import net.cyclestreets.fragments.R;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import net.cyclestreets.api.POICategories;
import net.cyclestreets.tiles.TileSource;
import net.cyclestreets.util.MapPack;
import net.cyclestreets.util.MessageBox;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends PreferenceActivity
                implements SharedPreferences.OnSharedPreferenceChangeListener

{
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.prefs);

    setupMapStyles();
    setupMapFileList();

    setSummary(CycleStreetsPreferences.PREF_ROUTE_TYPE_KEY);
    setSummary(CycleStreetsPreferences.PREF_UNITS_KEY);
    setSummary(CycleStreetsPreferences.PREF_SPEED_KEY);
    setSummary(CycleStreetsPreferences.PREF_MAPSTYLE_KEY);
    setSummary(CycleStreetsPreferences.PREF_MAPFILE_KEY);
    setSummary(CycleStreetsPreferences.PREF_UPLOAD_SIZE);
    setSummary(CycleStreetsPreferences.PREF_NEARING_TURN);
    setSummary(CycleStreetsPreferences.PREF_OFFTRACK_DISTANCE);
    setSummary(CycleStreetsPreferences.PREF_REPLAN_DISTANCE);
  } // onCreate

  private void setupMapStyles() {
    final ListPreference mapstylePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPSTYLE_KEY);
    if (mapstylePref == null)
      return;
    TileSource.configurePreference(mapstylePref);
  } // setupMapStyles

  private void setupMapFileList() {
    final ListPreference mapfilePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
    if (mapfilePref == null)
      return;
    populateMapFileList(mapfilePref);
  } // setupMapFileList

  private void populateMapFileList(final ListPreference mapfilePref) {
    final List<String> names = new ArrayList<String>();
    final List<String> files = new ArrayList<String>();

    for(final MapPack pack : MapPack.availableMapPacks()) {
      names.add(pack.name());
      files.add(pack.path());
    } // for

    mapfilePref.setEntries(names.toArray(new String[] { }));
    mapfilePref.setEntryValues(files.toArray(new String[] { }));
  } // populateMapFileList

  @Override
  protected void onResume() {
    super.onResume();

    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    setAccountSummary();
  } // onResume

  @Override
  protected void onPause() {
    super.onPause();

    // stop listening while paused
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  } // onPause

  public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
    setSummary(key);
  } // onSharedPreferencesChanged

  private void setSummary(final String key) {
    final Preference prefUI = findPreference(key);
    if (prefUI instanceof ListPreference)
      prefUI.setSummary(((ListPreference)prefUI).getEntry());
    if (prefUI instanceof EditTextPreference)
      prefUI.setSummary(((EditTextPreference)prefUI).getText());

    if(CycleStreetsPreferences.PREF_MAPSTYLE_KEY.equals(key))
      setMapFileSummary(((ListPreference)prefUI).getValue());
  } // setSummary

  private void setMapFileSummary(final String style) {
    final ListPreference mapfilePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
    if (mapfilePref == null)
      return;

    final boolean enabled = style.equals(CycleStreetsPreferences.MAPSTYLE_MAPSFORGE);
    mapfilePref.setEnabled(enabled);

    if(!enabled)
      return;

    if(mapfilePref.getEntryValues().length == 0) {
      mapfilePref.setEnabled(false);
      MessageBox.YesNo(getListView(),
                       R.string.no_map_packs,
                       new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface arg0, int arg1) {
                           MapPack.searchGooglePlay(SettingsActivity.this);
                         } // onClick
                       });
      return;
    } // if ...

    final String mapfile = CycleStreetsPreferences.mapfile();
    int index = mapfilePref.findIndexOfValue(mapfile);
    if(index == -1)
      index = 0; // default to something

    mapfilePref.setValueIndex(index);
    mapfilePref.setSummary(mapfilePref.getEntries()[index]);
  } // setMapFileSummary

  private void setAccountSummary() {
    final PreferenceScreen account = (PreferenceScreen)findPreference(CycleStreetsPreferences.PREF_ACCOUNT_KEY);
    if (account == null)
      return;

    if(CycleStreetsPreferences.accountOK())
      account.setSummary(R.string.settings_signed_in);
    else if(CycleStreetsPreferences.accountPending())
      account.setSummary(R.string.settings_awaiting);
    else
      account.setSummary("");
  } // setAccountSummary
} // class SettingsActivity
