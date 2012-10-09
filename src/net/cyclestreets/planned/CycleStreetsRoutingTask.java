package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;

import org.osmdroid.util.GeoPoint;

import android.content.Context;

import java.util.List;

class CycleStreetsRoutingTask extends RoutingTask<List<GeoPoint>>
{
	/////////////////////////////////////////////////////
	private final String routeType_;
	private final int speed_;
			
	CycleStreetsRoutingTask(final String routeType,
				                  final int speed,
				                  final Context context) 
	{
	  super(R.string.finding_route, context);
		routeType_ = routeType;
		speed_ = speed;
	} // NewRouteTask
	
	@Override
	protected RouteData doInBackground(List<GeoPoint>... waypoints)
	{
	  final List<GeoPoint> wp = waypoints[0];
		return fetchRoute(routeType_, speed_, wp);
	} // doInBackgroud
} // NewRouteTask
