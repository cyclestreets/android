package net.cyclestreets.views.overlay;

import java.util.Iterator;

import net.cyclestreets.util.Brush;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;

public class ControllerOverlay extends Overlay implements OnDoubleTapListener, 
														  OnGestureListener 
{
	private final GestureDetector gestureDetector_;
	private final MapView mapView_;
	private final Paint textBrush_;
	private boolean isDragging_;
	
	public ControllerOverlay(final Context context, final MapView mapView)
	{
		super(context);
		
		mapView_ = mapView;
		isDragging_ = false;
		textBrush_ = Brush.createTextBrush(OverlayHelper.offset(context)/2);
		textBrush_.setColor(Color.BLACK);
		
		gestureDetector_ = new GestureDetector(context, this);
		gestureDetector_.setOnDoubleTapListener(this);
	} // SingleTapOverlay
	
	public void onPause(final SharedPreferences.Editor prefEditor)
	{
	  for(final Iterator<PauseResumeListener> overlays = pauseResumeOverlays(); overlays.hasNext(); )
	    overlays.next().onPause(prefEditor);
	} // onPause
	
	public void onResume(final SharedPreferences prefs)
	{
    for(final Iterator<PauseResumeListener> overlays = pauseResumeOverlays(); overlays.hasNext(); )
      overlays.next().onResume(prefs);
	} // onResume

	public boolean onCreateOptionsMenu(final Menu menu)
	{
		boolean ret = false;
		
		for(final Iterator<MenuListener> overlays = menuOverlays(); overlays.hasNext(); )
			ret |= overlays.next().onCreateOptionsMenu(menu);
		
		return ret;
	} // onCreateOptionsMenu
	
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		boolean ret = false;
		
		for(final Iterator<DynamicMenuListener> overlays = dynamicMenuOverlays(); overlays.hasNext(); )
			ret |= overlays.next().onPrepareOptionsMenu(menu);
		
		return ret;
	} // onPrepareOptionsMenu
	
	public void onCreateContextMenu(final ContextMenu menu)
	{
		for(final Iterator<ContextMenuListener> overlays = contextMenuOverlays(); overlays.hasNext(); )
			overlays.next().onCreateContextMenu(menu);
	} // onPrepareContextMenu
	
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
		for(final Iterator<MenuListener> overlays = menuOverlays(); overlays.hasNext(); )
			if(overlays.next().onMenuItemSelected(featureId, item))
				return true;
		return false;		
	} // onMenuItemSelected
	
	@Override
	public boolean onTouchEvent(final MotionEvent event, final MapView mapView)
	{
		if(gestureDetector_.onTouchEvent(event))
			return true;
		return super.onTouchEvent(event, mapView);
	} // onTouchEvent

	@Override
	public boolean onSingleTapConfirmed(final MotionEvent e) 
	{
    for(final Iterator<ButtonTapListener> overlays = buttonTapOverlays(); overlays.hasNext(); )
      if(overlays.next().onButtonTap(e))
        return true;
		for(final Iterator<TapListener> overlays = tapOverlays(); overlays.hasNext(); )
			if(overlays.next().onSingleTap(e))
				return true;
		return false;
	} // onSingleTapConfirmed

	@Override
	public boolean onDoubleTap(final MotionEvent e) 
	{ 
    for(final Iterator<ButtonTapListener> overlays = buttonTapOverlays(); overlays.hasNext(); )
      if(overlays.next().onButtonDoubleTap(e))
        return true;
		for(final Iterator<TapListener> overlays = tapOverlays(); overlays.hasNext(); )
			if(overlays.next().onDoubleTap(e))
				return true;
		return false; 
	} // onDoubleTap
	
	@Override
	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
	{	
		isDragging_ = OverlayHelper.isDragging(canvas);
		for(final Iterator<ButtonTapListener> overlays = buttonTapOverlays(); overlays.hasNext(); )
			overlays.next().drawButtons(canvas, mapView);
		
		if(!(mapView instanceof CycleMapView))
			return;
		
		final Rect screen = canvas.getClipBounds();
    canvas.drawText(CycleMapView.mapAttribution(), 
             				screen.centerX(), 
        			    	screen.bottom-(textBrush_.descent()+2), 
        				    textBrush_);
	} // draw
	
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) { return false; }
	@Override
	public boolean onDown(MotionEvent e) { return false; }
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
	@Override
	public void onLongPress(final MotionEvent e) 
	{ 
		if(isDragging_)
			return;
		mapView_.showContextMenu();
	} // onLongPress
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
	@Override
	public void onShowPress(MotionEvent e) { }
	@Override
	public boolean onSingleTapUp(MotionEvent e) { return false; }
	
	//////////////////////////////////////////////////////	
	private Iterator<TapListener> tapOverlays()
	{
		return new OverlayIterator<TapListener>(mapView_, TapListener.class);
	} // tapOverlays
  private Iterator<ButtonTapListener> buttonTapOverlays()
  {
    return new OverlayIterator<ButtonTapListener>(mapView_, ButtonTapListener.class);
  } // buttonTapOverlays
	private Iterator<MenuListener> menuOverlays()
	{
		return new OverlayIterator<MenuListener>(mapView_, MenuListener.class);
	} // menuOverlays
	private Iterator<DynamicMenuListener> dynamicMenuOverlays()
	{
		return new OverlayIterator<DynamicMenuListener>(mapView_, DynamicMenuListener.class);
	} // dynamicMenuOverlays
	private Iterator<ContextMenuListener> contextMenuOverlays()
	{
		return new OverlayIterator<ContextMenuListener>(mapView_, ContextMenuListener.class);
	} // contextMenuOverlays
	private Iterator<PauseResumeListener> pauseResumeOverlays()
	{
	  return new OverlayIterator<PauseResumeListener>(mapView_, PauseResumeListener.class);
	} // pauseResumeOverlays
	
} // class ControllerOverlay
