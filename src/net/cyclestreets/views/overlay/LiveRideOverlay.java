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
import android.graphics.Canvas;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import static net.cyclestreets.FragmentHelper.createMenuItem;
import static net.cyclestreets.FragmentHelper.showMenuItem;

public class LiveRideOverlay extends Overlay implements MenuListener, ServiceConnection
{
  private Context context_;
  private boolean active_;
  private LiveRideService.LocalBinder binding_;
  
  public LiveRideOverlay(final Context context)
  {
    super(context);
    context_ = context;
    active_ = false;
  } // LiveRideOverlay

  @Override
  protected void draw(final Canvas canvas, final MapView view, final boolean shadow)
  {
    // nothing!
  } // draw

  @Override
  public void onCreateOptionsMenu(Menu menu)
  {
    createMenuItem(menu, R.string.ic_menu_liveride, Menu.NONE, 0);
    createMenuItem(menu, R.string.ic_menu_stopride, Menu.NONE, 0);
  } // onCreateOptionsMenu

  @Override
  public void onPrepareOptionsMenu(Menu menu)
  {
    showMenuItem(menu, R.string.ic_menu_liveride, Route.available() && !active_);
    showMenuItem(menu, R.string.ic_menu_stopride, Route.available() && active_);
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
  
  private void enableLiveRide() 
  {
    final Intent intent = new Intent(context_, LiveRideService.class);
    context_.bindService(intent, this, Context.BIND_AUTO_CREATE);    
  } // enableLiveRide
  
  private void disableLiveRide() 
  {
    active_ = false;
    context_.unbindService(this);
  } // disableLiveRide

  @Override
  public void onServiceConnected(final ComponentName className, final IBinder binder)
  {
    active_ = true;
    binding_ = (LiveRideService.LocalBinder)binder;
  } // onServiceConnected

  @Override
  public void onServiceDisconnected(final ComponentName className)
  {
    active_ = false;
  } // onServiceDisconnected
} // LiveRideOverlay
