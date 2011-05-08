package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

import android.content.Context;

class CycleStreetsRoutingTask extends RoutingTask<GeoPoint>
{
	/////////////////////////////////////////////////////
	private final String routeType_;
	private final int speed_;
	private int itinerary_;
			
	CycleStreetsRoutingTask(final String routeType,
				final int speed,
				final Route.Callback whoToTell,
				final Context context,
				final int itinerary) 
	{
		super(R.string.finding_route, whoToTell, context);
		routeType_ = routeType;
		speed_ = speed;
		itinerary_ = itinerary;
	} // NewRouteTask
	
	@Override
	protected RouteData doInBackground(GeoPoint... points)
	{
		final GeoPoint start = points[0];
		final GeoPoint finish = points[1];
		final String xml = fetchRoute(start, finish);
		return new RouteData(xml, start, finish);
	} // doInBackgroud
	
	protected String fetchRoute(final GeoPoint start, final GeoPoint finish) 
	{
		try {
	   		if(itinerary_ != -1)
	   			return ApiClient.getJourneyXml(routeType_, itinerary_, speed_);
	   		return ApiClient.getJourneyXml(routeType_, start, finish, speed_);
	   	} // try
	   	catch (Exception e) {
	   		throw new RuntimeException(e);
	   	} // catch
	} // doInBackground
} // NewRouteTask
