package net.cyclestreets.overlay;

import java.util.Iterator;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

public class PathOfRouteOverlay extends PathOverlay {
	static public int ROUTE_COLOUR = 0x80ff0000;
	
	public PathOfRouteOverlay(final ResourceProxy resProxy)
	{
		super(ROUTE_COLOUR, resProxy);
		mPaint.setStrokeWidth(6.0f);
	} // MapActivityPathOverlay
	
	public void setRoute(final Iterator<GeoPoint> points)
	{
		clearPath();
		while(points.hasNext())
			addPoint(points.next());
	} // setRoute
} // class PathOfRouteOverlay
