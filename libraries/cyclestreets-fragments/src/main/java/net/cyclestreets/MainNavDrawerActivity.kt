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
import android.support.v4.app.FragmentManager.OnBackStackChangedListener
import net.cyclestreets.addphoto.AddPhotoFragment
import net.cyclestreets.iconics.IconicsHelper.materialIcons
import net.cyclestreets.itinerary.ItineraryAndElevationFragment

private val TAG = Logging.getTag(MainNavDrawerActivity::class.java)
private const val DRAWER_ITEMID_SELECTED_KEY = "DRAWER_ITEM_SELECTED"

abstract class MainNavDrawerActivity : AppCompatActivity(), OnNavigationItemSelectedListener, Route.Listener, OnBackStackChangedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private var selectedItem: Int = 0
    private var currentFragment: Fragment? = null

    private val menuItemIdToFragment = object : SparseArray<Class<out Fragment>>() {
        init {
            put(R.id.nav_journey_planner, RouteMapFragment::class.java)
            put(R.id.nav_itinerary, ItineraryAndElevationFragment::class.java)
            put(R.id.nav_photomap, PhotoMapFragment::class.java)
            put(R.id.nav_addphoto, AddPhotoFragment::class.java)
            put(R.id.nav_blog, BlogFragment::class.java)
            put(R.id.nav_settings, SettingsFragment::class.java)
        }
    }

    private val fragmentToMenuItemId = mapOf(
        RouteMapFragment::class.java to R.id.nav_journey_planner,
        ItineraryAndElevationFragment::class.java to R.id.nav_itinerary,
        PhotoMapFragment::class.java to R.id.nav_photomap,
        AddPhotoFragment::class.java to R.id.nav_addphoto,
        BlogFragment::class.java to R.id.nav_blog,
        SettingsFragment::class.java to R.id.nav_settings
    )

    // The Journey Planner and Photomap are the 'heart' of the app.  Pressing 'back' on any other
    // fragment should eventually return you to whichever of those you were using.
    private val backOutableFragments = setOf(R.id.nav_itinerary, R.id.nav_addphoto, R.id.nav_blog, R.id.nav_settings)

    // If you're in one of these fragments at pause, then you'll be returned to it on resume.
    private val resumableFragments = setOf(R.id.nav_journey_planner, R.id.nav_photomap, R.id.nav_addphoto)

    override fun attachBaseContext(newBase: Context) {
        // Allows the use of Material icon library, see https://github.com/mikepenz/Android-Iconics
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_navdrawer_activity)

        val (burgerIcon, addPhotoIcon, blogIcon, settingsIcon) =
            materialIcons(context = this, icons = listOf(Icon.gmd_menu, Icon.gmd_add_a_photo, Icon.gmd_chat, Icon.gmd_settings))
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

        supportFragmentManager.addOnBackStackChangedListener(this)

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
        // Swap UI fragments based on the selection
        this.supportFragmentManager.beginTransaction().let { ft ->
            ft.replace(R.id.content_frame, instantiateFragmentFor(menuItem))
            if (backOutableFragments.contains(menuItem.itemId))
                ft.addToBackStack(null)
            ft.commit()
        }

        updateMenuDisplayFor(menuItem)

        return true
    }

    override fun onBackStackChanged() {
        val menuItem = navigationView.menu.findItem(currentMenuItemId())
        updateMenuDisplayFor(menuItem)
    }

    private fun currentMenuItemId(): Int {
        val currentFragment = this.supportFragmentManager.findFragmentById(R.id.content_frame)
        return fragmentToMenuItemId[currentFragment.javaClass]!!
    }

    private fun updateMenuDisplayFor(menuItem: MenuItem) {
        // set item as selected to persist highlight
        menuItem.isChecked = true
        // close drawer when item is tapped
        drawerLayout.closeDrawers()
        // Save which item is selected
        selectedItem = menuItem.itemId
        // Update the ActionBar title to be the title of the chosen fragment
        toolbar.title = menuItem.title
    }

    private fun instantiateFragmentFor(menuItem: MenuItem): Fragment {
        val fragmentClass = menuItemIdToFragment.get(menuItem.itemId)
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
            Log.d(TAG, "Loading page with menuItemId=$menuItemId (${menuItem.title})")
            onNavigationItemSelected(menuItem)
            return true
        }
        Log.d(TAG, "Page with menuItemId=$menuItemId could not be found")
        return false
    }

    public override fun onResume() {
        val selectedItem = prefs().getInt(DRAWER_ITEMID_SELECTED_KEY, R.id.nav_journey_planner)
        showPage(selectedItem)
        super.onResume()
        Route.registerListener(this)
        setBlogStateTitle()
    }

    public override fun onPause() {
        Route.unregisterListener(this)
        saveCurrentMenuSelection(currentMenuItemId())
        super.onPause()
    }

    private fun saveCurrentMenuSelection(menuItemId: Int) {
        if (backOutableFragments.contains(menuItemId))
            prefs().edit().let {
                it.putInt(DRAWER_ITEMID_SELECTED_KEY, selectedItem)
                it.apply()
            }
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
