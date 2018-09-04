package net.cyclestreets

import android.os.Bundle

class CycleStreets : MainNavDrawerActivity(), RouteMapActivity, PhotoMapActivity {
    public override fun onCreate(savedInstanceState: Bundle?) {
        MainSupport.switchMapFile(intent)

        super.onCreate(savedInstanceState)

        MainSupport.handleLaunchIntent(intent, this)
    }

    override fun onFirstRun() {
        Welcome.welcome(this)
    }

    override fun onNewVersion() {
        Welcome.whatsNew(this)
    }

    override fun showRouteMap() {
        showPage(R.id.nav_journey_planner)
    }

    override fun showPhotoMap() {
        showPage(R.id.nav_photomap)
    }
}
