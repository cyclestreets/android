package net.cyclestreets.v2;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import net.cyclestreets.ItineraryFragment;
import net.cyclestreets.PhotoMapFragment;
import net.cyclestreets.RouteMapFragment;

import java.util.ArrayList;
import java.util.List;

public class Main extends ActionBarActivity {
  private NavigationDrawerFragment navDrawer_;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    navDrawer_ = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    navDrawer_.setUp(R.id.navigation_drawer, (DrawerLayout)findViewById(R.id.drawer_layout));
  } // onCreate

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
} // class Main
