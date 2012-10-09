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
  private final String initialMsg_;
  private ProgressDialog progress_;
  private Context context_;
  private String error_;
  
  protected RoutingTask(final int progressMessageId,
                        final Context context)
  {
    this(context.getString(progressMessageId), context);
  } // RoutingTask
			
  protected RoutingTask(final String progressMessage,
                        final Context context)
  {
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
    try {
      progress_ = Dialog.createProgressDialog(context_, initialMsg_);
      progress_.show();
    }
    catch(Exception e) {
      progress_ = null;
    }
  } // onPreExecute
	
  @Override
  protected void onProgressUpdate(final Integer... p)
  {
    if(progress_ == null)
      return;
    progress_.setMessage(context_.getString(p[0]));
  } // onProgressUpdate

  @Override
  protected void onPostExecute(final RouteData route) 
  {
    if(route != null)
      Route.onNewJourney(route);
    progressDismiss();
    if(error_ != null)
      Toast.makeText(context_, error_, Toast.LENGTH_LONG).show();
  } // onPostExecute  
  
  private void progressDismiss()
  {
    if(progress_ == null)
      return;
    try {
      // some devices, in rare situations, can throw here so just catch and swallow
      progress_.dismiss();
    }
    catch(Exception e) {
    } // catch
  } // progressDismiss
} // class RoutingTask
