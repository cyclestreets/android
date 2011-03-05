package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.content.RouteDatabase;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class StoredRoutingTask extends AsyncTask<Integer, Integer, String> 
{
	private final Route.Callback whoToTell_;
	private final RouteDatabase db_;
	private ProgressDialog progress_;

	StoredRoutingTask(final Route.Callback whoToTell,
					  final Context context) 
	{
		whoToTell_ = whoToTell;
		db_ = new RouteDatabase(context);

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
	protected String doInBackground(Integer... params) 
	{
		return db_.route(params[0]);
	} // doInBackground

	@Override
    protected void onPostExecute(final String journey) 
    {
       	progress_.dismiss();
   		Route.onNewJourney(journey, null, null);
   		whoToTell_.onNewJourney();
	} // onPostExecute  
}
