package net.cyclestreets

import android.content.Intent
import android.os.Bundle

class CycleStreets : MainNavDrawerActivity(), RouteMapActivity, PhotoMapActivity {
    public override fun onCreate(savedInstanceState: Bundle?) {
        MainSupport.switchMapFile(intent, this)

        super.onCreate(savedInstanceState)

        MainSupport.handleLaunchIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            MainSupport.handleLaunchIntent(it, this)
        }
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
