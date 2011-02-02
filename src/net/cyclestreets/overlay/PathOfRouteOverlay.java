package net.cyclestreets.overlay;

import java.util.Iterator;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

public class PathOfRouteOverlay extends PathOverlay {
	static public int ROUTE_COLOUR = 0x80ff0000;
	static public int HIGHLIGHT_COLOUR = 0x8000ff00;
	
	public PathOfRouteOverlay(final ResourceProxy resProxy)
	{
		this(ROUTE_COLOUR, resProxy);
	} // MapActivityPathOverlay
	
	public PathOfRouteOverlay(final int colour, final ResourceProxy resProxy)
	{
		super(colour, resProxy);
	    mPaint.setStrokeWidth(6.0f);
	} // PathofRouteOverlay
	
	public void setRoute(final Iterator<GeoPoint> points)
	{
		clearPath();
		while(points.hasNext())
			addPoint(points.next());
	} // setRoute
} // class PathOfRouteOverlay
