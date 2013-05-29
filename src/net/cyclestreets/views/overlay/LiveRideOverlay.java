package net.cyclestreets.views.overlay;

import java.util.List;

import net.cyclestreets.liveride.LiveRideService;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Collections;
import net.cyclestreets.util.Draw;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.location.Location;
import android.os.IBinder;
import android.view.View;

public class LiveRideOverlay extends Overlay implements ServiceConnection, 
                                                        PauseResumeListener
{
  private final Activity activity_;
  private LiveRideService.Binding binding_;
  private final int offset_;
  private final float radius_;
  private final Paint speedBrush_;
  private final Paint textBrush_;

  private static List<String> headings_ = Collections.list("N", "NE", "E", "SE", "S", "SW", "W", "NW");

  public LiveRideOverlay(final Activity context, final View view) 
  {
    super(context);
    
    activity_ = context;

    final Intent intent = new Intent(activity_, LiveRideService.class);
    activity_.bindService(intent, this, Context.BIND_AUTO_CREATE);
    
    offset_ = DrawingHelper.offset(context);
    radius_ = DrawingHelper.cornerRadius(context);
    speedBrush_ = Brush.createTextBrush(offset_*4);
    speedBrush_.setTextAlign(Align.LEFT);
    textBrush_ = Brush.createTextBrush(offset_);
    textBrush_.setTextAlign(Align.LEFT);
} // LiveRideOverlay

  @Override
  public void onDetach(final MapView mapView)
  {
    if(binding_ != null)
      binding_.stopRiding();

    super.onDetach(mapView);
  } // onDetach

  @Override
  protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow)
  {
    final String speed = speed();
    final String info = movementDetails();
    if(info == null)
      return;
    
    final Rect box = canvas.getClipBounds();
    box.left += offset_; 
    box.right = box.left + (box.width()/2);
    box.bottom -= (offset_*2);
    box.top = box.bottom - offset_;
        
    final Rect textBox = new Rect(box);
    textBox.left += offset_;
    textBox.right -= offset_;
    
    int sHeight = Draw.measureTextInRect(canvas, speedBrush_, textBox, speed) - box.top;
    int iHeight = Draw.measureTextInRect(canvas, textBrush_, textBox, info) - box.top;
    
    int height = sHeight + iHeight + (offset_*2); 
    box.top = box.bottom - height;
    
    if(!DrawingHelper.drawRoundRect(canvas, box, radius_, Brush.Grey))
      return;
    box.left += offset_;
    Draw.drawTextInRect(canvas, speedBrush_, box, speed);
    box.top += sHeight;
    Draw.drawTextInRect(canvas, textBrush_, box, info);
    box.top -= offset_*2;
    box.left += (box.width() * 3 / 4); 
    Draw.drawTextInRect(canvas, textBrush_, box, "kmh");
  } // draw

  ///////////////////////////
  @Override
  public void onServiceConnected(final ComponentName className, final IBinder binder)
  {
    binding_ = (LiveRideService.Binding)binder;
    
    if(!binding_.areRiding())
      binding_.startRiding();
  } // onServiceConnected

  @Override
  public void onServiceDisconnected(final ComponentName className)
  {
  } // onServiceDisconnected

  @Override
  public void onResume(SharedPreferences prefs)
  {
  } // onResume

  @Override
  public void onPause(Editor prefs)
  {
  } // onPause

  private Location lastLocation() 
  {
    if(!Route.available())
      return null;

    if(binding_ == null)
      return null;
    
    final Location location = binding_.lastLocation();
    if(location == null)
      return null;

    return location;
  } // lastLocation

  private String speed() 
  {
    final Location location = lastLocation();
    if(location == null)
      return "0.0";
    
    final double speed = location.getSpeed() * 60.0 * 60.0 / 1000.0; 
    return String.format("%.1f", speed);
  } // speed
  
  private String movementDetails()
  {
    final Location location = lastLocation();
    if(location == null)
      return null;
    
    final int bearing = (int)location.getBearing();
    
    final GeoPoint whereIam = new GeoPoint(location);
    final Segment activeSeg = Route.journey().activeSegment();
    final int distance = activeSeg.distanceFrom(whereIam);
    final int crossTrack = activeSeg.crossTrackError(whereIam);
    final int alongTrackError = activeSeg.alongTrackError(whereIam);
    final int alongTrack = activeSeg.alongTrack(whereIam);
    final int fromEnd = activeSeg.distanceFromEnd(whereIam);

    final String info = String.format("Heading : %s\nDistance : %d m\nCross-track : %dm\nAlong-track error : %dm\nAlong-track : %dm\nFrom end : %d m\n%s",
                        heading(bearing),
                        distance,
                        crossTrack,
                        alongTrackError,
                        alongTrack,
                        fromEnd,
                        binding_.stage());
    
    return info;
  } // onLocationChanged
  
  public String heading(int bearing) 
  {
    final double step = 360.0 / headings_.size();
    double chunk = step/2;
    
    for(final String h : headings_)
    {
      if(bearing < chunk) 
        return h;
      chunk += step;
    }
    
    return headings_.get(0);
  } // heading

} // class LiveRideOverlay
