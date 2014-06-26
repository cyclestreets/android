package net.cyclestreets.v2;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import net.cyclestreets.ItineraryFragment;
import net.cyclestreets.PhotoMapFragment;
import net.cyclestreets.RouteMapFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {
  /**
   * Remember the position of the selected item.
   */
  private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

  /**
   * Per the design guidelines, you should show the drawer on launch until the user manually
   * expands it. This shared preference tracks this.
   */
  private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

  /**
   * Helper component that ties the action bar to the navigation drawer.
   */
  private ActionBarDrawerToggle mDrawerToggle;

  private DrawerLayout mDrawerLayout;
  private ListView mDrawerListView;
  private View mFragmentContainerView;

  private int mCurrentSelectedPosition = 0;
  private boolean mFromSavedInstanceState;
  private boolean mUserLearnedDrawer;

  public class PageInfo {
    private String title_;
    private Class<? extends Fragment> fragClass_;
    private Fragment fragment_;

    public PageInfo(final String title, final Class<? extends Fragment> fragClass) {
      title_ = title;
      fragClass_ = fragClass;
    } // PageInfo

    public String title() { return title_; }
    public Fragment fragment() {
      try {
        if (fragment_ == null)
          fragment_ = fragClass_.newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      } // try
      return fragment_;
    } // fragment

    @Override
    public String toString() { return title_; }
  } // PageInfo

  private List<PageInfo> fragments_;

  public NavigationDrawerFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    fragments_ = new ArrayList<>();
    fragments_.add(new PageInfo("Route Map", RouteMapFragment.class));
    fragments_.add(new PageInfo("Itinerary", ItineraryFragment.class));
    fragments_.add(new PageInfo("Photo Map", PhotoMapFragment.class));

    // Read in the flag indicating whether or not the user has demonstrated awareness of the
    // drawer. See PREF_USER_LEARNED_DRAWER for details.
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

    if (savedInstanceState != null) {
      mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
      mFromSavedInstanceState = true;
    }

    // Select either the default item (0) or the last selected item.
    selectItem(mCurrentSelectedPosition);
  }

  public String title() { return fragments_.get(mCurrentSelectedPosition).title(); }
  public Fragment fragment() { return fragments_.get(mCurrentSelectedPosition).fragment(); }

  @Override
  public void onActivityCreated (Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    // Indicate that this fragment would like to influence the set of actions in the action bar.
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mDrawerListView = (ListView) inflater.inflate(
        R.layout.fragment_navigation_drawer, container, false);
    mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
      }
    });
    mDrawerListView.setAdapter(new ArrayAdapter<>(
        getActionBar().getThemedContext(),
        android.R.layout.simple_list_item_1,
        android.R.id.text1,
        fragments_));
    mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
    return mDrawerListView;
  }

  public boolean isDrawerOpen() {
    return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
  }

  /**
   * Users of this fragment must call this method to set up the navigation drawer interactions.
   *
   * @param fragmentId   The android:id of this fragment in its activity's layout.
   * @param drawerLayout The DrawerLayout containing this fragment's UI.
   */
  public void setUp(int fragmentId, DrawerLayout drawerLayout) {
    mFragmentContainerView = getActivity().findViewById(fragmentId);
    mDrawerLayout = drawerLayout;

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    // set up the drawer's list view with items and click listener

    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the navigation drawer and the action bar app icon.
    mDrawerToggle = new ActionBarDrawerToggle(
        getActivity(),                    /* host Activity */
        mDrawerLayout,                    /* DrawerLayout object */
        R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
        R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
        R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
    ) {
      @Override
      public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        if (!isAdded()) {
          return;
        }

        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        if (!isAdded()) {
          return;
        }

        if (!mUserLearnedDrawer) {
          // The user manually opened the drawer; store this flag to prevent auto-showing
          // the navigation drawer automatically in the future.
          mUserLearnedDrawer = true;
          SharedPreferences sp = PreferenceManager
              .getDefaultSharedPreferences(getActivity());
          sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
        }

        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }
    };

    // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
    // per the navigation drawer design guidelines.
    if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
      mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    // Defer code dependent on restoration of previous instance state.
    mDrawerLayout.post(new Runnable() {
      @Override
      public void run() {
        mDrawerToggle.syncState();
      }
    });

    mDrawerLayout.setDrawerListener(mDrawerToggle);
  }

  private void selectItem(int position) {
    mCurrentSelectedPosition = position;
    if (mDrawerListView != null)
      mDrawerListView.setItemChecked(position, true);
    if (mDrawerLayout != null)
      mDrawerLayout.closeDrawer(mFragmentContainerView);

    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
    fragmentManager.beginTransaction()
                   .replace(R.id.container, fragments_.get(position).fragment())
                   .commit();
  } // selectItem

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Forward the new configuration the drawer toggle component.
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // If the drawer is open, show the global app actions in the action bar. See also
    // showGlobalContextActionBar, which controls the top-left area of the action bar.
    if (mDrawerLayout != null && isDrawerOpen())
      showGlobalContextActionBar();
    else
      fragment().onCreateOptionsMenu(menu, inflater);

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mDrawerToggle.onOptionsItemSelected(item))
      return true;

    if (fragment().onOptionsItemSelected(item))
      return true;

    return super.onOptionsItemSelected(item);
  } // onOptionsItemSelected

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    if (mDrawerLayout != null && isDrawerOpen())
      ;
    else
      fragment().onPrepareOptionsMenu(menu);

    super.onPrepareOptionsMenu(menu);
  } // onPrepareOptionsMenu

  /**
   * Per the navigation drawer design guidelines, updates the action bar to show the global app
   * 'context', rather than just what's in the current screen.
   */
  private void showGlobalContextActionBar() {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setTitle(R.string.app_name);
  }

  private ActionBar getActionBar() {
    return ((ActionBarActivity) getActivity()).getSupportActionBar();
  }
} // NavigationDrawerFragment
