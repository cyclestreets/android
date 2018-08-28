package net.cyclestreets

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.SparseArray
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.MenuItem

import android.view.View
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon
import com.mikepenz.iconics.context.IconicsContextWrapper

import net.cyclestreets.fragments.R
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Waypoints
import net.cyclestreets.util.Logging

import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
import android.support.transition.Fade
import net.cyclestreets.addphoto.AddPhotoFragment
import net.cyclestreets.iconics.IconicsHelper.materialIcons
import net.cyclestreets.itinerary.ItineraryAndElevationFragment

private val TAG = Logging.getTag(MainNavDrawerActivity::class.java)
private const val DRAWER_ITEMID_SELECTED_KEY = "DRAWER_ITEM_SELECTED"

abstract class MainNavDrawerActivity : AppCompatActivity(), OnNavigationItemSelectedListener, Route.Listener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private var selectedItem: Int = 0
    private var currentFragment: Fragment? = null

    private val itemToFragment = object : SparseArray<Class<out Fragment>>() {
        init {
            put(R.id.nav_journey_planner, RouteMapFragment::class.java)
            put(R.id.nav_itinerary, ItineraryAndElevationFragment::class.java)
            put(R.id.nav_photomap, PhotoMapFragment::class.java)
            put(R.id.nav_addphoto, AddPhotoFragment::class.java)
            put(R.id.nav_blog, BlogFragment::class.java)
            put(R.id.nav_settings, SettingsFragment::class.java)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Allows the use of Material icon library, see https://github.com/mikepenz/Android-Iconics
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_navdrawer_activity)

        val (burgerIcon, addPhotoIcon, blogIcon, settingsIcon) =
                materialIcons(context = this, size = 24,
                              icons = listOf(Icon.gmd_menu, Icon.gmd_add_a_photo, Icon.gmd_chat, Icon.gmd_settings))
        burgerIcon.setTint(resources.getColor(R.color.cs_primary_material_light, null))

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = (findViewById<NavigationView>(R.id.nav_view)).apply {
            setNavigationItemSelectedListener(this@MainNavDrawerActivity)
            menu.findItem(R.id.nav_itinerary).isVisible = Route.available()
            menu.findItem(R.id.nav_addphoto).icon = addPhotoIcon
            menu.findItem(R.id.nav_blog).icon = blogIcon
            menu.findItem(R.id.nav_settings).icon = settingsIcon
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(burgerIcon)
        }

        if (CycleStreetsAppSupport.isFirstRun())
            onFirstRun()
        else if (CycleStreetsAppSupport.isNewVersion())
            onNewVersion()
        CycleStreetsAppSupport.splashScreenSeen()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setBlogStateTitle()
                drawerLayout.openDrawer(Gravity.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //////////// OnNavigationItemSelectedListener method implementation
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        // set item as selected to persist highlight
        menuItem.isChecked = true
        // close drawer when item is tapped
        drawerLayout.closeDrawers()

        // Save which item is selected
        selectedItem = menuItem.itemId

        // Update the ActionBar title to be the title of the chosen fragment
        toolbar.title = menuItem.title

        // Swap UI fragments based on the selection
        val ft = this.supportFragmentManager.beginTransaction()
        ft.replace(R.id.content_frame, instantiateFragmentFor(menuItem))
        ft.commit()
        return true
    }

    private fun instantiateFragmentFor(menuItem: MenuItem): Fragment {
        val fragmentClass = itemToFragment.get(menuItem.itemId)
        try {
            return fragmentClass.newInstance().apply {
                enterTransition = Fade()
                exitTransition = Fade()
                currentFragment = this
            }
        }
        catch (e: InstantiationException) { throw RuntimeException(e) }
        catch (e: IllegalAccessException) { throw RuntimeException(e) }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers()
            return
        }
        currentFragment?.let {
            if (it is Undoable && it.onBackPressed())
                return
        }

        super.onBackPressed()
    }

    protected open fun onFirstRun() {}
    protected open fun onNewVersion() {}

    fun showPage(menuItemId: Int): Boolean {
        val menuItem = navigationView.menu.findItem(menuItemId)
        if (menuItem != null) {
            Log.d(TAG, "Loading page with menuItemId=${menuItemId} (${menuItem.title})")
            onNavigationItemSelected(menuItem)
            return true
        }
        Log.d(TAG, "Page with menuItemId=${menuItemId} could not be found")
        return false
    }

    public override fun onResume() {
        val selectedItem = prefs().getInt(DRAWER_ITEMID_SELECTED_KEY, R.id.nav_journey_planner)
        if (showPage(selectedItem)) {
            this.selectedItem = selectedItem
        }
        super.onResume()
        Route.registerListener(this)
        setBlogStateTitle()
    }

    public override fun onPause() {
        Route.unregisterListener(this)

        val edit = prefs().edit()
        edit.putInt(DRAWER_ITEMID_SELECTED_KEY, selectedItem)
        edit.apply()

        super.onPause()
    }

    private fun prefs(): SharedPreferences {
        return getSharedPreferences("net.cyclestreets.CycleStreets", Context.MODE_PRIVATE)
    }

    private fun setBlogStateTitle() {
        val titleId = if (BlogState.isBlogUpdateAvailable(this)) R.string.blog_updated else R.string.blog
        navigationView.menu.findItem(R.id.nav_blog).title = getString(titleId)
    }

    ////////// Route.Listener method implementations
    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        navigationView.menu.findItem(R.id.nav_itinerary).isVisible = Route.available()
        invalidateOptionsMenu()
    }

    override fun onResetJourney() {
        navigationView.menu.findItem(R.id.nav_itinerary).isVisible = Route.available()
        invalidateOptionsMenu()
    }

}
