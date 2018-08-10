package net.cyclestreets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import net.cyclestreets.fragments.R

class AboutFragment : WebPageFragment("file:///android_asset/credits.html", R.layout.about) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val about = super.onCreateView(inflater, container, savedInstanceState)
        (about!!.findViewById<View>(R.id.version_view) as TextView).text = CycleStreetsAppSupport.version()

        return about
    }
}
