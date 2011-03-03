package net.cyclestreets.planned;

import net.cyclestreets.content.RouteDatabase;

import android.content.Context;
import android.os.AsyncTask;

public class StoredRoutingTask extends AsyncTask<Integer, Integer, String> 
{
	private final Route.Callback whoToTell_;
	private final RouteDatabase db_;
	StoredRoutingTask(final Route.Callback whoToTell,
					  final Context context) 
	{
		whoToTell_ = whoToTell;
		db_ = new RouteDatabase(context);
	} // NewRouteTask

	@Override
	protected String doInBackground(Integer... params) 
	{
		return db_.route(params[0]);
	} // doInBackground

    protected void onPostExecute(final String journey) 
    {
   		Route.onNewJourney(journey, null, null);
   		whoToTell_.onNewJourney();
	} // onPostExecute  
}
