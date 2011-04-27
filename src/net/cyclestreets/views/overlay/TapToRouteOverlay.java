package net.cyclestreets.views.overlay;

import net.cyclestreets.R;
import net.cyclestreets.util.Brush;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

public class TapToRouteOverlay extends Overlay 
							   implements TapListener
{
	public interface Callback {
		void onRouteNow(final GeoPoint from, final GeoPoint to);
		void onClearRoute();
	} // Callback

	private final Drawable greenWisp_;
	private final Drawable redWisp_;
	private final Point screenPos_ = new Point();
	private final Matrix canvasTransform_ = new Matrix();
	private final float[] transformValues_ = new float[9];
	private final Matrix bitmapTransform_ = new Matrix();
	private final Paint bitmapPaint_ = new Paint();

	private final int offset_;
	private final float radius_;

	private final OverlayButton stepBackButton_;
	private final OverlayButton restartButton_;
	
	private final Callback callback_;
	
	private final MapView mapView_;
	
	private OverlayItem startItem_;
	private OverlayItem endItem_;

	private Paint textBrush_;

	private TapToRoute tapState_;
	
	public TapToRouteOverlay(final Context context, 
						   	 final MapView mapView,
						   	 final Callback callback) 
	{
		super(context);
		
		mapView_ = mapView;
		callback_ = callback;
		
		final Resources res = context.getResources();
		greenWisp_ = res.getDrawable(R.drawable.green_wisp_shadow_centred_big);
		redWisp_ = res.getDrawable(R.drawable.red_wisp_shadow_centred_big);

		offset_ = OverlayHelper.offset(context);
		radius_ = OverlayHelper.cornerRadius(context);

        stepBackButton_ = new OverlayButton(res.getDrawable(R.drawable.ic_menu_revert),
        									offset_,
        									offset_,
        									radius_);
        stepBackButton_.bottomAlign();
        restartButton_ = new OverlayButton(res.getDrawable(R.drawable.ic_menu_rotate),
        								   0, 
        								   offset_,
        								   radius_);
        restartButton_.centreAlign();
        restartButton_.bottomAlign();
        
		textBrush_ = Brush.createTextBrush(offset_);
        
		startItem_ = null;
		endItem_ = null;
		
		tapState_ = TapToRoute.start();
	} // LocationOverlay
	
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
		marker.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		return marker;
	} // addMarker

	////////////////////////////////////////////
	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
	{
        final Projection projection = mapView.getProjection();
        drawMarker(canvas, projection, startItem_);
        drawMarker(canvas, projection, endItem_);
	} // draw
	
	public void drawButtons(final Canvas canvas, final MapView mapView)
	{
		drawButtons(canvas);
		drawTapState(canvas);
	} // onDrawFinished
	
	private void drawButtons(final Canvas canvas)
	{
		stepBackButton_.enable(tapState_ == TapToRoute.WAITING_FOR_END || 
							   tapState_ == TapToRoute.WAITING_TO_ROUTE);
		if(tapState_ != TapToRoute.ALL_DONE)
			stepBackButton_.draw(canvas);

		restartButton_.enable(tapState_ == TapToRoute.ALL_DONE);
		if(tapState_ == TapToRoute.ALL_DONE)
			restartButton_.draw(canvas);
	} // drawLocationButton

	private void drawTapState(final Canvas canvas)
	{
		final String msg = tapState_.toString();
		if(msg.length() == 0)
			return;
				
		final Rect screen = canvas.getClipBounds();
        screen.left += stepBackButton_.right() + offset_; 
        screen.top += offset_;
        screen.right -= offset_;
        screen.bottom = screen.top + stepBackButton_.height();
		
		if(!OverlayHelper.drawRoundRect(canvas, screen, radius_, Brush.Grey))
			return;

		final Rect bounds = new Rect();
		textBrush_.getTextBounds(msg, 0, msg.length(), bounds);
		
		canvas.drawText(msg, screen.centerX(), screen.centerY() + bounds.bottom, textBrush_);
	} // drawTapState
	
	private void drawMarker(final Canvas canvas, 
							final Projection projection,
							final OverlayItem marker)
	{
		if(marker == null)
			return;

		projection.toMapPixels(marker.mGeoPoint, screenPos_);
		
		canvas.getMatrix(canvasTransform_);
		canvasTransform_.getValues(transformValues_);
		
		final BitmapDrawable thingToDraw = (BitmapDrawable)marker.getDrawable();
		final int halfWidth = thingToDraw.getIntrinsicWidth()/2;
		final int halfHeight = thingToDraw.getIntrinsicHeight()/2;
		bitmapTransform_.setTranslate(-halfWidth, -halfHeight);
		bitmapTransform_.postScale(1/transformValues_[Matrix.MSCALE_X], 1/transformValues_[Matrix.MSCALE_Y]);
		bitmapTransform_.postTranslate(screenPos_.x, screenPos_.y);
		canvas.drawBitmap(thingToDraw.getBitmap(), bitmapTransform_, bitmapPaint_);
	} // drawMarker

	//////////////////////////////////////////////
	@Override
    public boolean onSingleTap(final MotionEvent event) 
	{
    	return tapStepBack(event) || 
    		   tapRestart(event) ||
    		   tapMarker(event);
    } // onSingleTapUp
	
	@Override
	public boolean onDoubleTap(final MotionEvent event)
	{
		return stepBackButton_.hit(event);
	} // onDoubleTap
    
    public boolean onBackButton()
    {
    	return stepBack(false);
    } // onBackButton
    
	private boolean tapStepBack(final MotionEvent event)
	{
		if(!stepBackButton_.hit(event))
			return false;
		if(!stepBackButton_.enabled())
			return true;
		
		return stepBack(true);
	} // tapStepBack
	
	private boolean tapRestart(final MotionEvent event)
	{
		if(!restartButton_.enabled() || !restartButton_.hit(event))
			return false;

		return stepBack(true);
	} // tapRestart
	
	private boolean stepBack(final boolean tap)
	{
		if(!tap && tapState_.isAtEnd())
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
		
		public boolean isAtEnd()
		{
			return (this == previous()) ||
				   (this == next());
		} // isAtEnd
		
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
				return "Tap to set Finish";
			case WAITING_TO_ROUTE:
				return "Tap to Route";
			case ALL_DONE:
				break;
			} // switch
			return "";				
		} // toString
	}; // enum TapToRoute
	
} // LocationOverlay
