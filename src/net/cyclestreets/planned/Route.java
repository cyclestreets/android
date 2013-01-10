package net.cyclestreets.planned;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Segment;
import net.cyclestreets.api.Waypoints;

import android.content.Context;
import android.widget.Toast;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;
import net.cyclestreets.content.RouteSummary;
import net.cyclestreets.api.DistanceFormatter;

public class Route 
{
  public interface Listener {
    public void onNewJourney(final Journey journey, final Waypoints waypoints);
    public void onResetJourney();
  } // Listener

  static private class Listeners
  {
    private List<Listener> listeners_ = new ArrayList<Listener>();
  
    public void register(final Listener listener) 
    {
      if(!doRegister(listener))
        return;
    
      if((Route.journey() != Journey.NULL_JOURNEY) || (Route.waypoints() != Waypoints.NULL_WAYPOINTS))
        listener.onNewJourney(Route.journey(), Route.waypoints());
      else
        listener.onResetJourney();      
    } // registerListener
    
    public void softRegister(final Listener listener) 
    {
      doRegister(listener);
    } // softRegister
    
    private boolean doRegister(final Listener listener) 
    {
      if(listeners_.contains(listener))
        return false;
      listeners_.add(listener);
      return true;
    } // doRegister  
    public void unregister(final Listener listener) 
    {
      listeners_.remove(listener);
    } // unregisterListener
   
    public void onNewJourney(final Journey journey, final Waypoints waypoints) {
      for(final Listener l : listeners_)
        l.onNewJourney(journey, waypoints);
    } // onNewJourney
    
    public void onReset() {
      for(final Listener l : listeners_)
        l.onResetJourney();
    } // onReset
  } // Listeners
  
  static private final Listeners listeners_ = new Listeners();
  
  static public void registerListener(final Listener l) { listeners_.register(l); }
  static public void softRegisterListener(final Listener l) { listeners_.softRegister(l); }
  static public void unregisterListener(final Listener l) { listeners_.unregister(l); }
  
	static public void PlotRoute(final String plan,
              								 final int speed,
              								 final Context context,
                               final Waypoints waypoints)
	{
		final CycleStreetsRoutingTask query = new CycleStreetsRoutingTask(plan, speed, context);
		query.execute(waypoints);
	} // PlotRoute
	
	static public void FetchRoute(final String plan,
	                              final long itinerary,
	                              final int speed,
	                              final Context context)
	{
	  final FetchCycleStreetsRouteTask query = new FetchCycleStreetsRouteTask(plan, speed, context);
	  query.execute(itinerary);
	} // FetchRoute

	static public void RePlotRoute(final String plan,
              								   final Context context)
	{
		final ReplanRoutingTask query = new ReplanRoutingTask(plan, db_, context);
		query.execute(plannedRoute_);
	} // PlotRoute

	static public void PlotStoredRoute(final int localId,
								 final Context context)
	{
		final StoredRoutingTask query = new StoredRoutingTask(db_, context);
		query.execute(localId);
	} // PlotRoute
	
	static public void RenameRoute(final int localId, final String newName)
	{
		db_.renameRoute(localId, newName);
	} // RenameRoute
	
	static public void DeleteRoute(final int localId)
	{
		db_.deleteRoute(localId);
	} // DeleteRoute
	
	/////////////////////////////////////////	
	private static Journey plannedRoute_ = Journey.NULL_JOURNEY;
  private static Waypoints waypoints_ = plannedRoute_.waypoints();
	private static RouteDatabase db_;
	private static Context context_;

	static public void initialise(final Context context)
	{
		context_ = context;
		db_ = new RouteDatabase(context);
	} // initialise

	static public void setWaypoints(final Waypoints waypoints)
	{
	  waypoints_ = waypoints;
	} // setTerminals
	
	static public void resetJourney()
	{
		onNewJourney(null);
	} // resetJourney

	static public void onResume()
	{
		Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());
	} // onResult
	
	/////////////////////////////////////
	static public int storedCount()
	{
		return db_.routeCount();
	} // storedCount

	static public List<RouteSummary> storedRoutes()
	{
		return db_.savedRoutes();
	} // storedNames
	
	/////////////////////////////////////
  static public boolean onNewJourney(final RouteData route)
	{
		try {
			doOnNewJourney(route);
			return true;
		} // try
		catch(Exception e) {
		  Toast.makeText(context_, R.string.route_failed, Toast.LENGTH_LONG).show();
		}
		return false;
	} // onNewJourney
	
  static private void doOnNewJourney(final RouteData route)
		throws Exception
	{
		if(route == null)
		{
			plannedRoute_ = Journey.NULL_JOURNEY;
			waypoints_ = Waypoints.NULL_WAYPOINTS;
			listeners_.onReset();
			return;
		} // if ...
		
		plannedRoute_ = Journey.loadFromXml(route.xml(), route.points(), route.name());
		
		db_.saveRoute(plannedRoute_, route.xml());
		waypoints_ = plannedRoute_.waypoints();
    listeners_.onNewJourney(plannedRoute_, waypoints_);
	} // onNewJourney
	
	static public Waypoints waypoints() { return waypoints_; }
	
	static public boolean available() { return plannedRoute_ != Journey.NULL_JOURNEY; }
	static public Journey journey() { return plannedRoute_; }
	
	private Route() 
	{
		// don't create one of these
	} // Route
} // class Route
