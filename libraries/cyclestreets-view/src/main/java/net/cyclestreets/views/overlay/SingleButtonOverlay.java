package net.cyclestreets.views.overlay;

import org.osmdroid.views.MapView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;

public abstract class SingleButtonOverlay extends ButtonOnlyOverlay {
  private final int offset_;
  private final float radius_;

  private final OverlayButton theButton_;

  public SingleButtonOverlay(final Context context,
                             final int drawable) {
    super(context);

    offset_ = DrawingHelper.offset(context);
    radius_ = DrawingHelper.cornerRadius(context);

    final Resources res = context.getResources();
    theButton_ = new OverlayButton(res.getDrawable(drawable),
        offset_,
        offset_,
        radius_);

    layout(theButton_);
  } // SingleButtonOverlay

  //////////////////////////////////////////////
  //////////////////////////////////////////////
  protected void layout(OverlayButton theButton) { }

  protected void setState(OverlayButton theButton, MapView mapView) { }

  protected void buttonTapped() { }

  protected void buttonDoubleTapped() { }

  //////////////////////////////////////////////
  //////////////////////////////////////////////
  @Override
  public final void drawButtons(final Canvas canvas, final MapView mapView)  {
    setState(theButton_, mapView);
    theButton_.draw(canvas);
  } // drawLocationButton

  @Override
  public final boolean onButtonTap(final MotionEvent event) {
    if(!theButton_.hit(event))
      return false;

    buttonTapped();

    return true;
  } // onSingleTapUp

  @Override
  public final boolean onButtonDoubleTap(final MotionEvent event) {
    if(!theButton_.hit(event))
      return false;

    buttonDoubleTapped();

    return true;
  } // onDoubleTap
} // StopActivityOverlay
