package net.cyclestreets.views.overlay;

import android.view.Menu;

public interface DynamicMenuListener extends MenuListener 
{
	boolean onPrepareOptionsMenu(final Menu menu);
} // interface DynamicMenuListener
