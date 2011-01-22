package net.cyclestreets.overlay;

import net.cyclestreets.R;

import org.andnav.osm.DefaultResourceProxyImpl;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;


public class RouteOverlay extends OpenStreetMapViewOverlay {
	public interface Callback {
		void onRouteNow(final GeoPoint from, final GeoPoint to);
		void onClearRoute();
	} // Callback

	private final Callback callback_;
	private final Resources res_;
	private final GestureDetector gestureDetector_;
	private final OpenStreetMapView mapView_;
	private OpenStreetMapViewOverlayItem startItem_;
	private OpenStreetMapViewOverlayItem endItem_;
    
	public RouteOverlay(final Context context,
						final OpenStreetMapView mapView,
				        final Callback callback) {
		super(new DefaultResourceProxyImpl(context));
		mapView_ = mapView;
		callback_ = callback;
		res_ = context.getResources();
		
		final SingleTapDetector tapDetector = new SingleTapDetector(this);
		gestureDetector_ = new GestureDetector(context, tapDetector);
		gestureDetector_.setOnDoubleTapListener(tapDetector);
		
		startItem_ = null;
		endItem_ = null;
	} // RouteOverlay
		
	public void setRoute(final GeoPoint start, final GeoPoint end)
	{
		setStart(start);
		setEnd(end);
	} // setRoute
	
	public void setStart(final GeoPoint point)
	{
		startItem_ = addMarker(point, "start", R.drawable.green_wisp_36x30);
	} // setStart
	
	public void setEnd(final GeoPoint point)
	{
		endItem_ = addMarker(point, "finish", R.drawable.red_wisp_36x30);
	} // setEnd
	
	private OpenStreetMapViewOverlayItem addMarker(final GeoPoint point, final String label, final int iconId)
	{
		final OpenStreetMapViewOverlayItem marker = new OpenStreetMapViewOverlayItem(label, label, point);
		marker.setMarker(res_.getDrawable(iconId));
		marker.setMarkerHotspot(new Point(0,30));
		return marker;
	} // addMarker
	
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
    } // onSingleTapUp

	@Override
	protected void onDraw(final Canvas canvas, final OpenStreetMapView mapView) {
        final OpenStreetMapViewProjection projection = mapView.getProjection();

        drawMarker(canvas, projection, startItem_);
        drawMarker(canvas, projection, endItem_);
	} // onDraw
	
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
        
        Paint paint = new Paint();
        paint.setARGB(0, 100, 100, 255);
        paint.setAntiAlias(true);
        paint.setAlpha(50);
        paint.setStyle(Style.FILL);        
        canvas.drawCircle(screenPos.x, screenPos.y, 30, paint);
	} // drawMarker

	@Override
	protected void onDrawFinished(final Canvas canvas, final OpenStreetMapView mapView) {
	} // onDrawFinished
	
	static private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener
	{
		final private RouteOverlay owner_;
		SingleTapDetector(RouteOverlay owner) { owner_ = owner; }
		
		@Override
		public boolean onSingleTapConfirmed(final MotionEvent event)
		{
			return owner_.onSingleTapConfirmed(event);
		} // onSingleTapConfirmed
	} // class SingleTapDetector
} // class RouteOverlay
