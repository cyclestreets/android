package net.cyclestreets.views.overlay;

import net.cyclestreets.view.R;

import android.app.Activity;

public class StopActivityOverlay extends SingleButtonOverlay {
  private final Activity activity_;

  public StopActivityOverlay(final Activity context) {
    super(context, R.drawable.btn_stop);

    activity_ = context;
  } // StopActivityOverlay

  //////////////////////////////////////////////
  @Override
  protected void buttonTapped() {
    activity_.finish();
  } // onSingleTapUp
} // StopActivityOverlay
