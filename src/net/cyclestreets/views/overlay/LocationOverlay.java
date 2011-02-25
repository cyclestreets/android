package net.cyclestreets.views.overlay;

import net.cyclestreets.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class LocationOverlay extends MyLocationOverlay 
							 implements TapListener, MenuListener
{
	static private String LOCATION_ON = "Show Location";
	static private String LOCATION_OFF = "Location Off";

	private final int offset_;
	private final float radius_;

	private final OverlayButton locationButton_;	
	
	private final MapView mapView_;
	
	public LocationOverlay(final Context context, 
						   final MapView mapView) 
	{
		super(context, mapView);
		
		mapView_ = mapView;
		
		offset_ = OverlayHelper.offset(context);
		radius_ = OverlayHelper.cornerRadius(context);

		final Resources res = context.getResources();
		locationButton_ = new OverlayButton(res.getDrawable(R.drawable.ic_menu_mylocation),
											offset_,
											offset_,
											radius_);		
	} // LocationOverlay
	
	public void enableLocation(final boolean enable)
	{
		if(enable)
			enableMyLocation();
		else
			disableMyLocation();
	} // enableLocation
	
	public void enableAndFollowLocation(final boolean enable)
	{
		if(enable) 
		{
			try {
				enableMyLocation();
				followLocation(true);
				final Location lastFix = getLastFix();
				if (lastFix != null)
					mapView_.getController().setCenter(new GeoPoint(lastFix));
			} // try
			catch(RuntimeException e) {
				// might not have location service
			} // catch
		} 
		else
		{
			followLocation(false);
			disableMyLocation();
		} // if ...
		
		mapView_.invalidate();
	} // enableAndFollowLocation
	
	////////////////////////////////////////////
	@Override
	public void onDraw(final Canvas canvas, final MapView mapView) 
	{
		// I'm not thrilled about this but there isn't any other way (short of killing
		// and recreating the overlay) of turning off the little here-you-are man
		if(!isMyLocationEnabled())
			return;
		
		super.onDraw(canvas, mapView);
	} // onDraw
	
	@Override
	protected void onDrawFinished(final Canvas canvas, final MapView mapView) 
	{
	} // onDrawFinished
	
	public void drawButtons(final Canvas canvas, final MapView mapView)
	{
		drawButtons(canvas);
	} // onDrawFinished
	
	private void drawButtons(final Canvas canvas)
	{
		locationButton_.pressed(isMyLocationEnabled());
		locationButton_.draw(canvas);
	} // drawLocationButton

	////////////////////////////////////////////////
	public boolean onCreateOptionsMenu(final Menu menu)
    {
    	menu.add(0, R.string.ic_menu_mylocation, Menu.NONE, isMyLocationEnabled() ? LOCATION_OFF : LOCATION_ON).setIcon(R.drawable.ic_menu_mylocation);
    	return true;
	} // onCreateOptionsMenu

	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
        if(item.getItemId() != R.string.ic_menu_mylocation)
        	return false;
        
        enableLocation(!isMyLocationEnabled());
        
        item.setTitle(isMyLocationEnabled() ? LOCATION_OFF : LOCATION_ON);
        
        return true;
	} // onMenuItemSelected
	//////////////////////////////////////////////
	@Override
    public boolean onSingleTap(final MotionEvent event) 
	{
    	return tapLocation(event);
    } // onSingleTapUp
	
	@Override
	public boolean onDoubleTap(final MotionEvent event)
	{
		return locationButton_.hit(event);
	} // onDoubleTap
    
	private boolean tapLocation(final MotionEvent event)
	{
		if(!locationButton_.hit(event))
			return false;
		
		enableAndFollowLocation(!isMyLocationEnabled()); 

		return true;
	} // tapLocation
	
} // LocationOverlay
