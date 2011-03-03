package net.cyclestreets.views.overlay;

import net.cyclestreets.R;
import net.cyclestreets.content.RouteSummary;
import net.cyclestreets.planned.Route;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class StoredRouteOverlay extends Overlay implements DynamicMenuListener
{
	public interface Callback {
		void onStoredRouteNow(final int routeId);
	} // Callback

	static private int StoredGroup = 63782189;
	private SubMenu routeMenu_;
	private Callback listener_;
	
	public StoredRouteOverlay(final Context context,
							  final Callback listener)
	{
		super(context);
		listener_ = listener;
	} // StoredRouteOverlay

	/////////////////////////////////////////////////////////////////
	public boolean onCreateOptionsMenu(final Menu menu)
	{
    	routeMenu_ = menu.addSubMenu(0, R.string.ic_menu_saved_routes, Menu.NONE, R.string.ic_menu_saved_routes).setIcon(R.drawable.ic_menu_places);
    	return true;
	} // onCreateOptionsMenu
	
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		final MenuItem i = menu.findItem(R.string.ic_menu_saved_routes);
		if(Route.storedCount() == 0)
		{
			i.setVisible(false);
			return true;
		} // if ...
		i.setVisible(true);

		routeMenu_.removeGroup(StoredGroup);
		for(final RouteSummary n : Route.storedRoutes())
			routeMenu_.add(StoredGroup, n.id(), Menu.NONE, n.title());
		
		return true;
	} // onPrepareOptionsMenu
	
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
		if(item.getGroupId() != StoredGroup)
			return false;
		
		listener_.onStoredRouteNow(item.getItemId());
		return true;
	} // onMenuItemSelected

	/////////////////////////////////////////////////////////////////
	@Override
	protected void onDraw(Canvas arg0, MapView arg1) { }	
	@Override
	protected void onDrawFinished(Canvas arg0, MapView arg1) { } 
} // class StoredRouteOverlay
