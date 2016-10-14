package net.cyclestreets.views.overlay;

import android.content.Context;
import android.graphics.Canvas;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

public abstract class ButtonOnlyOverlay extends Overlay implements ButtonTapListener {
  protected ButtonOnlyOverlay(final Context context) {
    super(context);
  } // ButtonOnlyOverlay

  @Override
  public final void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
  } // draw
} // class ButtonOverlay
