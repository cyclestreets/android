package net.cyclestreets

import android.content.SharedPreferences
import android.os.Bundle
import android.support.transition.Fade
import android.support.transition.Slide
import android.support.v7.preference.*

import android.util.Log
import android.view.Gravity
import net.cyclestreets.fragments.R
import net.cyclestreets.tiles.TileSource
import net.cyclestreets.util.Logging
import net.cyclestreets.util.MapPack
import net.cyclestreets.util.MessageBox

private val TAG = Logging.getTag(SettingsFragment::class.java)
private const val PREFERENCE_SCREEN_ARG: String = "preferenceScreenArg"

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setupMapStyles()
        setupMapFileList()

        setSummary(CycleStreetsPreferences.PREF_ROUTE_TYPE_KEY)
        setSummary(CycleStreetsPreferences.PREF_UNITS_KEY)
        setSummary(CycleStreetsPreferences.PREF_SPEED_KEY)
        setSummary(CycleStreetsPreferences.PREF_MAPSTYLE_KEY)
        setSummary(CycleStreetsPreferences.PREF_MAPFILE_KEY)
        setSummary(CycleStreetsPreferences.PREF_UPLOAD_SIZE)
        setSummary(CycleStreetsPreferences.PREF_NEARING_TURN)
        setSummary(CycleStreetsPreferences.PREF_OFFTRACK_DISTANCE)
        setSummary(CycleStreetsPreferences.PREF_REPLAN_DISTANCE)
    }

    override fun onCreatePreferences(savedInstance: Bundle?, rootKey: String?) {
        if (arguments != null) {
            Log.d(TAG, "Creating preferences subscreen with key $rootKey")
            val key = arguments!!.getString(PREFERENCE_SCREEN_ARG)
            setPreferencesFromResource(R.xml.prefs, key)
        } else {
            Log.d(TAG, "Creating root preferences page")
            setPreferencesFromResource(R.xml.prefs, rootKey)
        }
    }

    override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
        // Allow instantiation of subscreens
        val args = Bundle()
        args.putString(PREFERENCE_SCREEN_ARG, preferenceScreen.key)
        val settingsSubScreen = SettingsFragment()
        settingsSubScreen.arguments = args

        // Make the transitions into and back out of subscreens look nice
        settingsSubScreen.enterTransition = Slide(Gravity.END)
        settingsSubScreen.exitTransition = Fade()
        this.enterTransition = Fade()
        this.exitTransition = Fade()

        fragmentManager!!
            .beginTransaction()
            .replace(id, settingsSubScreen)
            .addToBackStack(null)
            .commit()
    }

    private fun setupMapStyles() {
        findPreference(CycleStreetsPreferences.PREF_MAPSTYLE_KEY)?.apply {
            val pref = this as ListPreference

            if (pref.value == CycleStreetsPreferences.MAPSTYLE_MAPSFORGE && MapPack.availableMapPacks().isEmpty()) {
                Log.i(TAG, "Offline Vector Maps were selected, but there are no available map packs; default to OSM")
                pref.value = CycleStreetsPreferences.MAPSTYLE_OSM
            }

            TileSource.configurePreference(pref)
        }
    }

    private fun setupMapFileList() {
        findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY)?.apply {
            populateMapFileList(this as ListPreference)
        }
    }

    private fun populateMapFileList(mapfilePref: ListPreference) {
        val names = MapPack.availableMapPacks().map { pack: MapPack -> pack.name() }
        val files = MapPack.availableMapPacks().map { pack: MapPack -> pack.path() }
        mapfilePref.entries = names.toTypedArray()
        mapfilePref.entryValues = files.toTypedArray()
    }

    override fun onResume() {
        super.onResume()

        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        setAccountSummary()
    }

    override fun onPause() {
        super.onPause()

        // stop listening while paused
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        setSummary(key)
    }

    private fun setSummary(key: String) {
        val prefUI = findPreference(key) ?: return
        if (prefUI is ListPreference)
            prefUI.summary = prefUI.entry
        if (prefUI is EditTextPreference)
            prefUI.summary = prefUI.text

        if (CycleStreetsPreferences.PREF_MAPSTYLE_KEY == key)
            setMapFileSummary((prefUI as ListPreference).value)
    }

    private fun setMapFileSummary(style: String) {
        val pref = findPreference(CycleStreetsPreferences.PREF_MAPFILE_KEY) ?: return
        val mapfilePref = pref as ListPreference

        val enabled = style == CycleStreetsPreferences.MAPSTYLE_MAPSFORGE
        mapfilePref.isEnabled = enabled

        if (!enabled)
            return

        if (mapfilePref.entryValues.isEmpty()) {
            mapfilePref.isEnabled = false
            MessageBox.YesNo(view!!, R.string.settings_no_map_packs) { _, _ -> MapPack.searchGooglePlay(context!!) }
            return
        }

        val mapfile = CycleStreetsPreferences.mapfile()
        var index = mapfilePref.findIndexOfValue(mapfile)
        if (index == -1)
            index = 0 // default to something

        mapfilePref.setValueIndex(index)
        mapfilePref.summary = mapfilePref.entries[index]
    }

    private fun setAccountSummary() {
        val pref = findPreference(CycleStreetsPreferences.PREF_ACCOUNT_KEY) ?: return
        val account = pref as PreferenceScreen

        when {
            CycleStreetsPreferences.accountOK() -> account.setSummary(R.string.settings_signed_in)
            CycleStreetsPreferences.accountPending() -> account.setSummary(R.string.settings_awaiting)
            else -> account.summary = ""
        }
    }
}
