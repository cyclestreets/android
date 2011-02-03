package net.cyclestreets.overlay;

import java.util.Iterator;

import net.cyclestreets.planned.Route;
import net.cyclestreets.planned.Segment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class RouteHighlightOverlay extends PathOverlay 
{
	static public int HIGHLIGHT_COLOUR = 0x8000ff00;

	private final MapView mapView_;
	private final GestureDetector gestureDetector_;

	private Segment current_;

	private final int offset_;
	private final float radius_;

	private final OverlayButton prevButton_;
	private final OverlayButton nextButton_;	

	public RouteHighlightOverlay(final Context context, final MapView map, final ResourceProxy resProxy)
	{
		super(HIGHLIGHT_COLOUR, resProxy);
		mPaint.setStrokeWidth(6.0f);

		mapView_ = map;
		current_ = null;

		final Resources res = context.getResources();
		offset_ = (int)(8.0 * res.getDisplayMetrics().density);		
		radius_ = offset_ / 2.0f;

		nextButton_ = new OverlayButton(res.getDrawable(android.R.drawable.ic_media_next),
				offset_,
				offset_,
				radius_);
        nextButton_.rightAlign();
        prevButton_ = new OverlayButton(res.getDrawable(android.R.drawable.ic_media_previous),
        		nextButton_.right() + offset_,
				offset_,
				radius_);
        prevButton_.rightAlign();
	
		final SingleTapDetector tapDetector = new SingleTapDetector(this);
		gestureDetector_ = new GestureDetector(context, tapDetector);
		gestureDetector_.setOnDoubleTapListener(tapDetector);
	} // MapActivityPathOverlay
	
	@Override
	public void onDraw(final Canvas canvas, final MapView mapView)
	{
		if(current_ != Route.activeSegment())
			refresh(false);
		super.onDraw(canvas, mapView);
	} // onDraw
	
	@Override
	public void onDrawFinished(final Canvas canvas, final MapView mapView)
	{
		if(!Route.available())
			return;
		prevButton_.enable(!Route.atStart());
		prevButton_.draw(canvas);
		nextButton_.enable(!Route.atEnd());
		nextButton_.draw(canvas);
	} // onDrawFinished

	private void refresh(final boolean centre)
	{
		clearPath();
		current_ = Route.activeSegment();
		if(current_ == null)
			return;
		
		for(final Iterator<GeoPoint> points = current_.points(); points.hasNext(); )
			addPoint(points.next());
		
		if(!centre)
			return;
		
		mapView_.getController().animateTo(current_.start());
	} // refresh

	//////////////////////////////////////////////
	//////////////////////////////////////////////
	@Override
	public boolean onTouchEvent(final MotionEvent event, final MapView mapView)
	{
		if(gestureDetector_.onTouchEvent(event))
			return true;
		return super.onTouchEvent(event, mapView);
	} // onTouchEvent
	
    public boolean onSingleTapConfirmed(final MotionEvent event) {
    	return tapPrevNext(event);
    } // onSingleTapUp

    private boolean tapPrevNext(final MotionEvent event)
	{
		if(!Route.available())
			return false;
		
		if(prevButton_.hit(event))
		{
			Route.regressActiveSegment();
			refresh(true);
			mapView_.invalidate();
			return true;
		} // if ...
		if(nextButton_.hit(event))
		{
			Route.advanceActiveSegment();
			refresh(true);
			mapView_.invalidate();
			return true;
		} // if ...
		
		return false;
	} // tapPrevNext
    
	////////////////////////////////////
	static private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener
	{
		final private RouteHighlightOverlay owner_;
		SingleTapDetector(final RouteHighlightOverlay owner) { owner_ = owner; }
		
		@Override
		public boolean onSingleTapConfirmed(final MotionEvent event)
		{
			return owner_.onSingleTapConfirmed(event);
		} // onSingleTapConfirmed
	} // class SingleTapDetector

} // RouteHighlightOverlay
