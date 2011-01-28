package net.cyclestreets.overlay;

import net.cyclestreets.R;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.MyLocationOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class LocationOverlay extends MyLocationOverlay {
	public interface Callback {
		void onRouteNow(final GeoPoint from, final GeoPoint to);
		void onClearRoute();
	} // Callback
	
	private final Drawable locationButton_;
	private final Drawable greenWisp_;
	private final Drawable redWisp_;

	private final Rect locationButtonPos_;
	
	private final Callback callback_;
	
	private final GestureDetector gestureDetector_;
	private final OpenStreetMapView mapView_;
	
	private OpenStreetMapViewOverlayItem startItem_;
	private OpenStreetMapViewOverlayItem endItem_;

	
	public LocationOverlay(final Context context, 
						   final OpenStreetMapView mapView,
						   final Callback callback,
						   final ResourceProxy resProxy) {
		super(context, mapView, resProxy);
		
		mapView_ = mapView;
		callback_ = callback;
		
		final Resources res = context.getResources();
		locationButton_ = res.getDrawable(android.R.drawable.ic_menu_mylocation);
		greenWisp_ = res.getDrawable(R.drawable.green_wisp_36x30);
		redWisp_ = res.getDrawable(R.drawable.red_wisp_36x30);

		final int offset = (int)(8.0 * context.getResources().getDisplayMetrics().density);		
        locationButtonPos_ = new Rect(offset, offset, offset + locationButton_.getIntrinsicWidth(), offset + locationButton_.getIntrinsicHeight());
        
		final SingleTapDetector tapDetector = new SingleTapDetector(this);
		gestureDetector_ = new GestureDetector(context, tapDetector);
		gestureDetector_.setOnDoubleTapListener(tapDetector);
		
		startItem_ = null;
		endItem_ = null;
	} // LocationOverlay

	public void setRoute(final GeoPoint start, final GeoPoint end)
	{
		setStart(start);
		setEnd(end);
	} // setRoute
	
	public void setStart(final GeoPoint point)
	{
		startItem_ = addMarker(point, "start", greenWisp_);
	} // setStart
	
	public void setEnd(final GeoPoint point)
	{
		endItem_ = addMarker(point, "finish", redWisp_);
	} // setEnd
	
	private OpenStreetMapViewOverlayItem addMarker(final GeoPoint point, final String label, final Drawable icon)
	{
		final OpenStreetMapViewOverlayItem marker = new OpenStreetMapViewOverlayItem(label, label, point);
		marker.setMarker(icon);
		marker.setMarkerHotspot(new Point(0,30));
		return marker;
	} // addMarker

	////////////////////////////////////////////
	@Override
	protected void onDrawFinished(final Canvas canvas, final OpenStreetMapView mapView) {
		drawLocationButton(canvas);
		
        final OpenStreetMapViewProjection projection = mapView.getProjection();
        drawMarker(canvas, projection, startItem_);
        drawMarker(canvas, projection, endItem_);
 	} // onDrawFinished
	
	private void drawLocationButton(final Canvas canvas)
	{
        final Rect screen = canvas.getClipBounds();
        screen.offset(locationButtonPos_.left, locationButtonPos_.top);
        screen.right = screen.left + locationButtonPos_.width();
        screen.bottom = screen.top + locationButtonPos_.height();
        
        locationButton_.setBounds(screen);
        locationButton_.draw(canvas);
	} // drawLocationButton

	private void drawMarker(final Canvas canvas, 
							final OpenStreetMapViewProjection projection,
							final OpenStreetMapViewOverlayItem marker)
	{
		if(marker == null)
			return;
		final Point screenPos = new Point();
		projection.toMapPixels(marker.mGeoPoint, screenPos);

		final Drawable thingToDraw = marker.getDrawable();
		final int quarterWidth = thingToDraw.getIntrinsicWidth()/4;
		thingToDraw.setBounds(new Rect(screenPos.x - quarterWidth, 
									   screenPos.y - thingToDraw.getIntrinsicHeight(), 
									   screenPos.x + (quarterWidth*3), 
									   screenPos.y));
		thingToDraw.draw(canvas);

		final Paint paint = new Paint();
		paint.setARGB(0, 100, 100, 255);
		paint.setAntiAlias(true);
		paint.setAlpha(50);
		paint.setStyle(Style.FILL);        
		canvas.drawCircle(screenPos.x, screenPos.y, 30, paint);
	} // drawMarker

	//////////////////////////////////////////////
	@Override
	public boolean onTouchEvent(final MotionEvent event, final OpenStreetMapView mapView)
	{
		if(gestureDetector_.onTouchEvent(event))
			return true;
		return super.onTouchEvent(event, mapView);
	} // onTouchEvent
	
	@Override
	public boolean onLongPress(final MotionEvent event, final OpenStreetMapView mapView) {
		startItem_ = null;
		endItem_ = null;
		if(callback_ != null)
	    	callback_.onClearRoute();
		
		return super.onLongPress(event, mapView);
	} // onLongPress
	
    public boolean onSingleTapConfirmed(final MotionEvent event) {
    	return tapLocation(event) || tapMarker(event);
    } // onSingleTapUp
    
    private boolean tapMarker(final MotionEvent event)
    {
    	if(startItem_ != null && endItem_ != null)
    		return false;
    	
    	final GeoPoint p = mapView_.getProjection().fromPixels((int)event.getX(), (int)event.getY());
    	if(startItem_ == null)
    		setStart(p);
    	else {
    		setEnd(p);
    		if(callback_ != null)
    			callback_.onRouteNow(startItem_.getPoint(), 
    								 endItem_.getPoint());
    	}
    	mapView_.invalidate();
    	return true;
    } // tapMarker
	
	private boolean tapLocation(final MotionEvent event)
	{
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		if(!locationButtonPos_.contains(x, y))
			return false;

		if(!isMyLocationEnabled()) 
		{
			enableMyLocation();
			followLocation(true);
			final Location lastFix = getLastFix();
			if (lastFix != null)
				mapView_.getController().setCenter(new GeoPoint(lastFix));
		}
		else
		{
			followLocation(false);
			disableMyLocation();
		} // if ...
		
		mapView_.invalidate();

		return true;
	} // onSingleTapUp

	////////////////////////////////////
	static private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener
	{
		final private LocationOverlay owner_;
		SingleTapDetector(final LocationOverlay owner) { owner_ = owner; }
		
		@Override
		public boolean onSingleTapConfirmed(final MotionEvent event)
		{
			return owner_.onSingleTapConfirmed(event);
		} // onSingleTapConfirmed
	} // class SingleTapDetector
} // LocationOverlay
