package net.cyclestreets.views.overlay;

import net.cyclestreets.service.LiveRideService;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.os.IBinder;

public class LiveRideOverlay extends Overlay implements ServiceConnection
{
  private final Activity activity_;
  private LiveRideService.Binding binding_;
  private final int offset_;
  private final float radius_;
  private final Paint textBrush_;

  public LiveRideOverlay(final Activity context) 
  {
    super(context);
    
    activity_ = context;

    final Intent intent = new Intent(activity_, LiveRideService.class);
    activity_.bindService(intent, this, Context.BIND_AUTO_CREATE);
    
    offset_ = DrawingHelper.offset(context);
    radius_ = DrawingHelper.cornerRadius(context);
    textBrush_ = Brush.createTextBrush(offset_);
    textBrush_.setTextAlign(Align.LEFT);
} // LiveRideOverlay

  @Override
  public void onDetach(MapView mapView)
  {
    if(binding_ != null)
      binding_.stopRiding();

    super.onDetach(mapView);
  } // onDetach

  @Override
  protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow)
  {
    final String info = String.format("Speed : %d m/s\nBearing : %d deg\nError : %d m",
                                      binding_.speed(),
                                      binding_.bearing(),
                                      binding_.crossTrack());
    
    final Rect box = canvas.getClipBounds();
    box.left += offset_; 
    box.right = box.left + (box.width()/2);
    box.bottom -= offset_;
    box.top = box.bottom - offset_;
        
    final Rect textBox = new Rect(box);
    textBox.left += offset_;
    textBox.right -= offset_;
    
    int bottom = Draw.measureTextInRect(canvas, textBrush_, textBox, info);
    int height = bottom - box.top; 
    box.top = box.bottom - height;
    
    if(!DrawingHelper.drawRoundRect(canvas, box, radius_, Brush.Grey))
      return;
    
    Draw.drawTextInRect(canvas, textBrush_, textBox, info);
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
} // class LiveRideOverlay
