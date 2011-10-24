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
  static public interface LocationListener {
    void onSetLocation(final GeoPoint point);
  }
  
  private final MapView mapView_;
  
  private final Drawable thereMarker_;
  private GeoPoint there_ = null;
  private LocationListener listener_;
  
  public ThereOverlay(final Context context,
                      final MapView mapView)
  {
    super(context);
    mapView_ = mapView;
    
    final Resources res = context.getResources();
    thereMarker_  = res.getDrawable(R.drawable.x_marks_spot);
  } // ThereOverlay
  
  public void setLocationListener(final LocationListener listener)
  {
    listener_ = listener;
  } // setLocationListener
  
  public GeoPoint there() { return there_; }
  public void noOverThere(final GeoPoint there)
  {
    there_ = there;
    
    recentre();
    
    if(listener_ != null)
      listener_.onSetLocation(there);
  } // noOverThere
  
  public void recentre()
  {
    if(there_ == null)
      return;
   
    mapView_.getController().animateTo(there_);
    mapView_.invalidate();    
  } // recentre
    
  
  @Override
  protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
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
