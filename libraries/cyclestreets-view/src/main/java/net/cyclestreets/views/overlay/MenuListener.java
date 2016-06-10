package net.cyclestreets.views.overlay;

import android.view.Menu;
import android.view.MenuItem;

public interface MenuListener 
{
  void onCreateOptionsMenu(final Menu menu);
  void onPrepareOptionsMenu(final Menu menu);
  boolean onMenuItemSelected(final int featureId, final MenuItem item);
} // interface MenuListener
