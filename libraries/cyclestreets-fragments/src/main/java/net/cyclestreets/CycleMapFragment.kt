package net.cyclestreets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import net.cyclestreets.fragments.R
import net.cyclestreets.util.AsyncDelete
import net.cyclestreets.util.Logging
import net.cyclestreets.util.MenuHelper.createMenuItem
import net.cyclestreets.util.MenuHelper.enableMenuItem
import net.cyclestreets.util.doOrRequestPermission
import net.cyclestreets.views.CycleMapView
import net.cyclestreets.views.CycleMapView.FINDPLACE_ZOOM_LEVEL

import org.osmdroid.config.Configuration
import org.osmdroid.config.DefaultConfigurationProvider
import org.osmdroid.views.overlay.Overlay

import java.io.File
import java.util.Date

private val TAG = Logging.getTag(CycleMapFragment::class.java)

open class CycleMapFragment : Fragment(), Undoable {
    private var map: CycleMapView? = null
    private var forceMenuRebuild: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View? {
        super.onCreate(saved)

        forceMenuRebuild = true

        checkPermissionNoMoreThanOnceEveryFiveMinutes()

        map = CycleMapView(context, this.javaClass.name)
        return map
    }

    private fun checkPermissionNoMoreThanOnceEveryFiveMinutes() {
        val now = Date().time
        val oneMinuteAgo = now - (5 * 60 * 1000)
        if (oneMinuteAgo > permissionLastCheckedTime) {
            permissionLastCheckedTime = now
            doOrRequestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                Log.v(TAG, "Already have ${Manifest.permission.WRITE_EXTERNAL_STORAGE} permission")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(TAG, "Permissions granted (with 0) or denied (with -1): ${permissions.joinToString()}, ${grantResults.joinToString()}")

        for (i in permissions.indices) {
            val permission = permissions[i]
            val grantResult = grantResults[i]

            // If we have permission to write to external storage, we'll use the default OSMDroid location for caching
            // map tiles.  Therefore, when permission is granted, clear state accordingly so this is possible.
            if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResult == PackageManager.PERMISSION_GRANTED) {
                val oldCacheLocation: File = Configuration.getInstance().osmdroidTileCache

                CycleStreetsPreferences.clearOsmdroidCacheLocation()
                Configuration.setConfigurationProvider(DefaultConfigurationProvider())
                Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
                val newCacheLocation: File = Configuration.getInstance().osmdroidTileCache

                Log.i(TAG, "Permission ${Manifest.permission.WRITE_EXTERNAL_STORAGE} granted; update OSMDroid cache " +
                           "location from ${oldCacheLocation.absolutePath} to ${newCacheLocation.absolutePath}")
                if (newCacheLocation.absolutePath != oldCacheLocation.absolutePath)
                    AsyncDelete().execute(oldCacheLocation)
            }
        }
    }

    protected fun mapView(): CycleMapView { return map!! }
    protected fun overlayPushBottom(overlay: Overlay): Overlay { return map!!.overlayPushBottom(overlay) }
    protected fun overlayPushTop(overlay: Overlay): Overlay { return map!!.overlayPushTop(overlay) }

    override fun onPause() {
        map!!.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        map!!.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (map != null)
            map!!.onCreateOptionsMenu(menu)

        createMenuItem(menu, R.string.menu_find_place, Menu.NONE, R.drawable.ic_menu_search)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (forceMenuRebuild) {
            forceMenuRebuild = false
            menu.clear()
            onCreateOptionsMenu(menu, requireActivity().menuInflater)
            onPrepareOptionsMenu(menu)
        }

        if (map != null)
            map!!.onPrepareOptionsMenu(menu)

        enableMenuItem(menu, R.string.menu_find_place, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (map!!.onMenuItemSelected(item.itemId, item))
            return true

        if (item.itemId == R.string.menu_find_place) {
            launchFindDialog()
            return true
        }

        return false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return map!!.onMenuItemSelected(item.itemId, item)
    }

    private fun launchFindDialog() {
        FindPlace.launch(requireContext(), map!!.boundingBox) { place ->
            map!!.centreOn(place, FINDPLACE_ZOOM_LEVEL)
        }
    }

    override fun onBackPressed(): Boolean {
        return map!!.onBackPressed()
    }

    companion object {
        var permissionLastCheckedTime: Long = 0
    }
}
