package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;

import android.content.Context;

public class StoredRoutingTask extends RoutingTask<Integer> 
{
	private final RouteDatabase db_;

	StoredRoutingTask(final RouteDatabase db,
					  final Route.Callback whoToTell,
					  final Context context) 
	{
		super(R.string.loading_route, whoToTell, context);
		db_ = db;
	} // NewRouteTask

	@Override
	protected RouteData doInBackground(Integer... params) 
	{
		return db_.route(params[0]);
	} // doInBackground
} // StoredRoutingTask
