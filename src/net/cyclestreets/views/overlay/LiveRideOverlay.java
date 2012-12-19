package net.cyclestreets.views.overlay;

import net.cyclestreets.liveride.LiveRideService;
import net.cyclestreets.planned.Route;
import net.cyclestreets.util.Brush;
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

public class LiveRideOverlay extends Overlay implements ServiceConnection, 
                                                        LocationListener,
                                                        PauseResumeListener
{
  private final Activity activity_;
  private final View view_;
  private final LocationManager locationManager_;
  private LiveRideService.Binding binding_;
  private final int offset_;
  private final float radius_;
  private final Paint speedBrush_;
  private final Paint textBrush_;

  private String speed_ = null;
  private String info_ = null;

  public LiveRideOverlay(final Activity context, final View view) 
  {
    super(context);
    
    activity_ = context;
    view_ = view;

    final Intent intent = new Intent(activity_, LiveRideService.class);
    activity_.bindService(intent, this, Context.BIND_AUTO_CREATE);
    
    locationManager_ = (LocationManager)activity_.getSystemService(Context.LOCATION_SERVICE);
    
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
    if(info_ == null)
      return;
    
    final Rect box = canvas.getClipBounds();
    box.left += offset_; 
    box.right = box.left + (box.width()/2);
    box.bottom -= offset_;
    box.top = box.bottom - offset_;
        
    final Rect textBox = new Rect(box);
    textBox.left += offset_;
    textBox.right -= offset_;
    
    int bottom = Draw.measureTextInRect(canvas, speedBrush_, textBox, speed_);
    int height = bottom - box.top; 
    box.top = box.bottom - (height + offset_*6);
    
    if(!DrawingHelper.drawRoundRect(canvas, box, radius_, Brush.Grey))
      return;
    box.left += offset_;
    Draw.drawTextInRect(canvas, speedBrush_, box, speed_);
    box.top += height;
    Draw.drawTextInRect(canvas, textBrush_, box, info_);
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
    locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
  } // onResume

  @Override
  public void onPause(Editor prefs)
  {
    locationManager_.removeUpdates(this);
  } // onPause

  @Override
  public void onLocationChanged(final Location location)
  {
    if(!Route.available())
      return;
    
    final double speed = location.getSpeed() * 60.0 * 60.0 / 1000.0; 
    final int bearing = (int)location.getBearing();
    final int crossTrack = Route.journey().activeSegment().distanceFrom(new GeoPoint(location));

    final String info = String.format("Bearing : %d deg\nError : %d m",
                        bearing,
                        crossTrack);
    
    if(info.equals(info_))
      return;

    speed_ = String.format("%.1f", speed);
    info_ = info;
    view_.invalidate();
  } // onLocationChanged

  @Override
  public void onProviderDisabled(String arg0)
  {
  }

  @Override
  public void onProviderEnabled(String arg0)
  {
  }

  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2)
  {
  }
} // class LiveRideOverlay
