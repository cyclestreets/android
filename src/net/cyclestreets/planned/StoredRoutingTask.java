package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class StoredRoutingTask extends AsyncTask<Integer, Integer, RouteData> 
{
	private final Route.Callback whoToTell_;
	private final RouteDatabase db_;
	private ProgressDialog progress_;

	StoredRoutingTask(final RouteDatabase db,
					  final Route.Callback whoToTell,
					  final Context context) 
	{
		whoToTell_ = whoToTell;
		db_ = db;

		progress_ = new ProgressDialog(context);
		progress_.setMessage(context.getString(R.string.loading_route));
		progress_.setIndeterminate(true);
		progress_.setCancelable(false);
	} // NewRouteTask

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progress_.show();
	} // onPreExecute

	@Override
	protected RouteData doInBackground(Integer... params) 
	{
		return db_.route(params[0]);
	} // doInBackground

	@Override
    protected void onPostExecute(final RouteData route) 
    {
       	progress_.dismiss();
   		Route.onNewJourney(route.xml(), route.start(), route.end());
   		whoToTell_.onNewJourney();
	} // onPostExecute  
}
