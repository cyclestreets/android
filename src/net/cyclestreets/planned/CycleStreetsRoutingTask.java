package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

import android.content.Context;

class CycleStreetsRoutingTask extends RoutingTask<GeoPoint, String>
{
	/////////////////////////////////////////////////////
	private final String routeType_;
	private final int speed_;
	private GeoPoint from_;
	private GeoPoint to_;
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
	protected String doInBackground(GeoPoint... points) {
	   	try {
	   		from_ = points[0];
	   		to_ = points[1];
	   		
	   		if(itinerary_ != -1)
	   			return ApiClient.getJourneyXml(routeType_, itinerary_, speed_);
	   		return ApiClient.getJourneyXml(routeType_, from_, to_, speed_);
	   	}
	   	catch (Exception e) {
	   		throw new RuntimeException(e);
	   	}
	} // doInBackground

	@Override
    protected void onPostExecute(final String journey) 
	{
   		postExecuteNotify(journey, from_, to_);
	} // onPostExecute  
} // NewRouteTask
