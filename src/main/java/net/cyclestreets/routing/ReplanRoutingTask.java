package net.cyclestreets.routing;

import android.content.Context;
import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;

public class ReplanRoutingTask 
  extends RoutingTask<Journey>
{
	private final RouteDatabase db_;
	private final String newPlan_;

	ReplanRoutingTask(final String newPlan,
	                  final RouteDatabase db,
	                  final Context context) 
	{
		super(R.string.loading_route, context);
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
	  return fetchRoute(newPlan_, pr.itinerary(), 0, pr.waypoints());
	} // doInBackground
} // class ReplanRoutingTask
