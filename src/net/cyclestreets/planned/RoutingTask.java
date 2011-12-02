package net.cyclestreets.planned;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.content.RouteData;
import net.cyclestreets.util.Dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import net.cyclestreets.api.Journey;

public abstract class RoutingTask<Params> 
  extends	AsyncTask<Params, Integer, RouteData> 
{
	private final Route.Callback whoToTell_;
	private final String initialMsg_;
	private ProgressDialog progress_;
	private Context context_;
	private String error_;

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

	protected RouteData fetchRoute(final String routeType, 
	                               final GeoPoint start, 
	                               final GeoPoint finish,
	                               final int speed) 
	{
	  return fetchRoute(routeType, -1, start, finish, speed);
	} // fetchRoute
	
	protected RouteData fetchRoute(final String routeType,
	                               final long itinerary,
	                               final int speed)
	{ 
	  return fetchRoute(routeType, itinerary, null, null, speed);
	} // fetchRoute
	
	protected RouteData fetchRoute(final String routeType, 
								                 final long itinerary,
								                 final GeoPoint start, 
								                 final GeoPoint finish,
								                 final int speed) 
	{
		try {
		  final String xml = doFetchRoute(routeType, itinerary, start, finish, speed);
		  return new RouteData(xml, start, finish, null);
		} // try
	  catch (Exception e) {
	    error_ = "Could not contact CycleStreets.net : " + e.getMessage();
	    return null;
	  } // catch
	} // fetchRoute
	
	private String doFetchRoute(final String routeType, 
								              final long itinerary,
								              final GeoPoint start, 
								              final GeoPoint finish,
								              final int speed)
		throws Exception
	{
		if(itinerary != -1)
   			return Journey.getJourneyXml(routeType, itinerary);
		return Journey.getJourneyXml(routeType, start, finish, speed);
	} // doFetchRoute
	
	@Override
	protected void onPreExecute() 
	{
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
		if(route != null)
		{
			if(Route.onNewJourney(route.xml(), route.start(), route.finish(), route.name()))
				whoToTell_.onNewJourney();
		} // if ...
		progress_.dismiss();
		if(error_ != null)
		  Toast.makeText(context_, error_, Toast.LENGTH_LONG).show();
	} // onPostExecute  
} // class RoutingTask
