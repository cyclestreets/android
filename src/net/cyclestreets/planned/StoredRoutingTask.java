package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;

import android.content.Context;

public class StoredRoutingTask extends RoutingTask<Integer, RouteData> 
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

	@Override
    protected void onPostExecute(final RouteData route) 
    {
       	postExecuteNotify(route.xml(), route.start(), route.end());
	} // onPostExecute  
} // StoredRoutingTask
