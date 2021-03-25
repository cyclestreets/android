package net.cyclestreets

import net.cyclestreets.fragments.R
import net.cyclestreets.iconics.IconicsHelper
import net.cyclestreets.util.*
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import net.cyclestreets.util.MenuHelper.enableMenuItem
import net.cyclestreets.util.MenuHelper.showMenuItem
import net.cyclestreets.views.overlay.*

private val TAG = Logging.getTag(RouteMapFragment::class.java)

class RouteMapFragment : CycleMapFragment(), Route.Listener {
    private lateinit var routeSetter: TapToRouteOverlay
    private var hasGps: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View? {
        val v = super.onCreateView(inflater, container, saved)

        overlayPushBottom(RouteHighlightOverlay(requireContext(), mapView()))
        overlayPushBottom(POIOverlay(mapView()))
        overlayPushBottom(CircularRoutePOIOverlay(mapView()))
        overlayPushBottom(RouteOverlay())

        routeSetter = TapToRouteOverlay(mapView(), this)
        overlayPushTop(routeSetter)

        hasGps = GPS.deviceHasGPS(requireContext())

        return v
    }

    override fun onPause() {
        Route.onPause(routeSetter.waypoints())
        Route.unregisterListener(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        Route.registerListener(this)
        Route.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        IconicsHelper.inflate(inflater, R.menu.route_map, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        showMenuItem(menu, R.id.ic_menu_liveride, Route.routeAvailable() && hasGps)
        enableMenuItem(menu, R.id.ic_menu_directions, true)
        showMenuItem(menu, R.id.ic_menu_saved_routes, Route.storedCount() != 0)
        enableMenuItem(menu, R.id.ic_menu_route_number, true)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (super.onOptionsItemSelected(item))
            return true

        when (item.itemId) {
            R.id.ic_menu_liveride -> {
                startLiveRide()
                return true
            }
            R.id.ic_menu_directions -> {
                launchRouteDialog()
                return true
            }
            R.id.ic_menu_saved_routes -> {
                launchStoredRoutes()
                return true
            }
            R.id.ic_menu_route_number -> {
                launchFetchRouteDialog()
                return true
            }
            else -> return false
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == CIRCULAR_ROUTE_ACTIVITY_REQUEST_CODE) && (resultCode == Activity.RESULT_OK)) {
            if (data != null) {
                val distance = data.getIntExtra(EXTRA_CIRCULAR_ROUTE_DISTANCE, 0)
                val duration = data.getIntExtra(EXTRA_CIRCULAR_ROUTE_DURATION, 0)
                Route.plotCircularRoute(RoutePlans.PLAN_LEISURE,
                                        if (distance != 0) distance else null,
                                        if (duration != 0) duration else null,
                                        data.getStringExtra(EXTRA_CIRCULAR_ROUTE_POI_CATEGORIES),
                                        requireContext())
            }
        }
    }

    private fun startLiveRide() {
        doOrRequestPermission(null, this, Manifest.permission.ACCESS_FINE_LOCATION, LIVERIDE_LOCATION_PERMISSION_REQUEST) {
            LiveRideActivity.launch(requireContext())
        }
    }

    private fun launchRouteDialog() {
        startNewRoute(DialogInterface.OnClickListener { _, _ -> doLaunchRouteDialog() })
    }

    private fun doLaunchRouteDialog() {
        RouteByAddress.launch(requireContext(),
                              mapView().boundingBox,
                              mapView().lastFix,
                              routeSetter.waypoints())
    }

    private fun launchFetchRouteDialog() {
        startNewRoute(DialogInterface.OnClickListener { _, _ -> doLaunchFetchRouteDialog() })
    }

    private fun doLaunchFetchRouteDialog() {
        RouteByNumber.launch(requireContext())
    }

    private fun launchStoredRoutes() {
        StoredRoutes.launch(requireContext())
    }

    private fun startNewRoute(listener: DialogInterface.OnClickListener) {
        if (Route.routeAvailable() && CycleStreetsPreferences.confirmNewRoute())
            MessageBox.YesNo(mapView(), R.string.confirm_new_route, listener)
        else
            listener.onClick(null, 0)
    }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        if (!waypoints.isEmpty()) {
            Log.d(TAG, "Setting map centre to " + waypoints.first()!!)
            mapView().controller.setCenter(waypoints.first())
        }
        mapView().postInvalidate()
    }

    override fun onResetJourney() {
        mapView().invalidate()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(TAG, "Permission ${permissions.joinToString()} was ${if (grantResults.joinToString().equals("0")) "granted" else "denied"}")

        for (i in permissions.indices) {
            val permission = permissions[i]
            val grantResult = grantResults[i]

            if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                if (requestCode == LIVERIDE_LOCATION_PERMISSION_REQUEST) {
                    requestPermissionsResultAction(grantResult, permission) {
                        LiveRideActivity.launch(requireContext())
                    }
                }
                else if (requestCode == FOLLOW_LOCATION_PERMISSION_REQUEST) {
                    // Sequence of events is: onPause / (Android) permissions box / onRequestPermissionsResult / mainNavDrawerActivity.onResume
                    // After enabling location, need to save values, as mainNavDrawerActivity.onResume will subsequently be called
                    // and Fragments/Overlays will be re-initialised, so these values will be lost otherwise
                    requestPermissionsResultAction(grantResult, permission) {
                        mapView().doEnableFollowLocation()
                        mapView().saveLocationPrefs()
                    }
                }
                return
            }
        }
    }
}
