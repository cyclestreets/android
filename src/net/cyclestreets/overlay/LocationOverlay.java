package net.cyclestreets.overlay;

import net.cyclestreets.R;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
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

	private final Drawable greenWisp_;
	private final Drawable redWisp_;

	private final int offset_;

	private final Drawable locationButton_;
	private final Rect locationButtonPos_;
	
	private final Drawable stepBackButton_;
	private final Rect stepBackButtonPos_;
	
	private final Callback callback_;
	
	private final GestureDetector gestureDetector_;
	private final MapView mapView_;
	
	private OverlayItem startItem_;
	private OverlayItem endItem_;

	private Paint greyBrush_;
	private Paint whiteBrush_;
	private Paint textBrush_;

	private TapToRoute tapState_;
	
	public LocationOverlay(final Context context, 
						   final MapView mapView,
						   final Callback callback,
						   final ResourceProxy resProxy) 
	{
		super(context, mapView, resProxy);
		
		mapView_ = mapView;
		callback_ = callback;
		
		final Resources res = context.getResources();
		greenWisp_ = res.getDrawable(R.drawable.green_wisp_36x30);
		redWisp_ = res.getDrawable(R.drawable.red_wisp_36x30);

		offset_ = (int)(8.0 * context.getResources().getDisplayMetrics().density);		

		locationButton_ = res.getDrawable(android.R.drawable.ic_menu_mylocation);
        locationButtonPos_ = new Rect(offset_, offset_, offset_ + locationButton_.getIntrinsicWidth(), offset_ + locationButton_.getIntrinsicHeight());
        
        stepBackButton_ = res.getDrawable(android.R.drawable.ic_menu_revert);
        stepBackButtonPos_ = new Rect(locationButtonPos_);
        stepBackButtonPos_.offset(locationButtonPos_.width() + offset_, 0);

		greyBrush_ = createGreyBrush();
		whiteBrush_ = createWhiteBrush();
		textBrush_ = createTextBrush(offset_);
        
		final SingleTapDetector tapDetector = new SingleTapDetector(this);
		gestureDetector_ = new GestureDetector(context, tapDetector);
		gestureDetector_.setOnDoubleTapListener(tapDetector);
		
		startItem_ = null;
		endItem_ = null;
		
		tapState_ = TapToRoute.start();
	} // LocationOverlay
	
	public void enableLocation(final boolean enable)
	{
		if(enable)
			enableMyLocation();
		else
			disableMyLocation();
	} // enableLocation
	
	public void setRoute(final GeoPoint start, final GeoPoint end, final boolean complete)
	{
		setStart(start);
		setEnd(end);

		tapState_ = tapState_.reset();
		if(start == null)
			return;
		tapState_ = tapState_.next();
		if(end == null)
			return;
		tapState_ = tapState_.next();
		if(!complete)
			return;
		tapState_ = tapState_.next();
	} // setRoute
	
	public void resetRoute()
	{
		setStart(null);
		setEnd(null);
		tapState_ = tapState_.reset();
	} // resetRoute
	
	public GeoPoint getStart() { return getMarkerPoint(startItem_); }
	public GeoPoint getEnd() { return getMarkerPoint(endItem_); }
	private GeoPoint getMarkerPoint(final OverlayItem marker)
	{
		return marker != null ? marker.getPoint() : null;
	} // getMarkerPoint
	
	private void setStart(final GeoPoint point)
	{
		startItem_ = addMarker(point, "start", greenWisp_);
	} // setStart
	
	private void setEnd(final GeoPoint point)
	{
		endItem_ = addMarker(point, "finish", redWisp_);
	} // setEnd
	
	private OverlayItem addMarker(final GeoPoint point, final String label, final Drawable icon)
	{
		if(point == null)
			return null;
		final OverlayItem marker = new OverlayItem(label, label, point);
		marker.setMarker(icon);
		marker.setMarkerHotspot(new Point(0,30));
		return marker;
	} // addMarker

	////////////////////////////////////////////
	@Override
	public void onDraw(final Canvas canvas, final MapView mapView) {
		// I'm not thrilled about this but there isn't any other way (short of killing
		// and recreating the overlay) of turning off the little here-you-are man
		if(!isMyLocationEnabled())
			return;
		
		super.onDraw(canvas, mapView);
	} // onDraw
	
	@Override
	protected void onDrawFinished(final Canvas canvas, final MapView mapView) 
	{
        final Projection projection = mapView.getProjection();
        drawMarker(canvas, projection, startItem_);
        drawMarker(canvas, projection, endItem_);

		drawButtons(canvas);
		drawTapState(canvas);
	} // onDrawFinished
	
	private void drawButtons(final Canvas canvas)
	{
		drawButton(canvas, locationButton_, locationButtonPos_);
		drawButton(canvas, stepBackButton_, stepBackButtonPos_);
	} // drawLocationButton
	
	private void drawButton(final Canvas canvas, final Drawable button, final Rect pos)
	{
        final Rect screen = canvas.getClipBounds();
        screen.offset(pos.left, pos.top);
        screen.right = screen.left + pos.width();
        screen.bottom = screen.top + pos.height();
        
        drawRoundRect(canvas, screen, whiteBrush_);
        button.setBounds(screen);
        button.draw(canvas);
	} // drawButton

	private void drawTapState(final Canvas canvas)
	{
		final String msg = tapState_.toString();
		if(msg.length() == 0)
			return;
		
		
		final Rect screen = canvas.getClipBounds();
		final int halfWidth = screen.width() / 2;
        screen.left += halfWidth; 
        screen.top += offset_;
        screen.right -= offset_;
        screen.bottom = screen.top + locationButton_.getIntrinsicHeight();
		
		drawRoundRect(canvas, screen, greyBrush_);

		final Rect bounds = new Rect();
		textBrush_.getTextBounds(msg, 0, msg.length(), bounds);
		
		canvas.drawText(msg, screen.centerX(), screen.centerY() + bounds.bottom, textBrush_);
	} // drawTapState
	
	private void drawRoundRect(final Canvas canvas, final Rect rect, final Paint brush)
	{
		float radius = offset_/2.0f;
		canvas.drawRoundRect(new RectF(rect), radius, radius, brush);
	} // drawRoundRect

	private void drawMarker(final Canvas canvas, 
							final Projection projection,
							final OverlayItem marker)
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
	} // drawMarker

	//////////////////////////////////////////////
	@Override
	public boolean onTouchEvent(final MotionEvent event, final MapView mapView)
	{
		if(gestureDetector_.onTouchEvent(event))
			return true;
		return super.onTouchEvent(event, mapView);
	} // onTouchEvent
	
    public boolean onSingleTapConfirmed(final MotionEvent event) {
    	return tapLocation(event) ||
    		   tapStepBack(event) || 
    		   tapMarker(event);
    } // onSingleTapUp
    
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
	} // tapLocation
	
	private boolean tapStepBack(final MotionEvent event)
	{
		if(!stepBackButtonPos_.contains((int)event.getX(), (int)event.getY()))
			return false;
		
		switch(tapState_)
		{
    	case WAITING_FOR_START:
    		return true;
    	case WAITING_FOR_END:
    		setStart(null);
    		break;
    	case WAITING_TO_ROUTE:
    		setEnd(null);
    		break;
    	case ALL_DONE:
    		callback_.onClearRoute();
    		break;
    	} // switch ...
		
		mapView_.invalidate();
		tapState_ = tapState_.previous();
		
		return true;
	} // tapStepBack

    private boolean tapMarker(final MotionEvent event)
    {
    	final GeoPoint p = mapView_.getProjection().fromPixels((int)event.getX(), (int)event.getY());

    	switch(tapState_)
    	{
    	case WAITING_FOR_START:
    		setStart(p);
    		mapView_.invalidate();
    		break;
    	case WAITING_FOR_END:
    		setEnd(p);
    		mapView_.invalidate();
    		break;
    	case WAITING_TO_ROUTE:
			callback_.onRouteNow(startItem_.getPoint(), endItem_.getPoint());
    		break;
    	case ALL_DONE:
    		break;
    	} // switch ...

    	tapState_ = tapState_.next();

    	return true;
    } // tapMarker
	
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
	
	private enum TapToRoute 
	{ 
		WAITING_FOR_START, 
		WAITING_FOR_END, 
		WAITING_TO_ROUTE, 
		ALL_DONE;
		
		static public TapToRoute start()
		{
			return WAITING_FOR_START;
		} // start
		
		public TapToRoute reset()
		{
			return WAITING_FOR_START;
		} // reset
		
		public TapToRoute previous() 
		{
			switch(this) {
			case WAITING_FOR_START:
				break;
			case WAITING_FOR_END:
				return WAITING_FOR_START;
			case WAITING_TO_ROUTE:
				return WAITING_FOR_END;
			case ALL_DONE:
				break;
			} // switch
			return WAITING_FOR_START;				
		} // previous()

		public TapToRoute next() 
		{
			switch(this) {
			case WAITING_FOR_START:
				return WAITING_FOR_END;
			case WAITING_FOR_END:
				return WAITING_TO_ROUTE;
			case WAITING_TO_ROUTE:
				return ALL_DONE;
			case ALL_DONE:
				break;
			} // switch
			return ALL_DONE;				
		} // next()
		
		public String toString()
		{
			switch(this) {
			case WAITING_FOR_START:
				return "Tap to set Start";
			case WAITING_FOR_END:
				return "Tap to set End";
			case WAITING_TO_ROUTE:
				return "Tap to plan route";
			case ALL_DONE:
				break;
			} // switch
			return "";				
		} // toString
	}; // enum TapToRoute
	
	static private Paint createGreyBrush()
	{
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.DEFAULT);

		paint.setARGB(255, 127, 127, 127);
		
		return paint;
	} // createBgBrush

	static private Paint createWhiteBrush()
	{
		final Paint paint = createGreyBrush();

		paint.setARGB(255, 255, 255, 255);
		
		return paint;
	} // createTextBrush

	static private Paint createTextBrush(final int offset)
	{
		final Paint paint = createGreyBrush();

		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.DEFAULT);
		paint.setTextSize(offset * 2);
		paint.setARGB(255, 255, 255, 255);
		
		return paint;
	} // createTextBrush
} // LocationOverlay
