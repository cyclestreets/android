package net.cyclestreets.planned;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

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
	  final List<GeoPoint> gp = new ArrayList<GeoPoint>();
	  gp.add(pr.start());
	  gp.add(pr.finish());
	  return fetchRoute(newPlan_, pr.itinerary(), 0, gp);
	} // doInBackground
} // class ReplanRoutingTask
