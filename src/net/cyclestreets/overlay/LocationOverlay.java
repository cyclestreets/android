package net.cyclestreets.overlay;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.MyLocationOverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

public class LocationOverlay extends MyLocationOverlay {
	private final Drawable locationButton_;
	private final Rect locationButtonPos_;
	
	public LocationOverlay(final Context context, final OpenStreetMapView mapView, ResourceProxy resProxy) {
		super(context, mapView, resProxy);
		
		locationButton_ = context.getResources().getDrawable(android.R.drawable.ic_menu_mylocation);

		final int offset = (int)(8.0 * context.getResources().getDisplayMetrics().density);		
        locationButtonPos_ = new Rect(offset, offset, offset + locationButton_.getIntrinsicWidth(), offset + locationButton_.getIntrinsicHeight());
        

	} // LocationOverlay

	@Override
	protected void onDrawFinished(final Canvas canvas, final OpenStreetMapView mapView) {
        final Rect screen = canvas.getClipBounds();
        screen.offset(locationButtonPos_.left, locationButtonPos_.top);
        screen.right = screen.left + locationButtonPos_.width();
        screen.bottom = screen.top + locationButtonPos_.height();
        
        locationButton_.setBounds(screen);
        locationButton_.draw(canvas);
 	} // onDrawFinished

	@Override
	public boolean onSingleTapUp(final MotionEvent event, final OpenStreetMapView mapView)
	{
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		if(!locationButtonPos_.contains(x, y))
			return super.onSingleTapUp(event, mapView);
		
		toggleMyLocation();
		
		return true;
	} // onSingleTapUp

} // LocationOverlay
