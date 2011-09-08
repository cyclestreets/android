package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

import android.content.Context;

class CycleStreetsRoutingTask extends RoutingTask<GeoPoint>
{
	/////////////////////////////////////////////////////
	private final String routeType_;
	private final int speed_;
			
	CycleStreetsRoutingTask(final String routeType,
				                  final int speed,
				                  final Route.Callback whoToTell,
				                  final Context context) 
	{
		super(R.string.finding_route, whoToTell, context);
		routeType_ = routeType;
		speed_ = speed;
	} // NewRouteTask
	
	@Override
	protected RouteData doInBackground(GeoPoint... points)
	{
		final GeoPoint start = points[0];
		final GeoPoint finish = points[1];
		return fetchRoute(routeType_, start, finish, speed_);
	} // doInBackgroud
} // NewRouteTask
