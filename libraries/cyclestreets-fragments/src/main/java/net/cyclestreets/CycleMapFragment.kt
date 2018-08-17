package net.cyclestreets

import net.cyclestreets.fragments.R

import net.cyclestreets.views.CycleMapView

import org.osmdroid.views.overlay.Overlay

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import net.cyclestreets.util.MenuHelper.createMenuItem
import net.cyclestreets.util.MenuHelper.enableMenuItem

open class CycleMapFragment : Fragment(), Undoable {
    private lateinit var map: CycleMapView
    private var forceMenuRebuild: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View? {
        super.onCreate(saved)

        forceMenuRebuild = true

        map = CycleMapView(activity, this.javaClass.name)
        return map
    }

    protected fun mapView(): CycleMapView? { return map }

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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (map != null)
            map.onCreateOptionsMenu(menu)

        createMenuItem(menu!!, R.string.menu_find_place, Menu.NONE, R.drawable.ic_menu_search)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        if (forceMenuRebuild) {
            forceMenuRebuild = false
            menu!!.clear()
            onCreateOptionsMenu(menu, activity!!.menuInflater)
            onPrepareOptionsMenu(menu)
        }

        if (map != null)
            map.onPrepareOptionsMenu(menu)

        enableMenuItem(menu!!, R.string.menu_find_place, true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (map.onMenuItemSelected(item!!.itemId, item))
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
