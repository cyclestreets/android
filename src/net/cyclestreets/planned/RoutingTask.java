package net.cyclestreets.planned;

import net.cyclestreets.content.RouteData;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class RoutingTask<Params> extends
		AsyncTask<Params, Integer, RouteData> 
{
	private final Route.Callback whoToTell_;
	private ProgressDialog progress_;
	private Context context_;

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
		context_ = context;

		progress_ = new ProgressDialog(context);
		progress_.setMessage(progressMessage);
		progress_.setIndeterminate(true);
		progress_.setCancelable(false);
	} // Routing Task
	
	protected void setMessage(int msgId)
	{
		progress_.setMessage(context_.getString(msgId));
	} // setMessage
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progress_.show();
	} // onPreExecute

	@Override
    protected void onPostExecute(final RouteData route) 
    {
   		Route.onNewJourney(route.xml(), route.start(), route.finish());
   		whoToTell_.onNewJourney();
       	progress_.dismiss();
	} // onPostExecute  
} // class RoutingTask
