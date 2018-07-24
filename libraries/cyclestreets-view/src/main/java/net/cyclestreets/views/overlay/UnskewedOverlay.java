package net.cyclestreets.views.overlay;

import android.graphics.Canvas;

// An overlay which shouldn't be scaled when zooming in or out of the map
public interface UnskewedOverlay
{
  void drawUnskewed(final Canvas canvas);
}
