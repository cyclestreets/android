package net.cyclestreets.routing;

import net.cyclestreets.view.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;

import android.content.Context;

public class StoredRoutingTask extends RoutingTask<Integer>
{
	private final RouteDatabase db_;

	StoredRoutingTask(final RouteDatabase db,
	                  final Context context)
	{
		super(R.string.route_loading, context);
		db_ = db;
	} // StoredRoutingTask

	@Override
	protected RouteData doInBackground(Integer... params)
	{
		return db_.route(params[0]);
	} // doInBackground
} // StoredRoutingTask
