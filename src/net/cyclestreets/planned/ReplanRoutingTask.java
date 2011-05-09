package net.cyclestreets.planned;

import android.content.Context;
import net.cyclestreets.R;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;

public class ReplanRoutingTask extends RoutingTask<PlannedRoute>
{
	private final RouteDatabase db_;
	private final String newPlan_;

	ReplanRoutingTask(final String newPlan,
					  final RouteDatabase db,
					  final Route.Callback whoToTell,
					  final Context context) 
	{
		super(R.string.loading_route, whoToTell, context);
		db_ = db;
		newPlan_ = newPlan;
	} // ReplanRouteTask

	@Override
	protected RouteData doInBackground(PlannedRoute... params) 
	{
		try {
		  final PlannedRoute pr = params[0];
		  final RouteData rd = db_.route(pr.itinerary(), newPlan_);
		  if(rd != null)
			return rd;

		  setMessage(R.string.finding_route);
		  // otherwise go to cyclestreets
		  final String xml = ApiClient.getJourneyXml(newPlan_, pr.itinerary(), pr.speed());
		  return new RouteData(xml, pr.start(), pr.finish());
	   	} // try
	   	catch (Exception e) {
	   		throw new RuntimeException(e);
	   	} // catch

	} // doInBackground
} // class ReplanRoutingTask
