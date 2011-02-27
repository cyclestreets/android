package net.cyclestreets.views.overlay;

import net.cyclestreets.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Menu;
import android.view.MenuItem;

public class StoredRouteOverlay extends Overlay implements DynamicMenuListener
{
	public interface Callback {
		void onStoredRouteNow(final int routeId);
	} // Callback
	
	public StoredRouteOverlay(final Context context,
							  final Callback callback)
	{
		super(context);
	} // StoredRouteOverlay

	/////////////////////////////////////////////////////////////////
	public boolean onCreateOptionsMenu(final Menu menu)
	{
    	menu.add(0, R.string.ic_menu_saved_routes, Menu.NONE, R.string.ic_menu_saved_routes).setIcon(R.drawable.ic_menu_places);
    	return true;
	} // onCreateOptionsMenu
	
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		return false;
	} // onPrepareOptionsMenu
	
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
		return false;
	} // onMenuItemSelected

	/////////////////////////////////////////////////////////////////
	@Override
	protected void onDraw(Canvas arg0, MapView arg1) { }	
	@Override
	protected void onDrawFinished(Canvas arg0, MapView arg1) { } 
} // class StoredRouteOverlay
