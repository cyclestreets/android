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
import android.graphics.Rect;

public class PathOfRouteOverlay extends Overlay 
{
  static public int ROUTE_COLOUR = 0x80ff00ff;

  private List<Segment> route_;

	private Paint brush_;

	private int zoomLevel_ = -1;
	private Path path_;

	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

	// bounding rectangle for the current line segment.
	private final Rect mLineBounds = new Rect();

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
		
		Point screenPoint0 = null;
		Point screenPoint1 = null;
		Point projectedPoint0; // points from the points list
		Point projectedPoint1;

		path_ = new Path();
		path_.rewind();
		final int size = points.size();
		projectedPoint0 = points.get(size - 1);

		for (int i = size - 2; i >= 0; i--) 
		{
			// compute next points
			projectedPoint1 = points.get(i);

			// the starting point may be not calculated, because previous segment was out of clip
			// bounds
			if (screenPoint0 == null) 
			{
				screenPoint0 = projection.toMapPixelsTranslated(projectedPoint0, mTempPoint1);
				path_.moveTo(screenPoint0.x, screenPoint0.y);
			}

			screenPoint1 = projection.toMapPixelsTranslated(projectedPoint1, mTempPoint2);

			path_.lineTo(screenPoint1.x, screenPoint1.y);

			// update starting point to next position
			projectedPoint0 = projectedPoint1;
			screenPoint0.x = screenPoint1.x;
			screenPoint0.y = screenPoint1.y;
			mLineBounds.set(projectedPoint0.x, projectedPoint0.y, 
			                projectedPoint0.x, projectedPoint0.y);
		}

    canvas.drawPath(path_, brush_);
	} // draw
} // Path
