package net.cyclestreets.views.overlay;

import java.util.Iterator;

import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Segment;
import net.cyclestreets.api.Segments;
import net.cyclestreets.api.Waypoints;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Route.Listener;

import org.osmdroid.util.GeoPoint;
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
   
  private Path ridePath_;
  private Path walkPath_;
  private Path highlightPath_;
  
  private int zoomLevel_ = -1;
  private Segment highlight_;
  
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
		
    if((zoomLevel_ != mapView.getZoomLevel() && !mapView.isAnimating()) ||
       (highlight_ != Route.journey().activeSegment()))
    {
      ridePath_ = null;
      zoomLevel_ = mapView.getProjection().getZoomLevel();
      highlight_ = Route.journey().activeSegment();
    } // if ... 
  
    if(ridePath_ == null)
      drawSegments(mapView.getProjection());

    canvas.drawPath(ridePath_, rideBrush_);
    canvas.drawPath(walkPath_, walkBrush_);
    canvas.drawPath(highlightPath_, Route.journey().activeSegment().walk() ? hiWalkBrush_ : hiRideBrush_);
  } // draw

  private Path newPath()
  {
    final Path path = new Path();
    path.rewind();
    return path;
  } // newPath
  
  private void drawSegments(final IProjection projection)
  {
    ridePath_ = newPath();
    walkPath_ = newPath();
    highlightPath_ = newPath();

    Point screenPoint = new Point();
    for(Segment s : route_)
    {
      Path path = s.walk() ? walkPath_ : ridePath_;
      path = (Route.journey().activeSegment() != s) ? path : highlightPath_; 
      
      boolean first = true;
      for(Iterator<GeoPoint> i = s.points(); i.hasNext(); )
      {
        final GeoPoint gp = i.next();
        screenPoint = projection.toPixels(gp, screenPoint);
        
        if(first)
        {
          path.moveTo(screenPoint.x, screenPoint.y);
          first = false;
        } 
        else
          path.lineTo(screenPoint.x, screenPoint.y);
      } // for ...
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
