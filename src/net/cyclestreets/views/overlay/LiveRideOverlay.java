package net.cyclestreets.views.overlay;

import net.cyclestreets.R;
import net.cyclestreets.planned.Route;
import net.cyclestreets.service.LiveRideService;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import static net.cyclestreets.FragmentHelper.createMenuItem;
import static net.cyclestreets.FragmentHelper.showMenuItem;

public class LiveRideOverlay extends Overlay implements MenuListener, PauseResumeListener, ServiceConnection
{
  private Context context_;
  private LiveRideService.Binding binding_;
  
  public LiveRideOverlay(final Context context)
  {
    super(context);
    context_ = context;
  } // LiveRideOverlay

  @Override
  protected void draw(final Canvas canvas, final MapView view, final boolean shadow)
  {
    // nothing!
  } // draw

  @Override
  public void onCreateOptionsMenu(final Menu menu)
  {
    createMenuItem(menu, R.string.ic_menu_liveride, Menu.NONE, 0);
    createMenuItem(menu, R.string.ic_menu_stopride, Menu.NONE, 0);
  } // onCreateOptionsMenu

  @Override
  public void onPrepareOptionsMenu(final Menu menu)
  {
    showMenuItem(menu, R.string.ic_menu_liveride, Route.available() && !areRiding());
    showMenuItem(menu, R.string.ic_menu_stopride, Route.available() && areRiding());
  } // onPrepareOptionsMenu

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item)
  {
    switch(featureId) 
    {
    case R.string.ic_menu_liveride:
      enableLiveRide();
      return true;
    case R.string.ic_menu_stopride:
      disableLiveRide();
      return true;
    default:
      return false;
    } // switch
  } // onMenuItemSelected
  
  @Override
  public void onPause(final Editor prefs)
  {
    context_.unbindService(this);
  } // onPause

  @Override
  public void onResume(final SharedPreferences prefs)
  {
    final Intent intent = new Intent(context_, LiveRideService.class);
    context_.bindService(intent, this, Context.BIND_AUTO_CREATE);    
  } // onResume
  
  private boolean areRiding() 
  {
    return binding_ != null && binding_.areRiding();
  } // areRiding

  private void enableLiveRide() 
  {
    if(binding_ == null)
      return;
    
    binding_.startRiding();
  } // enableLiveRide
  
  private void disableLiveRide() 
  {
    if(binding_ == null)
      return;
    
    binding_.stopRiding();
  } // disableLiveRide

  @Override
  public void onServiceConnected(final ComponentName className, final IBinder binder)
  {
    binding_ = (LiveRideService.Binding)binder;
  } // onServiceConnected

  @Override
  public void onServiceDisconnected(final ComponentName className)
  {
  } // onServiceDisconnected
} // LiveRideOverlay
