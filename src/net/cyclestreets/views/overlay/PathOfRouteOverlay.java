package net.cyclestreets.views.overlay;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import net.cyclestreets.api.Segment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class PathOfRouteOverlay extends Overlay 
{
  static public int ROUTE_COLOUR = 0x80ff00ff;

  private List<Segment> route_;

  private Paint rideBrush_;
  private Paint walkBrush_;
  
  private int zoomLevel_ = -1;
  private Path ridePath_;
  private Path walkPath_;
  
  public PathOfRouteOverlay(final Context context) 
  {
    super(context);
    
    rideBrush_ = createBrush();
    
    walkBrush_ = createBrush();
    walkBrush_.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));

    reset();
  } // PathOverlay
	
  private Paint createBrush()
  {
    final Paint brush = new Paint();
    
    brush.setColor(ROUTE_COLOUR);
    brush.setStrokeWidth(2.0f);
    brush.setStyle(Paint.Style.STROKE);
    brush.setStrokeWidth(10.0f);
    
    return brush;
  } // createBrush	

  public void setRoute(final List<Segment> routeSegments)
  {
    reset();
    route_ = routeSegments;
  } // setRoute

  public void reset() 
  {
    ridePath_ = null;
    route_ = null;
  } // clearPath

  @Override
  protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
  {
    if (shadow) 
      return;
 
    if (route_ == null || route_.size() < 2) 
      return;
		
    if(zoomLevel_ != mapView.getZoomLevel() && !mapView.isAnimating())
    {
      ridePath_ = null;
      zoomLevel_ = mapView.getProjection().getZoomLevel();
    } // if ... 
  
    if(ridePath_ == null)
    {
      ridePath_ = drawSegments(mapView.getProjection(), route_, false);
      walkPath_ = drawSegments(mapView.getProjection(), route_, true);
    } // if ...

    canvas.drawPath(ridePath_, rideBrush_);
    canvas.drawPath(walkPath_, walkBrush_);
  } // draw
	
  private Path drawSegments(final Projection projection, final List<Segment> route,  final boolean walk)
  {
    final Path path = new Path();
    path.rewind();

    final List<Point> points = new ArrayList<Point>();
    Point screenPoint = new Point();
    for(Segment s : route)
    {
      if(s.walk() != walk)
        continue;
      
      points.clear();
      for(Iterator<GeoPoint> i = s.points(); i.hasNext(); )
      {
        final GeoPoint gp = i.next();
        final Point p = projection.toMapPixelsProjected(gp.getLatitudeE6(), gp.getLongitudeE6(), null);
        points.add(p);
      } // for ...
    
      screenPoint = projection.toMapPixelsTranslated(points.get(0), screenPoint);
      path.moveTo(screenPoint.x, screenPoint.y);
      
      for (int i = 0; i != points.size(); ++i) 
      {
        screenPoint = projection.toMapPixelsTranslated(points.get(i), screenPoint);
        path.lineTo(screenPoint.x, screenPoint.y);
      } // for ...
    } // for ...

    return path;
  } // drawSegments
} // Path
