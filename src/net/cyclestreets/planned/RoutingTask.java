package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

class RoutingTask extends AsyncTask<GeoPoint, Integer, String>
{
	/////////////////////////////////////////////////////
	private final String routeType_;
	private final int speed_;
	private final Route.Callback whoToTell_;
	private GeoPoint from_;
	private GeoPoint to_;
	private ProgressDialog progress_;
			
	RoutingTask(final String routeType,
				final int speed,
				final Route.Callback whoToTell,
				final Context context) 
	{
		routeType_ = routeType;
		speed_ = speed;
		whoToTell_ = whoToTell;

		progress_ = new ProgressDialog(context);
		progress_.setMessage(context.getString(R.string.finding_route));
		progress_.setIndeterminate(true);
		progress_.setCancelable(false);
	} // NewRouteTask
			
	protected void onPreExecute() {
		super.onPreExecute();
		progress_.show();
	} // onPreExecute

	protected String doInBackground(GeoPoint... points) {
	   	try {
	   		from_ = points[0];
	   		to_ = points[1];
	   		return ApiClient.getJourneyXml(routeType_, from_, to_, speed_);
	   	}
	   	catch (Exception e) {
	   		throw new RuntimeException(e);
	   	}
	} // doInBackground

    protected void onPostExecute(final String journey) {
       	progress_.dismiss();

   		Route.onNewJourney(journey, from_, to_);
   		whoToTell_.onNewJourney();
	} // onPostExecute  
} // NewRouteTask
