package net.cyclestreets.views.overlay;

import net.cyclestreets.view.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class LockScreenOnOverlay extends Overlay implements ButtonTapListener, PauseResumeListener
{
  private static String LOCK_PREF = "lockScreen";

  private final View view_;

  private final int offset_;
	private final float radius_;

	private final OverlayButton lockButton_;

	public LockScreenOnOverlay(final Context context, final View view)
	{
		super(context);

		view_ = view;

		offset_ = DrawingHelper.offset(context);
		radius_ = DrawingHelper.cornerRadius(context);

		final Resources res = context.getResources();
		lockButton_ = new OverlayButton(res.getDrawable(R.drawable.ic_action_lock),
		                                offset_,
		                                offset_,
		                                radius_);
		lockButton_.rightAlign();
	} // LocationOverlay

	////////////////////////////////////////////
  @Override
  protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow)
  {
  } // draw

	@Override
	public void drawButtons(final Canvas canvas, final MapView mapView)
	{
	  lockButton_.pressed(view_.getKeepScreenOn());
		lockButton_.draw(canvas);
	} // drawLocationButton

  //////////////////////////////////////////////
	@Override
	public boolean onButtonTap(final MotionEvent event)
	{
	  if(!lockButton_.hit(event))
	    return false;

	  view_.setKeepScreenOn(!view_.getKeepScreenOn());

	  return true;
	} // onSingleTapUp

	@Override
	public boolean onButtonDoubleTap(final MotionEvent event)
	{
		return lockButton_.hit(event);
	} // onDoubleTap

  @Override
  public void onResume(final SharedPreferences prefs)
  {
    view_.setKeepScreenOn(prefs.getBoolean(LOCK_PREF, false));
  } // onResume

  @Override
  public void onPause(final Editor prefs)
  {
    prefs.putBoolean(LOCK_PREF, view_.getKeepScreenOn());
  } // onPause
} // LockScreenOnOverlay
