package net.cyclestreets.overlay;

import java.util.Iterator;

import net.cyclestreets.R;
import net.cyclestreets.planned.Route;
import net.cyclestreets.planned.Segment;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class RouteHighlightOverlay extends PathOverlay 
								   implements TapListener
{
	static public int HIGHLIGHT_COLOUR = 0x8000ff00;

	private final MapView mapView_;

	private Segment current_;

	private final OverlayButton prevButton_;
	private final OverlayButton nextButton_;	

	private final int offset_;
	private final float radius_;
	
	private Paint textBrush_;

	public RouteHighlightOverlay(final Context context, final MapView map)
	{
		super(HIGHLIGHT_COLOUR, context);
		mPaint.setStrokeWidth(6.0f);

		mapView_ = map;
		current_ = null;

		offset_ = OverlayHelper.offset(context);
		radius_ = OverlayHelper.cornerRadius(context);

		final Resources res = context.getResources();
        prevButton_ = new OverlayButton(res.getDrawable(R.drawable.btn_previous),
        		offset_,
				offset_,
				radius_);
        prevButton_.bottomAlign();
		nextButton_ = new OverlayButton(res.getDrawable(R.drawable.btn_next),
				prevButton_.right() + offset_,
				offset_,
				radius_);
        nextButton_.bottomAlign();

		textBrush_ = Brush.createTextBrush(offset_);
		textBrush_.setTextAlign(Align.LEFT);

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
	} // onDrawFinished
	
	public void drawButtons(final Canvas canvas, final MapView mapView)
	{
		if(mapView.isAnimating())
			return;
		
		if(!Route.available())
			return;
		
		drawSegmentInfo(canvas);

		prevButton_.enable(!Route.atStart());
		prevButton_.draw(canvas);
		nextButton_.enable(!Route.atEnd());
		nextButton_.draw(canvas);
	} // drawButtons
	
	private void drawSegmentInfo(final Canvas canvas)
	{
		final Segment seg = Route.activeSegment();
		if(seg == null)
			return;
		
		final Rect box = canvas.getClipBounds();
		box.left += prevButton_.right() + offset_; 
		box.top += offset_;
		box.right -= offset_;
		box.bottom = box.top + prevButton_.height();
        
        final Rect textBox = new Rect(box);
        textBox.left += offset_;
        textBox.right -= offset_;
		int bottom = Draw.measureTextInRect(canvas, textBrush_, textBox, seg.toString());
		
		if(bottom >= box.bottom)
			box.bottom = bottom + offset_;
		
		OverlayHelper.drawRoundRect(canvas, box, radius_, Brush.Grey);

		Draw.drawTextInRect(canvas, textBrush_, textBox, seg.toString());
	} // drawSegmentInfo

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
	
	public boolean onDoubleTap(final MotionEvent event)
	{
		return doubleTapPrevNext(event);
	} // onDoubleTap

    private boolean tapPrevNext(final MotionEvent event)
	{
		if(!Route.available())
			return false;
		
		if(!prevButton_.hit(event) && !nextButton_.hit(event))
			return false;
		
		if(prevButton_.hit(event))
			Route.regressActiveSegment();

		if(nextButton_.hit(event))
			Route.advanceActiveSegment();

		mapView_.invalidate();
		return true;
	} // tapPrevNext
    
    private boolean doubleTapPrevNext(final MotionEvent event)
    {
    	if(!Route.available())
    		return false;
    	
		if(!prevButton_.hit(event) && !nextButton_.hit(event))
			return false;

		if(prevButton_.hit(event))
    		while(!Route.atStart())
    			Route.regressActiveSegment();

		if(nextButton_.hit(event))
			while(!Route.atEnd())
				Route.advanceActiveSegment();
		
		mapView_.invalidate();
		return true;
    } // doubleTapPrevNext
} // RouteHighlightOverlay
