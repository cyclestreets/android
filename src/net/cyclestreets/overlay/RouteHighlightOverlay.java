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
import android.view.MotionEvent;

public class RouteHighlightOverlay extends PathOverlay 
								   implements SingleTapListener
{
	static public int HIGHLIGHT_COLOUR = 0x8000ff00;

	private final MapView mapView_;

	private Segment current_;

	private final OverlayButton prevButton_;
	private final OverlayButton nextButton_;	

	public RouteHighlightOverlay(final Context context, final MapView map, final ResourceProxy resProxy)
	{
		super(HIGHLIGHT_COLOUR, resProxy);
		mPaint.setStrokeWidth(6.0f);

		mapView_ = map;
		current_ = null;

		final Resources res = context.getResources();
		final int offset = (int)(8.0 * res.getDisplayMetrics().density);		
		final float radius = offset / 2.0f;

        prevButton_ = new OverlayButton(res.getDrawable(android.R.drawable.btn_minus),
        		offset,
				offset,
				radius);
        prevButton_.bottomAlign();
		nextButton_ = new OverlayButton(res.getDrawable(android.R.drawable.btn_plus),
				prevButton_.right() + offset,
				offset,
				radius);
        nextButton_.bottomAlign();
	} // MapActivityPathOverlay
	
	@Override
	public void onDraw(final Canvas canvas, final MapView mapView)
	{
		if(current_ != Route.activeSegment())
			refresh();
		super.onDraw(canvas, mapView);
	} // onDraw
	
	@Override
	public void onDrawFinished(final Canvas canvas, final MapView mapView)
	{
		if(mapView.isAnimating())
			return;
		
		if(!Route.available())
			return;
		
		prevButton_.enable(!Route.atStart());
		prevButton_.draw(canvas);
		nextButton_.enable(!Route.atEnd());
		nextButton_.draw(canvas);
	} // onDrawFinished

	private void refresh()
	{
		clearPath();
		current_ = Route.activeSegment();
		if(current_ == null)
			return;
		
		for(final Iterator<GeoPoint> points = current_.points(); points.hasNext(); )
			addPoint(points.next());
		mapView_.getController().animateTo(current_.start());
	} // refresh

	//////////////////////////////////////////////
	@Override
    public boolean onSingleTap(final MotionEvent event) 
	{
    	return tapPrevNext(event);
    } // onSingleTapUp

    private boolean tapPrevNext(final MotionEvent event)
	{
		if(!Route.available())
			return false;
		
		if(prevButton_.hit(event))
		{
			Route.regressActiveSegment();
			mapView_.invalidate();
			return true;
		} // if ...
		if(nextButton_.hit(event))
		{
			Route.advanceActiveSegment();
			mapView_.invalidate();
			return true;
		} // if ...
		
		return false;
	} // tapPrevNext
} // RouteHighlightOverlay
