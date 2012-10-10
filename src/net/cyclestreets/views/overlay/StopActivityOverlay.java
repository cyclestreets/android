package net.cyclestreets.views.overlay;

import net.cyclestreets.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class StopActivityOverlay extends Overlay implements ButtonTapListener
{
	private final int offset_;
	private final float radius_;

	private final Activity activity_;
	private final OverlayButton stopButton_;	
	
	public StopActivityOverlay(final Activity context) 
	{
		super(context);
		
		activity_ = context;
		
		offset_ = DrawingHelper.offset(context);
		radius_ = DrawingHelper.cornerRadius(context);

		final Resources res = context.getResources();
		stopButton_ = new OverlayButton(res.getDrawable(R.drawable.btn_stop),
		                                offset_,
		                                offset_,
		                                radius_);		
	} // LocationOverlay
	
	////////////////////////////////////////////
  @Override
  protected void draw(Canvas arg0, MapView arg1, boolean arg2)
  {
  } // draw
  
	@Override
	public void drawButtons(final Canvas canvas, final MapView mapView)
	{
		stopButton_.draw(canvas);
	} // drawLocationButton

  //////////////////////////////////////////////
	@Override
	public boolean onButtonTap(final MotionEvent event) 
	{
	  if(!stopButton_.hit(event))
	    return false;
	  
	  activity_.finish();
	  return true;
	} // onSingleTapUp
	
	@Override
	public boolean onButtonDoubleTap(final MotionEvent event)
	{
		return stopButton_.hit(event);
	} // onDoubleTap
} // LocationOverlay
