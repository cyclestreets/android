package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;

import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Segment;

import org.osmdroid.util.GeoPoint;

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
	public interface Callback {
		public void onNewJourney();
	}

	@SuppressWarnings("unchecked")
	static public void PlotRoute(final String plan,
              								 final int speed,
              								 final Callback whoToTell,
              								 final Context context,
                               final List<GeoPoint> waypoints)
	{
		final CycleStreetsRoutingTask query = new CycleStreetsRoutingTask(plan, speed, whoToTell, context);
		query.execute(waypoints);
	} // PlotRoute
	
	static public void FetchRoute(final String plan,
	                              final long itinerary,
	                              final int speed,
	                              final Callback whoToTell,
	                              final Context context)
	{
	  final FetchCycleStreetsRouteTask query = new FetchCycleStreetsRouteTask(plan, speed, whoToTell, context);
	  query.execute(itinerary);
	} // FetchRoute

	static public void RePlotRoute(final String plan,
              								   final Callback whoToTell,
              								   final Context context)
	{
		final ReplanRoutingTask query = new ReplanRoutingTask(plan, db_, whoToTell, context);
		query.execute(plannedRoute_);
	} // PlotRoute

	static public void PlotStoredRoute(final int localId,
								 final Callback whoToTell,
								 final Context context)
	{
		final StoredRoutingTask query = new StoredRoutingTask(db_, whoToTell, context);
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
	private static GeoPoint start_;
	private static GeoPoint finish_;
	private static RouteDatabase db_;
	private static Context context_;

	static public void initialise(final Context context)
	{
		context_ = context;
		db_ = new RouteDatabase(context);
	} // initialise

	static public void setTerminals(final GeoPoint start, final GeoPoint finish)
	{
		start_ = start;
		finish_ = finish;
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
			start_ = finish_ = null;
			return;
		}
		
    start_ = route.points().get(0);
    finish_ = route.points().get(route.points().size()-1);
  
		plannedRoute_ = Journey.loadFromXml(route.xml(), start_, finish_, route.name());
		
		db_.saveRoute(plannedRoute_.itinerary(), 
      					  plannedRoute_.name(), 
      					  plannedRoute_.plan(),
      					  plannedRoute_.total_distance(),
      					  route.xml(), 
      					  plannedRoute_.start(), 
      					  plannedRoute_.finish());
		
		if(start_ == null)
		  start_ = plannedRoute_.start();
		if(finish_ == null)
		  finish_ = plannedRoute_.finish();
	} // onNewJourney
	
	static public GeoPoint start() { return start_; }
	static public GeoPoint finish() { return finish_; }
	
	static public int itinerary() { return planned().itinerary(); }
	static public boolean available() { return plannedRoute_ != Journey.NULL_JOURNEY; }
	static public Journey planned() { return plannedRoute_; }
	static public Segment activeSegment() { return planned().activeSegment(); }
	static public int activeSegmentIndex() { return planned().activeSegmentIndex(); }
	static public void setActiveSegmentIndex(int index) { planned().setActiveSegmentIndex(index); }
	static public void advanceActiveSegment() { planned().advanceActiveSegment(); }
	static public void regressActiveSegment() { planned().regressActiveSegment(); }
	
	static public List<Segment> segments() { return planned().segments(); }
	static public Iterator<GeoPoint> points() { return planned().points(); }
	
	static public boolean atStart() { return planned().atStart(); }
	static public boolean atEnd() { return planned().atEnd(); }
	
	private Route() 
	{
		// don't create one of these
	} // Route
} // class Route
