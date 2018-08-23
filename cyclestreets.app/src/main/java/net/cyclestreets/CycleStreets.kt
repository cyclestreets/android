package net.cyclestreets

import android.os.Bundle

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
        showPage(R.id.nav_journey_planner)
    }
}
