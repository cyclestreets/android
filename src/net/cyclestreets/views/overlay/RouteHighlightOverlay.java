package net.cyclestreets.views.overlay;

import net.cyclestreets.R;
import net.cyclestreets.planned.Route;
import net.cyclestreets.api.Segment;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class RouteHighlightOverlay extends Overlay 
		                   implements ButtonTapListener
{
  private final CycleMapView mapView_;
  
  private Segment current_;
  
  private final OverlayButton prevButton_;
  private final OverlayButton nextButton_;	

  private final int offset_;
  private final float radius_;
  
  private Paint textBrush_;

  public RouteHighlightOverlay(final Context context, final CycleMapView map)
  {
    super(context);
    
    mapView_ = map;
    current_ = null;
    
    offset_ = DrawingHelper.offset(context);
    radius_ = DrawingHelper.cornerRadius(context);
    
    final Resources res = context.getResources();
    prevButton_ = new OverlayButton(res.getDrawable(R.drawable.btn_previous),
                                    offset_,
                                    offset_*2,
                                    radius_);
    prevButton_.bottomAlign();
    nextButton_ = new OverlayButton(res.getDrawable(R.drawable.btn_next),
                                    prevButton_.right() + offset_,
                                    offset_*2,
                                    radius_);
    nextButton_.bottomAlign();

    textBrush_ = Brush.createTextBrush(offset_);
    textBrush_.setTextAlign(Align.LEFT);
  } // MapActivityPathOverlay
	
  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow)
  {
    if(current_ == Route.activeSegment())
      return;

    current_ = Route.activeSegment();
    if(current_ == null)
      return;
                  
    mapView_.getController().animateTo(current_.start());
  } // onDraw
	
  @Override
  public void drawButtons(final Canvas canvas, final MapView mapView)
  {
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
    
    if(!DrawingHelper.drawRoundRect(canvas, box, radius_, Brush.Grey))
      return;
    
    Draw.drawTextInRect(canvas, textBrush_, textBox, seg.toString());
  } // drawSegmentInfo
  
  //////////////////////////////////////////////
  @Override
  public boolean onButtonTap(final MotionEvent event) 
  {
    return tapPrevNext(event);
  } // onSingleTapUp
	
  public boolean onButtonDoubleTap(final MotionEvent event)
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
