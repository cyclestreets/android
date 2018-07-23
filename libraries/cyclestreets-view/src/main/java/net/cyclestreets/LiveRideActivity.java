package net.cyclestreets;

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

import com.mikepenz.iconics.context.IconicsContextWrapper;

public class LiveRideActivity extends Activity
{
  public static void launch(final Context context) {
    if (!GPS.isOn(context)) {
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
  }

  private static void launchActivity(final Context context) {
    final Intent intent = new Intent(context, LiveRideActivity.class);
    context.startActivity(intent);
  }

  private CycleMapView map_;

  @Override
  protected void attachBaseContext(Context newBase) {
    // Allows the use of Material icon library, see https://github.com/mikepenz/Android-Iconics
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
  }

  @Override
  public void onCreate(final Bundle saved) {
    super.onCreate(saved);

    // Map initialized in onResume
  }

  //////////////////////////
  @Override
  public void onPause() {
    map_.disableFollowLocation();
    map_.onPause();

    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();

    // Map needs to be recreated, because tile provider is shut down on CycleMapView.onPause
    initializeMapView();
    map_.onResume();
    map_.enableAndFollowLocation();
  }

  private void initializeMapView() {
    map_ = new CycleMapView(this, this.getClass().getName());
    map_.overlayPushBottom(new RouteOverlay());
    map_.overlayPushTop(new LockScreenOnOverlay(this, map_));
    map_.overlayPushTop(new LiveRideOverlay(this, map_));
    map_.lockOnLocation();
    map_.hideLocationButton();

    final RelativeLayout rl = new RelativeLayout(this);
    rl.addView(map_, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    setContentView(rl);
  }

}
