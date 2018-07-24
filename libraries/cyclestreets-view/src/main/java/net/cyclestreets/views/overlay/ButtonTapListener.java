package net.cyclestreets.views.overlay;

import org.osmdroid.views.MapView;

import android.graphics.Canvas;

public interface ButtonTapListener
{
  void drawButtons(final Canvas canvas, final MapView mapView);
}
