package net.cyclestreets

import android.os.Bundle

import net.cyclestreets.addphoto.AddPhotoFragment

class CycleStreets : MainNavDrawerActivity(), RouteMapActivity {
    public override fun onCreate(savedInstanceState: Bundle?) {
        MainSupport.switchMapFile(intent)

        super.onCreate(savedInstanceState)

        MainSupport.loadRoute(intent, this)
    }

    override fun onFirstRun() {
        Welcome.welcome(this)
    }

    override fun onNewVersion() {
        Welcome.whatsNew(this)
    }

    override fun showMap() {
        showPage(0)
    }

    override fun addDrawerItems() {
        addDrawerFragment(R.string.route_map,
                          R.drawable.ic_menu_mapmode,
                          RouteMapFragment::class.java)
        addDrawerFragment(R.string.itinerary,
                          R.drawable.ic_menu_agenda,
                          ItineraryAndElevationFragment::class.java,
                          RouteAvailablePageStatus())
        addDrawerFragment(R.string.photomap,
                          R.drawable.ic_menu_gallery,
                          PhotoMapFragment::class.java)
        addDrawerFragment(R.string.photo_upload,
                          R.drawable.ic_menu_camera,
                          AddPhotoFragment::class.java)
        addDrawerFragment(BlogFragment.blogTitle(this),
                          -1,
                          BlogFragment::class.java)
        addDrawerFragment(R.string.settings,
                          android.R.drawable.ic_menu_preferences,
                          SettingsFragment::class.java)
    }
}
