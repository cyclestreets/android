package net.cyclestreets

import android.content.SharedPreferences
import android.os.Bundle
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.preference.*

import android.util.Log
import android.view.Gravity
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import net.cyclestreets.fragments.R
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.tiles.TileSource
import net.cyclestreets.util.Logging
import net.cyclestreets.util.MapPack
import net.cyclestreets.util.MessageBox


private val TAG = Logging.getTag(SettingsFragment::class.java)
private const val PREFERENCE_SCREEN_ARG: String = "preferenceScreenArg"
private val SETTINGS_ICONS = mapOf(
    "screen-maps-display" to GoogleMaterial.Icon.gmd_map,
    "mapstyle" to null,
    "confirm-new-route" to null,
    "screen-routing-preferences" to GoogleMaterial.Icon.gmd_directions,
    "routetype" to null,
    "speed" to null,
    "units" to null,
    "screen-liveride" to GoogleMaterial.Icon.gmd_navigation,
    "nearing-turn-distance" to null,
    "offtrack-distance" to null,
    "replan-distance" to null,
    "screen-locations" to GoogleMaterial.Icon.gmd_edit_location,
    "screen-account" to GoogleMaterial.Icon.gmd_account_circle,
    "cyclestreets-account" to null,
    "username" to null,
    "password" to null,
    "uploadsize" to null,
    "screen-about" to GoogleMaterial.Icon.gmd_info_outline
)


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, Undoable {

    private var undoable = false

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setupMapStyles()

        setSummary(CycleStreetsPreferences.PREF_ROUTE_TYPE_KEY)
        setSummary(CycleStreetsPreferences.PREF_UNITS_KEY)
        setSummary(CycleStreetsPreferences.PREF_SPEED_KEY)
        setSummary(CycleStreetsPreferences.PREF_MAPSTYLE_KEY)
        setSummary(CycleStreetsPreferences.PREF_UPLOAD_SIZE)
        setSummary(CycleStreetsPreferences.PREF_NEARING_TURN)
        setSummary(CycleStreetsPreferences.PREF_OFFTRACK_DISTANCE)
        setSummary(CycleStreetsPreferences.PREF_REPLAN_DISTANCE)
    }

    override fun onCreatePreferences(savedInstance: Bundle?, rootKey: String?) {
        if (arguments != null) {
            val key = requireArguments().getString(PREFERENCE_SCREEN_ARG)
            Log.d(TAG, "Creating preferences subscreen with key $key")
            setPreferencesFromResource(R.xml.prefs, key)
            undoable = true
            this.enterTransition = Slide(Gravity.END)
            this.exitTransition = Slide(Gravity.END)
        } else {
            Log.d(TAG, "Creating root preferences page")
            setPreferencesFromResource(R.xml.prefs, rootKey)
            this.enterTransition = Fade()
            this.exitTransition = Fade()
        }
        populateSettingsIcons();
    }

    private fun populateSettingsIcons() {
        for (prefIndex in 0 until preferenceScreen.preferenceCount) {
            val pref = preferenceScreen.getPreference(prefIndex)
            SETTINGS_ICONS[pref.key]?.let {
                pref.icon = materialIcon(requireContext(), it)
            }
        }
    }

    override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
        showScreen(preferenceScreen.key)
    }

    override fun onBackPressed(): Boolean {
        if (undoable)
            showScreen()
        return undoable
    }

    private fun showScreen(key: String? = null) {
        val screen = SettingsFragment()

        if (key != null) {
            val args = Bundle()
            args.putString(PREFERENCE_SCREEN_ARG, key)
            screen.arguments = args
        }

        parentFragmentManager
                .beginTransaction()
                .replace(id, screen)
                .commit()

    }

    private fun setupMapStyles() {
        findPreference<ListPreference>(CycleStreetsPreferences.PREF_MAPSTYLE_KEY)?.apply {
           TileSource.configurePreference(this)
        }
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
        val prefUI = findPreference<Preference>(key) ?: return
        if (prefUI is ListPreference)
            prefUI.summary = prefUI.entry
        if (prefUI is EditTextPreference)
            prefUI.summary = prefUI.text
   }

   private fun setAccountSummary() {
        val pref = findPreference<Preference>(CycleStreetsPreferences.PREF_ACCOUNT_KEY) ?: return
        val account = pref as PreferenceScreen

        when {
            CycleStreetsPreferences.accountOK() -> account.setSummary(R.string.settings_signed_in)
            CycleStreetsPreferences.accountPending() -> account.setSummary(R.string.settings_awaiting)
            else -> account.summary = ""
        }
    }
}
