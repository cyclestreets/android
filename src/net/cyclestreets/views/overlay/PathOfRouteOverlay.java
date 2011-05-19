package net.cyclestreets.views.overlay;

import java.util.Iterator;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.Context;

public class PathOfRouteOverlay extends PathOverlay 
{
	static public int ROUTE_COLOUR = 0x80ff00ff;
	
	public PathOfRouteOverlay(final Context context)
	{
		super(ROUTE_COLOUR, context);
		mPaint.setStrokeWidth(10.0f);
	} // MapActivityPathOverlay
	
	public void setRoute(final Iterator<GeoPoint> points)
	{
		clearPath();
		while(points.hasNext())
			addPoint(points.next());
	} // setRoute
} // class PathOfRouteOverlay
