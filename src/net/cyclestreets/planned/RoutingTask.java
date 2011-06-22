package net.cyclestreets.planned;

import net.cyclestreets.content.RouteData;
import net.cyclestreets.util.Dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class RoutingTask<Params> extends
		AsyncTask<Params, Integer, RouteData> 
{
	private final Route.Callback whoToTell_;
	private final String initialMsg_;
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
		initialMsg_ = progressMessage;
	} // Routing Task
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progress_ = Dialog.createProgressDialog(context_, initialMsg_);
		progress_.show();
	} // onPreExecute
	
	@Override
	protected void onProgressUpdate(final Integer... p)
	{
		progress_.setMessage(context_.getString(p[0]));
	} // onProgressUpdate

	@Override
    protected void onPostExecute(final RouteData route) 
    {
   		Route.onNewJourney(route.xml(), route.start(), route.finish());
   		whoToTell_.onNewJourney();
       	progress_.dismiss();
	} // onPostExecute  
} // class RoutingTask
