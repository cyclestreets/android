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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class PathOfRouteOverlay extends Overlay 
{
  static public int ROUTE_COLOUR = 0x80ff00ff;

  private List<Segment> route_;

	private Paint brush_;

	private int zoomLevel_ = -1;
	private Path path_;

	public PathOfRouteOverlay(final Context context) 
	{
		super(context);
		
		brush_ = new Paint();
		brush_.setColor(ROUTE_COLOUR);
		brush_.setStrokeWidth(2.0f);
		brush_.setStyle(Paint.Style.STROKE);
    brush_.setStrokeWidth(10.0f);

		clearPath();
	} // PathOverlay

  public void setRoute(final List<Segment> routeSegments)
  {
    clearPath();
    route_ = routeSegments;
  } // setRoute

  public void clearPath() 
  {
    path_ = null;
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
		  path_ = null;
		  zoomLevel_ = mapView.getProjection().getZoomLevel();
		} // if ... 
		
		if(path_ != null)
		{
	    canvas.drawPath(path_, brush_);
		  return;
		} // if ...

		final Projection projection = mapView.getProjection();
		final List<Point> points = new ArrayList<Point>();
		for(Segment s : route_)
      for(Iterator<GeoPoint> i = s.points(); i.hasNext(); )
      {
        final GeoPoint gp = i.next();
        final Point p = new Point(gp.getLatitudeE6(), gp.getLongitudeE6());
        projection.toMapPixelsProjected(p.x, p.y, p);
        points.add(p);
      } // for ...
		
		path_ = new Path();
		path_.rewind();

		final int size = points.size();
    Point screenPoint = new Point();
		for (int i = size - 2; i >= 0; i--) 
		{
			final Point projectedPoint = points.get(i);

			if (path_.isEmpty()) 
			{
				screenPoint = projection.toMapPixelsTranslated(projectedPoint, screenPoint);
				path_.moveTo(screenPoint.x, screenPoint.y);
			}

			screenPoint = projection.toMapPixelsTranslated(projectedPoint, screenPoint);
			path_.lineTo(screenPoint.x, screenPoint.y);
		} // for ...

    canvas.drawPath(path_, brush_);
	} // draw
} // Path
