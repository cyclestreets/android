package net.cyclestreets.views.overlay;

import org.osmdroid.views.MapView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;

import net.cyclestreets.PhotoUploadActivity;
import net.cyclestreets.view.R;

public class PhotoUploadButtonOverlay extends ButtonOnlyOverlay {
  /////////////////////////////////////////////////////
  private final Context context_;

  private final OverlayButton addPhotoBtn_;

  public PhotoUploadButtonOverlay(final Context context) {
    super(context);

    context_ = context;

    final int offset = DrawingHelper.offset(context);
    final float radius = DrawingHelper.cornerRadius(context);

    final Resources res = context.getResources();
    addPhotoBtn_ = new OverlayButton(res.getDrawable(R.drawable.ic_menu_takephoto),
        offset,
        offset*2,
        radius);
    addPhotoBtn_.bottomAlign();
  } // PhotoItemOverlay

  ///////////////////////////////////////////////////
  @Override
  public boolean onButtonTap(final MotionEvent event) {
    if(addPhotoBtn_.hit(event)) {
      context_.startActivity(new Intent(context_, PhotoUploadActivity.class));
      return true;
    } // if ...
    return false;
  } // onButtonTap

  @Override
  public boolean onButtonDoubleTap(final MotionEvent event) {
    return onButtonTap(event);
  } // onButtonDoubleTap

  @Override
  public void drawButtons(final Canvas canvas, final MapView mapView) {
    addPhotoBtn_.draw(canvas);
  } // drawButtons
} // class PhotoItemOverlay
