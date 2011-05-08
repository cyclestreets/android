package net.cyclestreets.planned;

import org.osmdroid.util.GeoPoint;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class RoutingTask<Params, Result> extends
		AsyncTask<Params, Integer, Result> 
{
	private final Route.Callback whoToTell_;
	private ProgressDialog progress_;

	protected RoutingTask(final int progressMessageId,
						  final Route.Callback whoToTell,
						  final Context context)
	{
		this(context.getString(progressMessageId), whoToTell, context);
	} // RoutingTask
			
	protected RoutingTask(final String progressMessage,
						  final Route.Callback whoToTell,
						  final Context context)
	{
		whoToTell_ = whoToTell;

		progress_ = new ProgressDialog(context);
		progress_.setMessage(progressMessage);
		progress_.setIndeterminate(true);
		progress_.setCancelable(false);
	} // Routing Task
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progress_.show();
	} // onPreExecute

    protected void postExecuteNotify(final String xml, 
    								 final GeoPoint start,
    								 final GeoPoint finish) 
    {
   		Route.onNewJourney(xml, start, finish);
   		whoToTell_.onNewJourney();
       	progress_.dismiss();
	} // onPostExecute  
} // class RoutingTask
