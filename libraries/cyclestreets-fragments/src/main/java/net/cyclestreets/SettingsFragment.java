package net.cyclestreets;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.transition.Fade;
import android.support.transition.Slide;
import android.support.v7.preference.*;

import android.util.Log;
import android.view.Gravity;
import net.cyclestreets.fragments.R;
import net.cyclestreets.tiles.TileSource;
import net.cyclestreets.util.Logging;
import net.cyclestreets.util.MapPack;
import net.cyclestreets.util.MessageBox;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
  private static final String TAG = Logging.getTag(SettingsFragment.class);
  private static final String PREFERENCE_SCREEN_ARG = "preferenceScreenArg";

  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);

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

  @Override
  public void onCreatePreferences(final Bundle savedInstance, final String rootKey) {
    if (getArguments() != null) {
      Log.d(TAG, "Creating preferences subscreen with key " + rootKey);
      String key = getArguments().getString(PREFERENCE_SCREEN_ARG);
      setPreferencesFromResource(R.xml.prefs, key);
    } else {
      Log.d(TAG, "Creating root preferences page");
      setPreferencesFromResource(R.xml.prefs, rootKey);
    }
  }

  @Override
  public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
    // Allow instantiation of subscreens
    Bundle args = new Bundle();
    args.putString(PREFERENCE_SCREEN_ARG, preferenceScreen.getKey());
    SettingsFragment settingsSubScreen = new SettingsFragment();
    settingsSubScreen.setArguments(args);

    // Make the transitions into and back out of subscreens look nice
    settingsSubScreen.setEnterTransition(new Slide(Gravity.END));
    settingsSubScreen.setExitTransition(new Slide(Gravity.START));
    this.setEnterTransition(new Fade());
    this.setExitTransition(new Fade());

    getFragmentManager()
        .beginTransaction()
        .replace(getId(), settingsSubScreen)
        .addToBackStack(null)
        .commit();
  }

  private void setupMapStyles() {
    final ListPreference mapstylePref = (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPSTYLE_KEY);
    if (mapstylePref == null)
      return;
    TileSource.configurePreference(mapstylePref);
  }

  private void setupMapFileList() {
    final ListPreference mapfilePref = (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
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
    if (prefUI == null)
      return;
    if (prefUI instanceof ListPreference)
      prefUI.setSummary(((ListPreference)prefUI).getEntry());
    if (prefUI instanceof EditTextPreference)
      prefUI.setSummary(((EditTextPreference)prefUI).getText());

    if (CycleStreetsPreferences.PREF_MAPSTYLE_KEY.equals(key))
      setMapFileSummary(((ListPreference)prefUI).getValue());
  }

  private void setMapFileSummary(final String style) {
    final ListPreference mapfilePref = (ListPreference)findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY);
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
