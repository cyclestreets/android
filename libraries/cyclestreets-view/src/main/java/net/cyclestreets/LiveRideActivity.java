package net.cyclestreets;

import net.cyclestreets.liveride.PebbleNotifier;
import net.cyclestreets.util.GPS;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.views.CycleMapView;
import net.cyclestreets.views.overlay.LiveRideOverlay;
import net.cyclestreets.views.overlay.LockScreenOnOverlay;
import net.cyclestreets.views.overlay.RouteOverlay;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class LiveRideActivity extends Activity
{
  static public void launch(final Context context)
  {
    if(!GPS.isOn(context)) {
      MessageBox.YesNo(context, 
                       "LiveRide needs the GPS location service.\n\nWould you like to turn it on now?",
                       new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface arg0, int arg1) { 
                            GPS.showSettings(context);
                          }
                       });
      return;
    }
    launchActivity(context);
  } // launch
  
  static private void launchActivity(final Context context) 
  {
    final Intent intent = new Intent(context, LiveRideActivity.class);
    context.startActivity(intent);
  } // launchActivity
  
  private CycleMapView map_;

  private PebbleNotifier notifier_;

  @Override
  public void onCreate(final Bundle saved)
  {
    super.onCreate(saved);

    // Map initialized in onResume
  } // onCreate

  //////////////////////////
  @Override
  public void onPause()
  {
    map_.disableFollowLocation();
    map_.onPause();

    super.onPause();
  } // onPause
  
  @Override
  public void onResume()
  {
    super.onResume();

    // Map needs to be recreated, because tile provider is shut down on CycleMapView.onPause
    initializeMapView();
    map_.onResume();
    map_.enableAndFollowLocation();
  } // onResume

  private void initializeMapView() {
    map_ = new CycleMapView(this, this.getClass().getName());
    map_.overlayPushBottom(new RouteOverlay(this));
    map_.overlayPushTop(new LockScreenOnOverlay(this, map_));
    map_.overlayPushTop(new LiveRideOverlay(this, map_));
    map_.lockOnLocation();
    map_.hideLocationButton();

    final RelativeLayout rl = new RelativeLayout(this);
    rl.addView(map_, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    setContentView(rl);
  }

} // class LiveRideActivity
