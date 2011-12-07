package net.cyclestreets.planned;

import android.content.Context;
import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;
import net.cyclestreets.api.Journey;

public class ReplanRoutingTask 
  extends RoutingTask<Journey>
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
	protected RouteData doInBackground(Journey... params) 
	{
	  final Journey pr = params[0];
	  final RouteData rd = db_.route(pr.itinerary(), newPlan_);
	  if(rd != null)
		  return rd;

	  publishProgress(R.string.finding_route);
	  return fetchRoute(newPlan_, pr.itinerary(), pr.start(), pr.finish(), 0);
	} // doInBackground
} // class ReplanRoutingTask
