package net.cyclestreets.views.overlay;

import android.widget.Toast;
import net.cyclestreets.util.Theme;
import net.cyclestreets.view.R;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.MapView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.osmdroid.views.overlay.Overlay;

public class LockScreenOnOverlay extends Overlay implements PauseResumeListener {
  private static String LOCK_PREF = "lockScreen";

  private final CycleMapView mapView;

  private final FloatingActionButton screenLockButton;
  private final Drawable onIcon;
  private final Drawable offIcon;

  public LockScreenOnOverlay(final Context context, final CycleMapView mapView) {
    super();
    this.mapView = mapView;

    onIcon = new IconicsDrawable(context)
        .icon(GoogleMaterial.Icon.gmd_lock_open)
        .color(Theme.highlightColor(context))
        .sizeDp(24);
    offIcon = new IconicsDrawable(context)
        .icon(GoogleMaterial.Icon.gmd_lock_open)
        .color(Theme.lowlightColor(context))
        .sizeDp(24);

    View liverideButtonView = LayoutInflater.from(mapView.getContext()).inflate(R.layout.liveride_buttons, null);
    screenLockButton = liverideButtonView.findViewById(R.id.liveride_screenlock_button);
    screenLockButton.setOnClickListener(view -> screenLockButtonTapped());
    mapView.addView(liverideButtonView);

    mapView.setKeepScreenOn(false);
  }

  private void screenLockButtonTapped() {
    setScreenLockState(!mapView.getKeepScreenOn());
  }

  private void setScreenLockState(boolean state) {
    Log.d("LiveRide", "Setting keepScreenOn state to " + state);
    screenLockButton.setImageDrawable(state ? onIcon : offIcon);
    int message = state ? R.string.liveride_keep_screen_on_enabled : R.string.liveride_keep_screen_on_disabled;
    Toast.makeText(mapView.getContext(), message, Toast.LENGTH_LONG).show();
    mapView.setKeepScreenOn(state);
  }

  @Override
  public void draw(Canvas c, MapView osmv, boolean shadow) {}

  /////////////////////////////////////////
  @Override
  public void onResume(final SharedPreferences prefs) {
    mapView.setKeepScreenOn(prefs.getBoolean(LOCK_PREF, false));
  }

  @Override
  public void onPause(final Editor prefs) {
    prefs.putBoolean(LOCK_PREF, mapView.getKeepScreenOn());
  }
}
