package net.cyclestreets.views.overlay;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import net.cyclestreets.Undoable;
import net.cyclestreets.util.Brush;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
	private final CycleMapView mapView_;
	private final Paint textBrush_;
	private List<Undoable> undoStack_;
	
	public ControllerOverlay(final Context context, final CycleMapView mapView)
	{
		super(context);
		
		mapView_ = mapView;
		textBrush_ = Brush.createTextBrush(DrawingHelper.offset(context)/2);
		textBrush_.setColor(Color.BLACK);
		
		gestureDetector_ = new GestureDetector(context, this);
		gestureDetector_.setOnDoubleTapListener(this);
		
		undoStack_ = new ArrayList<>();
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

	public void onCreateOptionsMenu(final Menu menu)
	{
		for(final Iterator<MenuListener> overlays = menuOverlays(); overlays.hasNext(); )
			overlays.next().onCreateOptionsMenu(menu);
	} // onCreateOptionsMenu
	
	public void onPrepareOptionsMenu(final Menu menu)
	{
		for(final Iterator<MenuListener> overlays = menuOverlays(); overlays.hasNext(); )
			overlays.next().onPrepareOptionsMenu(menu);
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
	
	//////////////////////////////////////////////
	public boolean onBackPressed()
	{
	  if(undoStack_.isEmpty())
	    return false;
	  
	  int last = undoStack_.size()-1;
    final Undoable undo = undoStack_.get(last);
    undoStack_.remove(last);
    undo.onBackPressed();
	  return true;
	} // onBackPressed
	
	public void pushUndo(final Undoable undo)
	{
	  undoStack_.add(undo);
	} // pushUndo
	
	public void flushUndo(final Undoable undo)
	{
	  for(int i = undoStack_.size() - 1; i >= 0; --i)
	    if(undoStack_.get(i).equals(undo))
	      undoStack_.remove(i);
	} // flushUndo

  public void popUndo(final Undoable undo)
  {
    for(int i = undoStack_.size() - 1; i >= 0; --i)
      if(undoStack_.get(i).equals(undo))
      {
        undoStack_.remove(i);
        return;
      } // if ...
  } // flushUndo

	////////////////////////////////////////////////////////////////
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
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    final Matrix unscaled = mapView.getProjection().getInvertedScaleRotateCanvasMatrix();

    canvas.save();
    canvas.concat(unscaled);

    drawUnskewed(canvas, mapView);

    canvas.restore();
	} // draw

  protected void drawUnskewed(final Canvas canvas, final MapView mapView) {
    for(final Iterator<ButtonTapListener> overlays = buttonTapOverlays(); overlays.hasNext(); )
      overlays.next().drawButtons(canvas, mapView);

    if(!(mapView instanceof CycleMapView))
      return;

    final Rect screen = canvas.getClipBounds();
    canvas.drawText(mapView_.mapAttribution(),
        screen.centerX(),
        screen.bottom-(textBrush_.descent()+2),
        textBrush_);
  } // drawReverted
	
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) { return false; }
	@Override
	public boolean onDown(MotionEvent e) { return false; }
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
	@Override
	public void onLongPress(final MotionEvent e) {
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
		return new OverlayIterator<>(mapView_, TapListener.class);
	} // tapOverlays
  private Iterator<ButtonTapListener> buttonTapOverlays()
  {
    return new OverlayIterator<>(mapView_, ButtonTapListener.class);
  } // buttonTapOverlays
	private Iterator<MenuListener> menuOverlays()
	{
		return new OverlayIterator<>(mapView_, MenuListener.class);
	} // menuOverlays
	private Iterator<ContextMenuListener> contextMenuOverlays()
	{
		return new OverlayIterator<>(mapView_, ContextMenuListener.class);
	} // contextMenuOverlays
	private Iterator<PauseResumeListener> pauseResumeOverlays()
	{
	  return new OverlayIterator<>(mapView_, PauseResumeListener.class);
	} // pauseResumeOverlays	
} // class ControllerOverlay
