package net.cyclestreets.views.overlay;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import net.cyclestreets.Undoable;
import net.cyclestreets.tiles.TileSource;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Logging;
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

public class ControllerOverlay extends Overlay implements OnDoubleTapListener, OnGestureListener
{
  private static final String TAG = Logging.getTag(ControllerOverlay.class);

  private final GestureDetector gestureDetector_;
  private final CycleMapView mapView_;
  private final Paint textBrush_;
  private List<Undoable> undoStack_;
  private boolean attributionShifted;

  public ControllerOverlay(final CycleMapView mapView) {
    super();

    mapView_ = mapView;

    final Context context = mapView_.getContext();
    textBrush_ = Brush.createTextBrush(DrawingHelperKt.offset(context)/2);
    textBrush_.setColor(Color.BLACK);

    gestureDetector_ = new GestureDetector(context, this);
    gestureDetector_.setOnDoubleTapListener(this);

    undoStack_ = new ArrayList<>();
  }

  public void onPause(final SharedPreferences.Editor prefEditor) {
    for (final Iterator<PauseResumeListener> overlays = pauseResumeOverlays(); overlays.hasNext(); )
      overlays.next().onPause(prefEditor);
  }

  public void onResume(final SharedPreferences prefs) {
    for (final Iterator<PauseResumeListener> overlays = pauseResumeOverlays(); overlays.hasNext(); )
      overlays.next().onResume(prefs);
  }

  public void onCreateOptionsMenu(final Menu menu) {
    for (final Iterator<MenuListener> overlays = menuOverlays(); overlays.hasNext(); )
      overlays.next().onCreateOptionsMenu(menu);
  }

  public void onPrepareOptionsMenu(final Menu menu) {
    for (final Iterator<MenuListener> overlays = menuOverlays(); overlays.hasNext(); )
      overlays.next().onPrepareOptionsMenu(menu);
  }

  public void onCreateContextMenu(final ContextMenu menu) {
    for (final Iterator<ContextMenuListener> overlays = contextMenuOverlays(); overlays.hasNext(); )
      overlays.next().onCreateContextMenu(menu);
  }

  public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
    for (final Iterator<MenuListener> overlays = menuOverlays(); overlays.hasNext(); )
      if (overlays.next().onMenuItemSelected(featureId, item))
        return true;
    return false;
  }

  //////////////////////////////////////////////
  public boolean onBackPressed() {
    if (undoStack_.isEmpty())
      return false;

    int last = undoStack_.size()-1;
    final Undoable undo = undoStack_.get(last);
    undoStack_.remove(last);
    undo.onBackPressed();
    return true;
  }

  public void pushUndo(final Undoable undo) {
    undoStack_.add(undo);
  }

  public void flushUndo(final Undoable undo) {
    for (int i = undoStack_.size() - 1; i >= 0; --i)
      if (undoStack_.get(i).equals(undo))
        undoStack_.remove(i);
  }

  public boolean checkUndo(final Undoable undo) {
    for (int i = undoStack_.size() - 1; i >= 0; --i)
      if (undoStack_.get(i).equals(undo))
        return true;
      return false;
  }

  public void popUndo(final Undoable undo) {
    for (int i = undoStack_.size() - 1; i >= 0; --i)
      if (undoStack_.get(i).equals(undo)) {
        undoStack_.remove(i);
        return;
      }
  }

  ////////////////////////////////////////////////////////////////
  private boolean redraw() {
    mapView_.mapView().postInvalidate();
    return true;
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
    if (gestureDetector_.onTouchEvent(event))
      return redraw();
    return super.onTouchEvent(event, mapView);
  }

  @Override
  public boolean onSingleTapConfirmed(final MotionEvent e) {
    for (final Iterator<TapListener> overlays = tapOverlays(); overlays.hasNext(); )
      if (overlays.next().onSingleTap(e))
        return redraw();
    return false;
  }

  @Override
  public boolean onDoubleTap(final MotionEvent e) {
    for (final Iterator<TapListener> overlays = tapOverlays(); overlays.hasNext(); )
      if (overlays.next().onDoubleTap(e))
        return redraw();
    return false;
  }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    final Matrix unscaled = mapView.getProjection().getInvertedScaleRotateCanvasMatrix();

    canvas.save();
    canvas.concat(unscaled);

    drawUnskewed(canvas);

    canvas.restore();
  }

  private void drawUnskewed(final Canvas canvas) {
    final Rect screen = canvas.getClipBounds();
    final int yHeight = attributionShifted ? TileSource.getAttributionUpShift() : 0;
    canvas.drawText(mapView_.mapAttribution(),
            screen.centerX(),
            screen.bottom - (textBrush_.descent()+2) - yHeight,
            textBrush_);
  }

  public void setAttributionShifted() {
    this.attributionShifted = true;
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent e) { return false; }
  @Override
  public boolean onDown(MotionEvent e) { return false; }
  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
  @Override
  public void onLongPress(final MotionEvent e) {
    mapView_.showContextMenu();
  }
  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
  @Override
  public void onShowPress(MotionEvent e) { }
  @Override
  public boolean onSingleTapUp(MotionEvent e) { return false; }

  //////////////////////////////////////////////////////
  private Iterator<TapListener> tapOverlays() {
    return new OverlayIterator<>(mapView_, TapListener.class);
  }
  private Iterator<MenuListener> menuOverlays() {
    return new OverlayIterator<>(mapView_, MenuListener.class);
  }
  private Iterator<ContextMenuListener> contextMenuOverlays() {
    return new OverlayIterator<>(mapView_, ContextMenuListener.class);
  }
  private Iterator<PauseResumeListener> pauseResumeOverlays() {
    return new OverlayIterator<>(mapView_, PauseResumeListener.class);
  }
}
