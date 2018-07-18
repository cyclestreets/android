package net.cyclestreets;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import net.cyclestreets.fragments.R;
import net.cyclestreets.tiles.TileSource;
import net.cyclestreets.util.MapPack;
import net.cyclestreets.util.MessageBox;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
  @Override
  public void onCreate(final Bundle savedInstance) {
    super.onCreate(savedInstance);

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
  }

  private void setupMapStyles() {
    final ListPreference mapstylePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPSTYLE_KEY);
    if (mapstylePref == null)
      return;
    TileSource.configurePreference(mapstylePref);
  }

  private void setupMapFileList() {
    final ListPreference mapfilePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
    if (mapfilePref == null)
      return;
    populateMapFileList(mapfilePref);
  }

  private void populateMapFileList(final ListPreference mapfilePref) {
    final List<String> names = new ArrayList<>();
    final List<String> files = new ArrayList<>();

    for (final MapPack pack : MapPack.availableMapPacks()) {
      names.add(pack.name());
      files.add(pack.path());
    }

    mapfilePref.setEntries(names.toArray(new String[] { }));
    mapfilePref.setEntryValues(files.toArray(new String[] { }));
  }

  @Override
  public void onResume() {
    super.onResume();

    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    setAccountSummary();
  }

  @Override
  public void onPause() {
    super.onPause();

    // stop listening while paused
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
    setSummary(key);
  }

  private void setSummary(final String key) {
    final Preference prefUI = findPreference(key);
    if (prefUI instanceof ListPreference)
      prefUI.setSummary(((ListPreference)prefUI).getEntry());
    if (prefUI instanceof EditTextPreference)
      prefUI.setSummary(((EditTextPreference)prefUI).getText());

    if (CycleStreetsPreferences.PREF_MAPSTYLE_KEY.equals(key))
      setMapFileSummary(((ListPreference)prefUI).getValue());
  }

  private void setMapFileSummary(final String style) {
    final ListPreference mapfilePref= (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
    if (mapfilePref == null)
      return;

    final boolean enabled = style.equals(CycleStreetsPreferences.MAPSTYLE_MAPSFORGE);
    mapfilePref.setEnabled(enabled);

    if (!enabled)
      return;

    if (mapfilePref.getEntryValues().length == 0) {
      mapfilePref.setEnabled(false);
      MessageBox.YesNo(getView(),
          R.string.settings_no_map_packs,
          (DialogInterface di, int i) -> MapPack.searchGooglePlay(getContext())
      );
      return;
    }

    final String mapfile = CycleStreetsPreferences.mapfile();
    int index = mapfilePref.findIndexOfValue(mapfile);
    if (index == -1)
      index = 0; // default to something

    mapfilePref.setValueIndex(index);
    mapfilePref.setSummary(mapfilePref.getEntries()[index]);
  }

  private void setAccountSummary() {
    final PreferenceScreen account = (PreferenceScreen)findPreference(CycleStreetsPreferences.PREF_ACCOUNT_KEY);
    if (account == null)
      return;

    if (CycleStreetsPreferences.accountOK())
      account.setSummary(R.string.settings_signed_in);
    else if (CycleStreetsPreferences.accountPending())
      account.setSummary(R.string.settings_awaiting);
    else
      account.setSummary("");
  }
}
