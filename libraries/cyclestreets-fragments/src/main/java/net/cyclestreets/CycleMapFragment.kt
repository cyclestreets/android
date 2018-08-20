package net.cyclestreets

import android.Manifest
import android.app.Activity
import net.cyclestreets.fragments.R

import net.cyclestreets.views.CycleMapView

import org.osmdroid.views.overlay.Overlay

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import net.cyclestreets.util.Logging

import net.cyclestreets.util.MenuHelper.createMenuItem
import net.cyclestreets.util.MenuHelper.enableMenuItem
import net.cyclestreets.util.doOrRequestPermission
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import net.cyclestreets.util.hasPermission
import org.osmdroid.config.Configuration
import java.io.File

private val TAG = Logging.getTag(CycleMapFragment::class.java)

open class CycleMapFragment : Fragment(), Undoable {
    private lateinit var map: CycleMapView
    private var forceMenuRebuild: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View? {
        super.onCreate(saved)

        forceMenuRebuild = true

        doOrRequestPermission(this.activity as Activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            Log.d(TAG, "Already have ${Manifest.permission.WRITE_EXTERNAL_STORAGE} permission")
        }

        map = CycleMapView(activity, this.javaClass.name)
        return map
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (i in 0 until permissions.size) {
            val permission = permissions[i]
            val grantResult = grantResults[i]

            // If we have permission to write to external storage, we'll use the default OSMDroid location for caching
            // map tiles.  Therefore, when permission is granted, clear state accordingly so this is possible.
            if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResult == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission ${Manifest.permission.WRITE_EXTERNAL_STORAGE} granted; update OSMDroid cache location")
                val oldCacheLocation: File = Configuration.getInstance().osmdroidTileCache

                CycleStreetsPreferences.clearOsmdroidCacheLocation();
                Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

                if (Configuration.getInstance().osmdroidTileCache.absolutePath != oldCacheLocation.absolutePath)
                    oldCacheLocation.delete()
            }
        }
    }

    protected fun mapView(): CycleMapView { return map }
    protected fun overlayPushBottom(overlay: Overlay): Overlay { return map.overlayPushBottom(overlay) }
    protected fun overlayPushTop(overlay: Overlay): Overlay { return map.overlayPushTop(overlay) }

    override fun onPause() {
        map.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (map != null)
            map.onCreateOptionsMenu(menu)

        createMenuItem(menu, R.string.menu_find_place, Menu.NONE, R.drawable.ic_menu_search)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (forceMenuRebuild) {
            forceMenuRebuild = false
            menu.clear()
            onCreateOptionsMenu(menu, activity!!.menuInflater)
            onPrepareOptionsMenu(menu)
        }

        if (map != null)
            map.onPrepareOptionsMenu(menu)

        enableMenuItem(menu, R.string.menu_find_place, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (map.onMenuItemSelected(item.itemId, item))
            return true

        if (item.itemId == R.string.menu_find_place) {
            launchFindDialog()
            return true
        }

        return false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return map.onMenuItemSelected(item.itemId, item)
    }

    private fun launchFindDialog() {
        FindPlace.launch(activity, map.boundingBox) {
            place -> map.centreOn(place)
        }
    }

    override fun onBackPressed(): Boolean {
        return map.onBackPressed()
    }
}
