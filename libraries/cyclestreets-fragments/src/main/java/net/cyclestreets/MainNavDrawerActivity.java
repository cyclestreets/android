package net.cyclestreets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;

import android.view.View;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsContextWrapper;

import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.util.Logging;
import net.cyclestreets.util.Theme;

import static android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;

public abstract class MainNavDrawerActivity extends AppCompatActivity
    implements OnNavigationItemSelectedListener, Route.Listener {
  private static final String TAG = Logging.getTag(MainNavDrawerActivity.class);

  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  private int selectedItem;

  private SparseArray<Class<? extends Fragment>> itemToFragment = new SparseArray<Class<? extends Fragment>>() {{
    put(R.id.nav_journey_planner, RouteMapFragment.class);
    put(R.id.nav_itinerary, ItineraryAndElevationFragment.class);
    put(R.id.nav_photomap, PhotoMapFragment.class);
    put(R.id.nav_addphoto, PhotoUploadFragment.class);
    put(R.id.nav_blog, BlogFragment.class);
    put(R.id.nav_settings, SettingsFragment.class);
  }};

  @Override
  protected void attachBaseContext(Context newBase) {
    // Allows the use of Material icon library, see https://github.com/mikepenz/Android-Iconics
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_navdrawer_activity);

    drawerLayout = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    navigationView.getMenu().findItem(R.id.nav_itinerary).setEnabled(Route.available());
    setBlogStateTitle();

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setVisibility(View.VISIBLE);
    setSupportActionBar(toolbar);
    ActionBar actionbar = getSupportActionBar();
    actionbar.setDisplayHomeAsUpEnabled(true);
    Drawable burger = new IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_menu)
        .color(Theme.lowlightColor(this))
        .sizeDp(24);
    actionbar.setHomeAsUpIndicator(burger);

    if (CycleStreetsAppSupport.isFirstRun())
      onFirstRun();
    else if (CycleStreetsAppSupport.isNewVersion())
      onNewVersion();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        drawerLayout.openDrawer(Gravity.START);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  //////////// OnNavigationItemSelectedListener method implementation
  public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
    // set item as selected to persist highlight
    menuItem.setChecked(true);
    // close drawer when item is tapped
    drawerLayout.closeDrawers();

    // Save which item is selected
    selectedItem = menuItem.getItemId();

    // Swap UI fragments based on the selection
    final FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.content_frame, instantiateFragmentFor(menuItem));
    ft.commit();
    return true;
  }
  private Fragment instantiateFragmentFor(@NonNull MenuItem menuItem) {
    Class<? extends Fragment> fragmentClass = itemToFragment.get(menuItem.getItemId());
    try {
      return fragmentClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  protected void onFirstRun() { }
  protected void onNewVersion() { }

  public void showPage(int menuItemId) {
    MenuItem menuItem = navigationView.getMenu().findItem(menuItemId);
    if (menuItem != null) {
      Log.d(TAG, "Loading page with menuItemId=" + menuItemId + " (" + menuItem.getTitle() + ")");
      onNavigationItemSelected(menuItem);
    }
  }

  private static final String DRAWER_ITEMID_SELECTED_KEY = "DRAWER_ITEM_SELECTED";

  @Override
  public void onResume() {
    final int selectedItem = prefs().getInt(DRAWER_ITEMID_SELECTED_KEY, R.id.nav_journey_planner);
    if (selectedItem != -1) {
      this.selectedItem = selectedItem;
      showPage(selectedItem);
    }

    super.onResume();
    Route.registerListener(this);
    setBlogStateTitle();
  }

  private void setBlogStateTitle() {
    String title = getString(BlogState.INSTANCE.isBlogUpdateAvailable(this) ? R.string.blog_updated : R.string.blog);
    navigationView.getMenu().findItem(R.id.nav_blog).setTitle(title);
    invalidateOptionsMenu();
  }

  @Override
  public void onPause() {
    Route.unregisterListener(this);

    final SharedPreferences.Editor edit = prefs().edit();
    edit.putInt(DRAWER_ITEMID_SELECTED_KEY, selectedItem);
    edit.apply();

    super.onPause();
  }

  private SharedPreferences prefs() {
    return getSharedPreferences("net.cyclestreets.CycleStreets", Context.MODE_PRIVATE);
  }

  ////////// Route.Listener method implementations
  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    navigationView.getMenu().findItem(R.id.nav_itinerary).setEnabled(Route.available());
    invalidateOptionsMenu();
  }
  @Override
  public void onResetJourney() {
    navigationView.getMenu().findItem(R.id.nav_itinerary).setEnabled(Route.available());
    invalidateOptionsMenu();
  }
}
