package net.cyclestreets.views.overlay;

import android.view.Menu;
import android.view.MenuItem;

public interface MenuListener 
{
	public void onCreateOptionsMenu(final Menu menu);
  public void onPrepareOptionsMenu(final Menu menu);
	public boolean onMenuItemSelected(final int featureId, final MenuItem item);
} // interface MenuListener
