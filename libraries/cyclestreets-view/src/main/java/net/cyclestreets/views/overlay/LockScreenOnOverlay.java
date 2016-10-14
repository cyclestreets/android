package net.cyclestreets.views.overlay;

import net.cyclestreets.view.R;

import org.osmdroid.views.MapView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;

public class LockScreenOnOverlay extends SingleButtonOverlay
    implements PauseResumeListener {
  private static String LOCK_PREF = "lockScreen";

  private final View view_;

  public LockScreenOnOverlay(final Context context, final View view) {
    super(context, R.drawable.ic_action_lock);

    view_ = view;
  } // LockScreenOnOverlay

  @Override
  protected void layout(final OverlayButton theButton) {
    theButton.rightAlign();
  } // layout

  @Override
  protected void setState(OverlayButton theButton, MapView mapView) {
    theButton.pressed(view_.getKeepScreenOn());
  } // setState

  @Override
  protected void buttonTapped() {
    view_.setKeepScreenOn(!view_.getKeepScreenOn());
  } // buttonTapped

  /////////////////////////////////////////
  @Override
  public void onResume(final SharedPreferences prefs) {
    view_.setKeepScreenOn(prefs.getBoolean(LOCK_PREF, false));
  } // onResume

  @Override
  public void onPause(final Editor prefs) {
    prefs.putBoolean(LOCK_PREF, view_.getKeepScreenOn());
  } // onPause
} // LockScreenOnOverlay
