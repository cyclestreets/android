package net.cyclestreets;

import net.cyclestreets.api.Journey;

import org.andnav.osm.util.GeoPoint;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class RoutingTask extends AsyncTask<GeoPoint, Integer, Journey>
{
	public interface Callback {
		public void onNewJourney();
	}

	static public void PlotRoute(final String routeType,
								 final GeoPoint placeFrom, 
								 final GeoPoint placeTo,
								 final Callback whoToTell,
								 final Context context)
	{
		final RoutingTask query = new RoutingTask(routeType, whoToTell, context);
		query.execute(placeFrom, placeTo);
	} // GetRoute
	
	/////////////////////////////////////////////////////
	private final String routeType_;
	private final Callback whoToTell_;
	private final Context context_;
	private ProgressDialog progress_;
			
	private	RoutingTask(final String routeType,
						 final Callback whoToTell,
						 final Context context) {
		routeType_ = routeType;
		whoToTell_ = whoToTell;
		context_ = context;

		progress_ = new ProgressDialog(context);
		progress_.setMessage(context.getString(R.string.finding_route));
		progress_.setIndeterminate(true);
		progress_.setCancelable(false);
	} // NewRouteTask
			
	protected void onPreExecute() {
		super.onPreExecute();
		progress_.show();
	} // onPreExecute

	protected Journey doInBackground(GeoPoint... points) {
	   	try {
	   		return CycleStreets.apiClient.getJourney(routeType_, points[0], points[1]);
	   	}
	   	catch (Exception e) {
	   		throw new RuntimeException(e);
	   	}
	} // doInBackground

    protected void onPostExecute(final Journey journey) {
       	progress_.dismiss();

       	if (journey.markers.isEmpty()) {
    		// TODO: No route - something went wrong!
       		Toast.makeText(context_, R.string.route_failed, Toast.LENGTH_SHORT).show();
       		return;
    	}

   		CycleStreets.journey = journey;
   		whoToTell_.onNewJourney();
	} // onPostExecute  
} // NewRouteTask
