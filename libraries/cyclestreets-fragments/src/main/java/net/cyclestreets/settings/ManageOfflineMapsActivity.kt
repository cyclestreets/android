package net.cyclestreets.settings

import android.support.v4.app.Fragment

import net.cyclestreets.FragmentHolder

class ManageOfflineMapsActivity : FragmentHolder() {
    override fun fragment(): Fragment {
        return ManageOfflineMapsFragment()
    }
}
