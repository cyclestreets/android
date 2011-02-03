package net.cyclestreets.overlay;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class ZoomButtonsOverlay extends Overlay 
							    implements SingleTapListener
{
	private final MapView mapView_;
	private final OverlayButton zoomIn_;
	private final OverlayButton zoomOut_;
	
	public ZoomButtonsOverlay(final Context context, 
							  final MapView mapView)
	{
		super(context);
		
		mapView_ = mapView;

		final Resources res = context.getResources();
		final int offset = (int)(8.0 * context.getResources().getDisplayMetrics().density);		
		final float radius = offset / 2.0f;
		
		zoomIn_ = new OverlayButton(res.getDrawable(android.R.drawable.btn_plus),
				offset,
				offset,
				radius);
		zoomIn_.rightAlign().bottomAlign();

		zoomOut_ = new OverlayButton(res.getDrawable(android.R.drawable.btn_minus),
				 zoomIn_.right() + offset,
				 offset,
				 radius);
		zoomOut_.rightAlign().bottomAlign();
	} // ZoomButtonsOverlay
	
	@Override
	protected void onDraw(final Canvas canvas, final MapView mapView) 
	{
	} // onDraw

	@Override
	protected void onDrawFinished(final Canvas canvas, MapView mapView) 
	{
		if(mapView.isAnimating())
			return;
		
		drawButtons(canvas);
	} // onDrawFinished
	
	private void drawButtons(final Canvas canvas)
	{
		zoomIn_.enable(mapView_.canZoomIn());
		zoomIn_.draw(canvas);
		zoomOut_.enable(mapView_.canZoomOut());
		zoomOut_.draw(canvas);
	} // drawButtons
	
	//////////////////////////////////////////////
	@Override
    public boolean onSingleTap(final MotionEvent event) 
	{
    	return tapZoom(event);
    } // onSingleTapUp

    private boolean tapZoom(final MotionEvent event)
	{
		if(zoomIn_.hit(event))
		{
			mapView_.getController().zoomIn();
			return true;
		} // if ...
		if(zoomOut_.hit(event))
		{
			mapView_.getController().zoomOut();
			return true;
		} // if ...
		
		return false;
	} // tapPrevNext
} // class ZoomButtonsOverlay
