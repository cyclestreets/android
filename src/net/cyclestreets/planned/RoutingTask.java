package net.cyclestreets.planned;

import java.util.List;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.content.RouteData;
import net.cyclestreets.util.Dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import net.cyclestreets.api.Journey;

public abstract class RoutingTask<Params> 
    extends AsyncTask<Params, Integer, RouteData> 
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
                                 final int speed,
                                 final List<GeoPoint> waypoints) 
  {
    return fetchRoute(routeType, -1, speed, waypoints);
  } // fetchRoute
	
  protected RouteData fetchRoute(final String routeType,
                                 final long itinerary,
                                 final int speed)
  { 
    return fetchRoute(routeType, itinerary, speed, null);
  } // fetchRoute
	
  protected RouteData fetchRoute(final String routeType, 
                                 final long itinerary,
                                 final int speed,
                                 final List<GeoPoint> waypoints) 
  {
    try {
      final String xml = doFetchRoute(routeType, itinerary, speed, waypoints);
      return new RouteData(xml, waypoints, null);
    } // try
    catch (Exception e) {
      error_ = "Could not contact CycleStreets.net : " + e.getMessage();
      return null;
    } // catch
  } // fetchRoute
	
  private String doFetchRoute(final String routeType, 
                              final long itinerary,
                              final int speed,
                              final List<GeoPoint> waypoints)
    throws Exception
  {
    if(itinerary != -1)
      return Journey.getJourneyXml(routeType, itinerary);
    return Journey.getJourneyXml(routeType, speed, waypoints);
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
      if(Route.onNewJourney(route))
        whoToTell_.onNewJourney();
    } // if ...
    progress_.dismiss();
    if(error_ != null)
      Toast.makeText(context_, error_, Toast.LENGTH_LONG).show();
  } // onPostExecute  
} // class RoutingTask
