package net.cyclestreets.itinerary

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import net.cyclestreets.fragments.R

import net.cyclestreets.util.MenuHelper.showMenuItem

class ItineraryAndElevationFragment : Fragment() {
    private var lastFrag: Fragment? = null
    private lateinit var itinerary: Fragment
    private lateinit var elevation: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        retainInstance = true
        itinerary = ItineraryFragment()
        elevation = ElevationProfileFragment()

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.itinerary_and_elevation, container, false)
    }

    override fun onResume() {
        super.onResume()
        showFrag(lastFrag ?: itinerary)
    }

    private fun showFrag(frag: Fragment) {
        val fm = childFragmentManager
        val ft = fm.beginTransaction()

        if (lastFrag != null)
            ft.detach(lastFrag)

        if (fm.findFragmentByTag(frag.tag) == null)
            ft.add(R.id.container, frag, frag.javaClass.simpleName)
        else
            ft.attach(frag)
        ft.commit()

        lastFrag = frag

        activity!!.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.itinerary_and_elevation_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        showMenuItem(menu, R.id.ic_menu_itinerary, itinerary !== lastFrag)
        showMenuItem(menu, R.id.ic_menu_elevation, elevation !== lastFrag)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (super.onOptionsItemSelected(item))
            return true

        val menuId = item!!.itemId

        if (R.id.ic_menu_itinerary == menuId)
            showFrag(itinerary)

        if (R.id.ic_menu_elevation == menuId)
            showFrag(elevation)

        return true
    }
}
