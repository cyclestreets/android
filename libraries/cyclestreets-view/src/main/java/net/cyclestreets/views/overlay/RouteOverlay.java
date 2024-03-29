package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.routing.Segments;
import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.api.IProjection;
import org.osmdroid.views.overlay.Overlay;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

// Overlay is used for main route and alternative route (2 instances)
public class RouteOverlay extends Overlay implements PauseResumeListener, Route.Listener
{
  private CycleMapView cMapView;

  private static int ROUTE_COLOUR = 0x80ff00ff;
  private static int HIGHLIGHT_COLOUR = 0xA000ff00;

  private static int ALT_ROUTE_COLOUR = 0x808080ff;

  private boolean altRoute;

  private Segments route_;

  private Paint rideBrush_;
  private Paint walkBrush_;
  private Paint hiRideBrush_;
  private Paint hiWalkBrush_;

  private List<Path> ridePath_;
  private List<Path> walkPath_;
  private List<Path> highlightPath_;

  private int zoomLevel_ = -1;
  private Segment highlight_;
  private IGeoPoint mapCentre_;

  public RouteOverlay(CycleMapView cycleMapView, boolean pAltRoute) {
    super();
    cMapView = cycleMapView;
    altRoute = pAltRoute;
    if (altRoute) {
      createBrushes(ALT_ROUTE_COLOUR);
    }
    else {
      createBrushes(ROUTE_COLOUR);
      hiRideBrush_ = createBrush(HIGHLIGHT_COLOUR);
      hiWalkBrush_ = createBrush(HIGHLIGHT_COLOUR);
      hiWalkBrush_.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));

      highlight_ = null;
    }

    reset();
  }

  private void createBrushes(int colour) {
    rideBrush_ = createBrush(colour);
    walkBrush_ = createBrush(colour);
    walkBrush_.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));

  }

  private Paint createBrush(int colour) {
    final Paint brush = new Paint();

    brush.setColor(colour);
    brush.setStrokeWidth(2.0f);
    brush.setStyle(Paint.Style.STROKE);
    brush.setStrokeWidth(10.0f);

    return brush;
  }

  public void setRoute(final Segments routeSegments) {
    reset();
    route_ = routeSegments;
    if (altRoute) cMapView.postInvalidate();
  }

  private void reset() {
    ridePath_ = null;
    route_ = null;
  }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    if (shadow)
      return;

    if (route_ == null || route_.count() < 2)
      return;

    final IGeoPoint centre = mapView.getMapCenter();

    if (zoomLevel_ != (int)mapView.getZoomLevelDouble() ||
       highlight_ != Route.journey().activeSegment() ||
       !centre.equals(mapCentre_)) {
      ridePath_ = null;
      zoomLevel_ = (int)mapView.getProjection().getZoomLevel();
      highlight_ = Route.journey().activeSegment();
      mapCentre_ = centre;
    }

    if (ridePath_ == null)
      drawSegments(mapView.getProjection());

    for (Path path : ridePath_)
      canvas.drawPath(path, rideBrush_);
    for (Path path : walkPath_)
      canvas.drawPath(path, walkBrush_);
    for (Path path : highlightPath_)
      canvas.drawPath(path, Route.journey().activeSegment().walk() ? hiWalkBrush_ : hiRideBrush_);
  }

  private Path newPath() {
    final Path path = new Path();
    path.rewind();
    return path;
  }

  private void drawSegments(final IProjection projection) {
    ridePath_ = new ArrayList<>();
    walkPath_ = new ArrayList<>();
    highlightPath_ = new ArrayList<>();

    Point screenPoint = new Point();
    for (Segment s : route_) {
      final Path path = newPath();

      boolean first = true;
      for (Iterator<IGeoPoint> i = s.points(); i.hasNext(); ) {
        final IGeoPoint gp = i.next();
        screenPoint = projection.toPixels(gp, screenPoint);

        if (first) {
          path.moveTo(screenPoint.x, screenPoint.y);
          first = false;
        }
        else
          path.lineTo(screenPoint.x, screenPoint.y);
      }

      if (Route.journey().activeSegment() == s)
        highlightPath_.add(path);
      else if (s.walk())
        walkPath_.add(path);
      else
        ridePath_.add(path);
    }
  }

  // pause/resume
  @Override
  public void onResume(SharedPreferences prefs) {
    if (altRoute) {
      // Just do softregister for alt routeoverlay so that OnNewJourney below doesn't get called for alt route
      Route.softRegisterListener(this);
      Route.setAltRouteOverlay(this);
      Route.reloadAltRoute();
    }
    else
      Route.registerListener(this);
  }

  @Override
  public void onPause(Editor prefs) {
    Route.unregisterListener(this);
  }

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    if (!altRoute)
      setRoute(journey.getSegments());
    else
      reset();
  }

  @Override
  public void onResetJourney() {
    reset();
  }
}
