package net.cyclestreets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;

public abstract class MainNavDrawerActivity
    extends ActionBarActivity
    implements Route.Listener {
  private NavigationDrawerFragment navDrawer_;
  private List<DrawerItem> pages_;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.mainnavdraweractivity);

    navDrawer_ = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    navDrawer_.setUp(R.id.navigation_drawer, (DrawerLayout)findViewById(R.id.drawer_layout));

    pages_ = new ArrayList<>();

    addDrawerItems();

    navDrawer_.addPages(pages_);

    if (CycleStreetsAppSupport.isFirstRun())
      onFirstRun();
    else if (CycleStreetsAppSupport.isNewVersion())
      onNewVersion();
  } // onCreate

  protected void onFirstRun() { }
  protected void onNewVersion() { }

  protected abstract void addDrawerItems();

  protected void addDrawerFragment(final int titleId,
                                   final int iconId,
                                   final Class<? extends Fragment> fragClass) {
    addDrawerFragment(titleId, iconId, fragClass, null, null);
  } // addDrawerFragment

  protected void addDrawerFragment(final PageTitle title,
                                   final int iconId,
                                   final Class<? extends Fragment> fragClass) {
    addDrawerFragment(title, iconId, fragClass, null, null);
  } // addDrawerFragment

  protected void addDrawerFragment(final int titleId,
                                   final int iconId,
                                   final Class<? extends Fragment> fragClass,
                                   final PageStatus pageStatus) {
    addDrawerFragment(titleId, iconId, fragClass, null, pageStatus);
  } // addDrawerFragment

  protected void addDrawerFragment(final int titleId,
                                   final int iconId,
                                   final Class<? extends Fragment> fragClass,
                                   final PageInitialiser initialiser) {
    addDrawerFragment(titleId, iconId, fragClass, initialiser, null);
  } // addDrawerFragment

  protected void addDrawerFragment(final int titleId,
                                   final int iconId,
                                   final Class<? extends Fragment> fragClass,
                                   final PageInitialiser initialiser,
                                   final PageStatus pageStatus) {
    final String title = getResources().getString(titleId);
    addDrawerFragment(new FixedTitle(title), iconId, fragClass, initialiser, pageStatus);
  } // addDrawerFragment

  protected void addDrawerFragment(final PageTitle title,
                                   final int iconId,
                                   final Class<? extends Fragment> fragClass,
                                   final PageInitialiser initialiser,
                                   final PageStatus pageStatus) {
    final Drawable icon = iconId != -1 ? getResources().getDrawable(iconId) : null;

    pages_.add(new FragmentItem(title, icon, fragClass, initialiser, pageStatus));
  } // addDrawerFragment

  protected void addDrawerActivity(int titleId,
                                   final int iconId,
                                   final Class<? extends Activity> fragClass) {
    addDrawerActivity(titleId, iconId, fragClass, null);
  } // addDrawerActivity

  protected void addDrawerActivity(final int titleId,
                                   final int iconId,
                                   final Class<? extends Activity> fragClass,
                                   final PageStatus pageStatus) {
    final String title = getResources().getString(titleId);
    final Drawable icon = iconId != -1 ? getResources().getDrawable(iconId) : null;

    pages_.add(new ActivityItem(new FixedTitle(title), icon, fragClass, pageStatus));
  } // addDrawerActivity

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    if (!navDrawer_.isDrawerOpen()) {
      restoreActionBar();
      return true;
    }
    return super.onCreateOptionsMenu(menu);
  } // onCreateOptionsMenu

  @Override
  public void onBackPressed() {
    if(navDrawer_.onBackPressed())
      return;
    super.onBackPressed();
  } // onBackPressed


  private void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setTitle(navDrawer_.title());
  } // restoreActionBar

  public void showPage(int page) {
    navDrawer_.selectItem(page);
  } // showPage

  private final String Drawer = "DRAWER";
  @Override
  public void onResume() {
    final int selectedFrag = prefs().getInt(Drawer, -1);
    if (selectedFrag != -1)
      navDrawer_.selectItem(selectedFrag);

    super.onResume();
    Route.registerListener(this);
  } // onResume

  @Override
  public void onPause() {
    Route.unregisterListener(this);

    final SharedPreferences.Editor edit = prefs().edit();
    edit.putInt(Drawer, navDrawer_.selectedItem());
    edit.commit();

    navDrawer_.fragment().onPause();

    super.onPause();
  } // onPause

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    supportInvalidateOptionsMenu();
  } // onNewJourney
  public void onResetJourney() {
    supportInvalidateOptionsMenu();
  } // onResetJourney

  private SharedPreferences prefs() {
    return getSharedPreferences("net.cyclestreets.CycleStreets", Context.MODE_PRIVATE);
  } // prefs()

  //////////////////////////////////////////
  //////////////////////////////////////////
  public static class NavigationDrawerFragment extends Fragment {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private ActionBarDrawerToggle drawerToggle_;

    private DrawerLayout drawerLayout_;
    private ListView drawerListView_;
    private PageInfoAdapter drawerContents_;
    private View fragmentContainerView_;

    private int currentSelectedPosition_ = 0;
    private int nextSelectedPosition_ = 0;
    private boolean firstRun_;

    public NavigationDrawerFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      firstRun_ = CycleStreetsAppSupport.isFirstRun();

      if (savedInstanceState != null)
        currentSelectedPosition_ = savedInstanceState.getInt(STATE_SELECTED_POSITION);
    } // onCreate

    void addPages(final List<DrawerItem> pages) {
      drawerContents_ = new PageInfoAdapter(this, pages, getActionBar().getThemedContext());
      drawerListView_.setAdapter(drawerContents_);

      selectItem(currentSelectedPosition_);
      drawerListView_.setItemChecked(currentSelectedPosition_, true);
    } // addDrawerItems

    public String title() { return drawerContents_.getItem(currentSelectedPosition_).title(); }
    public Fragment fragment() { return ((FragmentItem)drawerContents_.getItem(currentSelectedPosition_)).fragment(); }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      // Indicate that this fragment would like to influence the set of actions in the action bar.
      setHasOptionsMenu(true);
    } // onActivityCreated

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      drawerListView_ = (ListView)inflater.inflate(R.layout.navigation_drawer, container, false);
      drawerListView_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          nextSelectedPosition_ = position;
          drawerLayout_.closeDrawer(fragmentContainerView_);
        } // onItemClick
      });

      return drawerListView_;
    } // onCreateView

    public boolean isDrawerOpen() {
      return drawerLayout_ != null && drawerLayout_.isDrawerOpen(fragmentContainerView_);
    } // isDrawerOpen

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
      fragmentContainerView_ = getActivity().findViewById(fragmentId);
      drawerLayout_ = drawerLayout;

      // set a custom shadow that overlays the maintabbedactivity content when the drawer opens
      drawerLayout_.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
      // set up the drawer's list view with items and click listener

      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);

      // ActionBarDrawerToggle ties together the the proper interactions
      // between the navigation drawer and the action bar app icon.
      drawerToggle_ = new ActionBarDrawerToggle(
              getActivity(),                    /* host Activity */
              drawerLayout_,                    /* DrawerLayout object */
              R.drawable.apptheme_ic_navigation_drawer,             /* nav drawer image to replace 'Up' caret */
              R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
              R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
          ) {
            @Override
            public void onDrawerClosed(View drawerView) {
              super.onDrawerClosed(drawerView);
              if (!isAdded()) { return; }
              getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
              if (nextSelectedPosition_ != currentSelectedPosition_)
                selectItem(nextSelectedPosition_);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
              super.onDrawerOpened(drawerView);
              if (!isAdded()) { return; }
              getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
          };

      if (firstRun_) {
        drawerLayout_.openDrawer(fragmentContainerView_);
        firstRun_ = false;
      } // if ...

      // Defer code dependent on restoration of previous instance state.
      drawerLayout_.post(new Runnable() {
        @Override
        public void run() {
          drawerToggle_.syncState();
        }
      });
      drawerLayout_.setDrawerListener(drawerToggle_);
    } // setUp

    public void selectItem(int position) {
      if (position >= drawerContents_.getCount())
        return;

      final DrawerItem di = drawerContents_.getItem(position);
      if (drawerLayout_ != null)
        drawerLayout_.closeDrawer(fragmentContainerView_);
      getActivity().supportInvalidateOptionsMenu();

      if (di instanceof FragmentItem) {
        currentSelectedPosition_ = position;
        drawerListView_.setItemChecked(position, true);

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        final Fragment newFrag = ((FragmentItem)di).create();

        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, newFrag);
        ft.commit();
      } //
      if (di instanceof ActivityItem) {
        final Intent intent = new Intent(getActivity(), ((ActivityItem)di).activityClass());
        startActivity(intent);
      } //
    } // selectItem

    public int selectedItem() { return currentSelectedPosition_; }

    @Override
    public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition_);
    } // onSaveInstanceState

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      drawerToggle_.onConfigurationChanged(newConfig);
    } // onConfigurationChanged

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      // If the drawer is open, show the global app actions in the action bar. See also
      // showGlobalContextActionBar, which controls the top-left area of the action bar.
      if (drawerLayout_ != null && isDrawerOpen())
        showGlobalContextActionBar();
      else
        fragment().onCreateOptionsMenu(menu, inflater);

      super.onCreateOptionsMenu(menu, inflater);
    } // onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      if (drawerToggle_.onOptionsItemSelected(item))
        return true;

      if (fragment().onOptionsItemSelected(item))
        return true;

      return super.onOptionsItemSelected(item);
    } // onOptionsItemSelected

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
      if (drawerLayout_ != null && isDrawerOpen())
        ;
      else
        fragment().onPrepareOptionsMenu(menu);

      super.onPrepareOptionsMenu(menu);
    } // onPrepareOptionsMenu

    public boolean onBackPressed() {
      if (isDrawerOpen()) {
        drawerLayout_.closeDrawers();
        return true;
      } // if ...
      if(!(fragment() instanceof Undoable))
        return false;
      return ((Undoable)fragment()).onBackPressed();
    } // onBackPressed

    private void showGlobalContextActionBar() {
      ActionBar actionBar = getActionBar();
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
      actionBar.setTitle(R.string.app_name);
    } // showGlobalContextActionBar

    private ActionBar getActionBar() {
      return ((ActionBarActivity) getActivity()).getSupportActionBar();
    } // getActionBar
  } // NavigationDrawerFragment


  //////////////////////////////////////////
  //////////////////////////////////////////
  static class PageInfoAdapter extends BaseAdapter {
    private final List<DrawerItem> pageInfo_;
    private final List<DrawerItem> activePages_;
    private final LayoutInflater inflater_;
    private final NavigationDrawerFragment parentFrag_;
    private final Drawable themeColor_;
    private final Context context_;

    PageInfoAdapter(final NavigationDrawerFragment parentFrag,
                    final List<DrawerItem> pageInfo,
                    final Context context) {
      context_ = context;
      parentFrag_ = parentFrag;
      pageInfo_ = pageInfo;
      for (DrawerItem di : pageInfo_)
        di.setAdapter(this);
      activePages_ = new ArrayList<>();
      inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      themeColor_ = context.getResources().getDrawable(R.color.apptheme_color);

      buildActiveList();
    } // PageInfoAdaptor

    private void buildActiveList() {
      activePages_.clear();
      for (DrawerItem di : pageInfo_)
        if (di.enabled())
          activePages_.add(di);
    } // buildActiveList

    @Override
    public void notifyDataSetChanged() {
      buildActiveList();
      super.notifyDataSetChanged();
    } // notifyDataSetChanged

    @Override
    public int getCount() { return activePages_.size(); }

    @Override
    public DrawerItem getItem(int position) { return activePages_.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      final View v = inflater_.inflate(R.layout.navigation_item, parent, false);

      final boolean highlight = (position == parentFrag_.currentSelectedPosition_);

      setText(v, getItem(position).title(), highlight);
      setIcon(v, getItem(position).icon());

      return v;
    } // getView

    private void setText(final View v, final String t, boolean highlight) {
      final TextView n = (TextView)v.findViewById(R.id.menu_name);
      n.setText(t);

      if (highlight) {
        v.setBackgroundDrawable(themeColor_);
        n.setTextAppearance(context_, android.R.style.TextAppearance);
      } // if ...
    } // setText

    private void setIcon(final View v, final Drawable icon) {
      if (icon == null)
        return;
      final ImageView iv = (ImageView)v.findViewById(R.id.menu_icon);
      iv.setImageDrawable(icon);
    } // setIcon
  } // class PageInfoAdaptor

  //////////////////////////////////////////
  //////////////////////////////////////////
  private static abstract class DrawerItem {
    private PageTitle title_;
    private Drawable icon_;
    private PageStatus pageStatus_;

    public DrawerItem(final PageTitle title,
                      final Drawable icon,
                      final PageStatus pageStatus) {
      title_ = title;
      icon_ = icon;
      pageStatus_ = pageStatus;
    } // DrawerItem

    public String title() { return title_.title(); }
    public Drawable icon() { return icon_; }
    public boolean enabled() { return (pageStatus_ != null) ? pageStatus_.enabled() : true; }

    @Override
    public String toString() { return title_.title(); }

    public void setAdapter(final BaseAdapter adapter) {
      if (pageStatus_ != null)
        pageStatus_.setAdapter(adapter);
    } // setAdapter
  } // DrawerItem

  private static class FragmentItem extends DrawerItem {
    private Class<? extends Fragment> fragClass_;
    private Fragment fragment_;
    private PageInitialiser initialiser_;

    public FragmentItem(final PageTitle title,
                        final Drawable icon,
                        final Class<? extends Fragment> fragClass,
                        final PageInitialiser initialiser,
                        final PageStatus pageStatus) {
      super(title, icon, pageStatus);
      fragClass_ = fragClass;
      initialiser_ = initialiser;
    } // FragmentItem

    public Fragment create() {
      try {
        fragment_ = fragClass_.newInstance();
        if (initialiser_ != null)
          initialiser_.initialise(fragment_);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } // try

      return fragment_;
    } // attach

    public Fragment fragment() { return fragment_; }
  } // FragmentInfo

  private static class ActivityItem extends DrawerItem {
    private Class<? extends Activity> activityClass_;

    public ActivityItem(final PageTitle title,
                        final Drawable icon,
                        final Class<? extends Activity> activityClass,
                        final PageStatus pageStatus) {

      super(title, icon, pageStatus);
      activityClass_ = activityClass;
    } // ActivityItem

    public Class<? extends Activity> activityClass() {
      return activityClass_;
    } // activityClass
  } // ActivityItem

  public interface PageTitle {
    String title();
  } // PageTitle

  public class FixedTitle implements PageTitle {
    private final String title_;
    public FixedTitle(String t) { title_ = t; }
    public String title() { return title_; }
  } // FixedTitle

  public interface PageInitialiser {
    void initialise(final Fragment page);
  } // PageInitialiser

  public interface PageStatus {
    void setAdapter(BaseAdapter adapter);
    boolean enabled();
  } // PageStatus
} // class Main
