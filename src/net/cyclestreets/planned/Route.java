package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.widget.Toast;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.R;
import net.cyclestreets.content.RouteDatabase;
import net.cyclestreets.content.RouteSummary;

public class Route 
{
	public interface Callback {
		public void onNewJourney();
	}

	static public void PlotRoute(final String plan,
								 final GeoPoint placeFrom, 
								 final GeoPoint placeTo,
								 final int speed,
								 final Callback whoToTell,
								 final Context context)
	{
		final CycleStreetsRoutingTask query = new CycleStreetsRoutingTask(plan, speed, whoToTell, context, -1);
		query.execute(placeFrom, placeTo);
	} // PlotRoute

	static public void RePlotRoute(final String plan,
								   final Callback whoToTell,
								   final Context context)
	{
		final ReplanRoutingTask query = new ReplanRoutingTask(plan, db_, whoToTell, context);
		query.execute(plannedRoute_);
	} // PlotRoute

	static public void PlotRoute(final int localId,
								 final Callback whoToTell,
								 final Context context)
	{
		final StoredRoutingTask query = new StoredRoutingTask(db_, whoToTell, context);
		query.execute(localId);
	} // PlotRoute
	
	static public void DeleteRoute(final int localId)
	{
		db_.deleteRoute(localId);
	} // DeleteRoute
	
	/////////////////////////////////////////	
	private static PlannedRoute plannedRoute_ = PlannedRoute.NULL_ROUTE;
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
		onNewJourney(null, null, null);
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
	static public void onNewJourney(final String journeyXml, final GeoPoint from, final GeoPoint to)
	{
		try {
			doOnNewJourney(journeyXml, from, to);
		} // try
		catch(Exception e) {
       		Toast.makeText(context_, R.string.route_failed, Toast.LENGTH_SHORT).show();
		}
	} // onNewJourney
	
	static private void doOnNewJourney(final String journeyXml, 
									   final GeoPoint start, 
									   final GeoPoint finish)
		throws Exception
	{
		start_ = start;
		finish_ = finish;
	
		if(journeyXml == null)
		{
			plannedRoute_ = PlannedRoute.NULL_ROUTE;
			return;
		}
		
		plannedRoute_ = PlannedRoute.load(journeyXml, start, finish);
		
		db_.saveRoute(plannedRoute_.itinerary(), 
					  plannedRoute_.name(), 
					  plannedRoute_.plan(),
					  plannedRoute_.total_distance(),
					  journeyXml, 
					  plannedRoute_.start(), 
					  plannedRoute_.finish());
	} // onNewJourney
	
	static public GeoPoint start() { return start_; }
	static public GeoPoint finish() { return finish_; }
	
	static public int itinerary() { return planned().itinerary(); }
	static public boolean available() { return plannedRoute_ != PlannedRoute.NULL_ROUTE; }
	static public PlannedRoute planned() { return plannedRoute_; }
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
