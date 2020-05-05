package net.cyclestreets.views.overlay;

import net.cyclestreets.util.Theme;
import net.cyclestreets.view.R;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class RouteHighlightOverlay extends Overlay
{
  private final CycleMapView mapView;

  private Segment current;

  private final Button routingInfoRect;
  private final ImageView routeNowIcon;
  private final FloatingActionButton prevButton;
  private final FloatingActionButton nextButton;

  private int highlightColour;

  public RouteHighlightOverlay(final Context context, final CycleMapView map) {
    super();

    mapView = map;
    current = null;

    View routeView = LayoutInflater.from(mapView.getContext()).inflate(R.layout.route_view, null);

    routingInfoRect = routeView.findViewById(R.id.routing_info_rect);
    routeNowIcon = routeView.findViewById(R.id.route_now_icon);

    prevButton = routeView.findViewById(R.id.route_highlight_prev);
    prevButton.setVisibility(View.INVISIBLE);
    prevButton.setOnClickListener(view -> regressActiveSegment(1));
    prevButton.setOnLongClickListener(view -> regressActiveSegment(Integer.MAX_VALUE));

    nextButton = routeView.findViewById(R.id.route_highlight_next);
    nextButton.setVisibility(View.INVISIBLE);
    nextButton.setOnClickListener(view -> advanceActiveSegment(1));
    nextButton.setOnLongClickListener(view -> advanceActiveSegment(Integer.MAX_VALUE));

    mapView.addView(routeView);

    highlightColour = Theme.highlightColor(context) | 0xFF000000;
  }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    drawButtons();

    if (current == Route.journey().activeSegment())
      return;

    current = Route.journey().activeSegment();
    if (current == null)
      return;

    drawSegmentInfo();
    this.mapView.getController().animateTo(current.start());
  }

  private void drawButtons() {
    if (!Route.available()) {
      prevButton.hide();
      nextButton.hide();
      return;
    }

    prevButton.setEnabled(!Route.journey().atStart());
    prevButton.show();
    nextButton.setEnabled(!Route.journey().atEnd());
    nextButton.show();
  }

  private void drawSegmentInfo() {
    final Segment seg = Route.journey().activeSegment();
    if (seg == null) {
      // In this case, populating the routing info is done by the TapToRouteOverlay
      return;
    }

    routeNowIcon.setVisibility(View.INVISIBLE);

    routingInfoRect.setBackgroundColor(highlightColour);
    routingInfoRect.setGravity(Gravity.LEFT);
    routingInfoRect.setText(seg.toString());
    routingInfoRect.setEnabled(false);
  }

  private boolean regressActiveSegment(int stepsToMove) {
    if (!Route.available()) {
      return false;
    }

    for (int i = stepsToMove; i > 0 && !Route.journey().atStart(); i--)
      Route.journey().regressActiveSegment();

    mapView.invalidate();
    return true;
  }

  private boolean advanceActiveSegment(int stepsToMove) {
    if (!Route.available())
      return false;

    for (int i = stepsToMove; i > 0 && !Route.journey().atEnd(); i--)
      Route.journey().advanceActiveSegment();
    mapView.invalidate();
    return true;
  }

}
