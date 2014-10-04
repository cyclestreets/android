package net.cyclestreets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.util.TurnIcons;

public abstract class MainNavDrawerActivity extends ActionBarActivity {
  private NavigationDrawerFragment navDrawer_;
  private List<PageInfo> pages_;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.mainnavdraweractivity);

    navDrawer_ = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    navDrawer_.setUp(R.id.navigation_drawer, (DrawerLayout)findViewById(R.id.drawer_layout));

    pages_ = new ArrayList<>();

    addPages();

    navDrawer_.addPages(pages_);
  } // onCreate

  protected abstract void addPages();

  protected void addPage(final String title,
                         final int iconId,
                         final Class<? extends Fragment> fragClass) {
    addPage(title, iconId, fragClass, null);
  } // addPage

  protected void addPage(final String title,
                         final int iconId,
                         final Class<? extends Fragment> fragClass,
                         final PageInitialiser initialiser) {
    final Drawable icon = iconId != -1 ? getResources().getDrawable(iconId) : null;
    pages_.add(new PageInfo(title, icon, fragClass, initialiser));
  } // addPage

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(navDrawer_.title());
  } // restoreActionBar

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    if (!navDrawer_.isDrawerOpen()) {
      restoreActionBar();
      return true;
    }
    return super.onCreateOptionsMenu(menu);
  } // onCreateOptionsMenu

  //////////////////////////////////////////
  //////////////////////////////////////////
  public static class NavigationDrawerFragment extends Fragment {
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

    private List<PageInfo> fragments_;

    public NavigationDrawerFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      fragments_ = new ArrayList<>();

      // Read in the flag indicating whether or not the user has demonstrated awareness of the
      // drawer. See PREF_USER_LEARNED_DRAWER for details.
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
      mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

      if (savedInstanceState != null) {
        mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        mFromSavedInstanceState = true;
      }
    } // onCreate

    public void addPages(final List<PageInfo> pages) {
      fragments_.addAll(pages);

      mDrawerListView.setAdapter(new PageInfoAdapter(this, fragments_, getActionBar().getThemedContext()));

      selectItem(mCurrentSelectedPosition);
      mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
    } // addPages

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
          R.layout.navigation_drawer, container, false);
      mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          selectItem(position);
        }
      });

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

      // set a custom shadow that overlays the maintabbedactivity content when the drawer opens
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


  //////////////////////////////////////////
  //////////////////////////////////////////
  static class PageInfoAdapter extends BaseAdapter {
    private final List<PageInfo> pageInfo_;
    private final LayoutInflater inflater_;
    private final NavigationDrawerFragment parentFrag_;

    PageInfoAdapter(final NavigationDrawerFragment parentFrag,
                    final List<PageInfo> pageInfo,
                    final Context context) {
      parentFrag_ = parentFrag;
      pageInfo_ = pageInfo;
      inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    } // PageInfoAdaptor

    @Override
    public int getCount() { return pageInfo_.size(); }

    @Override
    public PageInfo getItem(int position) { return pageInfo_.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      final View v = inflater_.inflate(R.layout.navigation_item, parent, false);

      final boolean highlight = (position == parentFrag_.mCurrentSelectedPosition);

      setText(v, getItem(position).title(), highlight);
      setIcon(v, getItem(position).icon());

      if (highlight)
        v.setBackgroundColor(Color.GREEN);

      return v;
    } // getView

    private void setText(final View v, final String t, final boolean highlight) {
      final TextView n = (TextView)v.findViewById(R.id.menu_name);
      n.setText(t);
      if (highlight)
        n.setTextColor(Color.BLACK);
    } // setText

    private void setIcon(final View v, final Drawable icon) {
      if (icon == null)
        return;
      final ImageView iv = (ImageView)v.findViewById(R.id.menu_icon);
      iv.setImageDrawable(icon);
    } // setTurnIcon
  } // class PageInfoAdaptor

  //////////////////////////////////////////
  //////////////////////////////////////////
  private static class PageInfo {
    private String title_;
    private Drawable icon_;
    private Class<? extends Fragment> fragClass_;
    private Fragment fragment_;
    private PageInitialiser initialiser_;


    public PageInfo(final String title,
                    final Drawable icon,
                    final Class<? extends Fragment> fragClass,
                    final PageInitialiser initialiser) {
      title_ = title;
      icon_ = icon;
      fragClass_ = fragClass;
      initialiser_ = initialiser;
    } // PageInfo

    public String title() { return title_; }
    public Fragment fragment() {
      try {
        if (fragment_ == null) {
          fragment_ = fragClass_.newInstance();
          if (initialiser_ != null)
            initialiser_.initialise(fragment_);
        } // if ...
      } catch (Exception e) {
        throw new RuntimeException(e);
      } // try
      return fragment_;
    } // fragment
    public Drawable icon() { return icon_; }
    @Override
    public String toString() { return title_; }
  } // PageInfo

  public static interface PageInitialiser {
    void initialise(final Fragment page);
  } // PageInitialiser
} // class Main
