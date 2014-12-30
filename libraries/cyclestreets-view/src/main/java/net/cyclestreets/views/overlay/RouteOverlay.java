package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.routing.Segments;
import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.routing.Route.Listener;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.api.IProjection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class RouteOverlay extends Overlay implements PauseResumeListener, Listener
{
  static public int ROUTE_COLOUR = 0x80ff00ff;
  static public int HIGHLIGHT_COLOUR = 0xA000ff00;

  private Segments route_;

  private final Paint rideBrush_;
  private final Paint walkBrush_;
  private final Paint hiRideBrush_;
  private final Paint hiWalkBrush_;
   
  private List<Path> ridePath_;
  private List<Path> walkPath_;
  private List<Path> highlightPath_;
  
  private int zoomLevel_ = -1;
  private Segment highlight_;
  private IGeoPoint mapCentre_;

  public RouteOverlay(final Context context) 
  {
    super(context);
    
    rideBrush_ = createBrush(ROUTE_COLOUR);
    walkBrush_ = createBrush(ROUTE_COLOUR);
    walkBrush_.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));

    hiRideBrush_ = createBrush(HIGHLIGHT_COLOUR);
    hiWalkBrush_ = createBrush(HIGHLIGHT_COLOUR);
    hiWalkBrush_.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));

    highlight_ = null;
    
    reset();
  } // PathOverlay
	
  private Paint createBrush(int colour)
  {
    final Paint brush = new Paint();
    
    brush.setColor(colour);
    brush.setStrokeWidth(2.0f);
    brush.setStyle(Paint.Style.STROKE);
    brush.setStrokeWidth(10.0f);
    
    return brush;
  } // createBrush	

  private void setRoute(final Segments routeSegments)
  {
    reset();
    route_ = routeSegments;
  } // setRoute

  private void reset() 
  {
    ridePath_ = null;
    route_ = null;
  } // clearPath

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
  {
    if (shadow) 
      return;
 
    if (route_ == null || route_.count() < 2) 
      return;

    final IGeoPoint centre = mapView.getMapCenter();

    if(zoomLevel_ != mapView.getZoomLevel() ||
       highlight_ != Route.journey().activeSegment() ||
       !centre.equals(mapCentre_))
    {
      ridePath_ = null;
      zoomLevel_ = mapView.getProjection().getZoomLevel();
      highlight_ = Route.journey().activeSegment();
      mapCentre_ = centre;
    } // if ...
  
    if(ridePath_ == null)
      drawSegments(mapView.getProjection());

    for(Path path : ridePath_)
      canvas.drawPath(path, rideBrush_);
    for(Path path : walkPath_)
      canvas.drawPath(path, walkBrush_);
    for(Path path : highlightPath_)
      canvas.drawPath(path, Route.journey().activeSegment().walk() ? hiWalkBrush_ : hiRideBrush_);
  } // draw

  private Path newPath()
  {
    final Path path = new Path();
    path.rewind();
    return path;
  } // newPath
  
  private void drawSegments(final IProjection projection)
  {
    ridePath_ = new ArrayList<>();
    walkPath_ = new ArrayList<>();
    highlightPath_ = new ArrayList<>();

    Point screenPoint = new Point();
    for(Segment s : route_) {
      final Path path = newPath();

      boolean first = true;
      for(Iterator<IGeoPoint> i = s.points(); i.hasNext(); )
      {
        final IGeoPoint gp = i.next();
        screenPoint = projection.toPixels(gp, screenPoint);
        
        if(first)
        {
          path.moveTo(screenPoint.x, screenPoint.y);
          first = false;
        } 
        else
          path.lineTo(screenPoint.x, screenPoint.y);
      } // for ...

      if (Route.journey().activeSegment() == s)
        highlightPath_.add(path);
      else if (s.walk())
        walkPath_.add(path);
      else
        ridePath_.add(path);
    } // for ...
  } // drawSegments

  // pause/resume
  @Override
  public void onResume(SharedPreferences prefs)
  {
    Route.registerListener(this);
  } // onResume

  @Override
  public void onPause(Editor prefs)
  {
    Route.unregisterListener(this);
  } // onPause

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints)
  {
    setRoute(journey.segments());
  } // onNewJourney
  
  @Override
  public void onResetJourney()
  {
    reset();
  } // onReset
} // Path
