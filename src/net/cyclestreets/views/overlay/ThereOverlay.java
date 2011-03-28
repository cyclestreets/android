package net.cyclestreets.views.overlay;

import net.cyclestreets.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

public class ThereOverlay extends Overlay 
						  implements TapListener 
{
	private final MapView mapView_;
	
	private final Drawable thereMarker_;
	private GeoPoint there_ = null;
	
	public ThereOverlay(final Context context,
				        final MapView mapView)
	{
		super(context);
		mapView_ = mapView;
		
		final Resources res = context.getResources();
		thereMarker_  = res.getDrawable(R.drawable.x_marks_spot);

	} // ThereOverlay
	
	public GeoPoint there() { return there_; }
	public void noOverThere(final GeoPoint there)
	{
		there_ = there;
		mapView_.invalidate();		
	} // noOverThere
	
	@Override
	protected void onDraw(final Canvas canvas, final MapView mapView) 
	{
	} // onDraw

	@Override
	protected void onDrawFinished(final Canvas canvas, final MapView mapView) 
	{
		if(there_ == null)
			return;
		
		final Point screenPos = new Point();
        final Projection projection = mapView.getProjection();
		projection.toMapPixels(there_, screenPos);

		final int halfWidth = thereMarker_.getIntrinsicWidth()/2;
		final int halfHeight = thereMarker_.getIntrinsicHeight()/2;
		thereMarker_.setBounds(new Rect(screenPos.x - halfWidth, 
									    screenPos.y - halfHeight, 
									    screenPos.x + halfWidth, 
									    screenPos.y + halfHeight));
		thereMarker_.draw(canvas);
	} // onDrawFinished

	@Override
	public void drawButtons(Canvas canvas, MapView mapView) 
	{
	} // drawButtons

	@Override
	public boolean onDoubleTap(MotionEvent event) 
	{
		return false;
	} // onDoubleTap

	@Override
	public boolean onSingleTap(final MotionEvent event) 
	{
    	final GeoPoint p = mapView_.getProjection().fromPixels((int)event.getX(), (int)event.getY());
    	noOverThere(p);
    	return true;
	} // onSingleTap
} // class ThereOverlay
