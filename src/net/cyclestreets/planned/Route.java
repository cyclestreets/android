package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.widget.Toast;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.R;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Marker;
import net.cyclestreets.content.RouteDatabase;

public class Route 
{
	private static Journey journey_;
	private static GeoPoint from_;
	private static GeoPoint to_;
	private static int activeSegment_ = -1;
	private static RouteDatabase db_;
	private static Context context_;

	static public void initialise(final Context context)
	{
		context_ = context;
		db_ = new RouteDatabase(context);
	} // initialise

	static public void setTerminals(final GeoPoint from, final GeoPoint to)
	{
		from_ = from;
		to_ = to;
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
	static private List<Segment> segments_ = new ArrayList<Segment>();
	
	static public void onNewJourney(final String journeyXml, final GeoPoint from, final GeoPoint to)
	{
		try {
			doOnNewJourney(journeyXml, from, to);
		} // try
		catch(Exception e) {
       		Toast.makeText(context_, R.string.route_failed, Toast.LENGTH_SHORT).show();
		}
	} // onNewJourney
	
	static private void doOnNewJourney(final String journeyXml, final GeoPoint from, final GeoPoint to)
		throws Exception
	{
		from_ = from;
		to_ = to;
	
		segments_.clear();
		activeSegment_ = -1;
		
		if(journeyXml == null)
			return;
		
		journey_ = ApiClient.loadRaw(Journey.class, journeyXml);
		if(journey_.markers.isEmpty())
			throw new RuntimeException();
				
		int total_time = 0;
		int total_distance = 0;
		
		Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());
		
		for (final Marker marker : journey_.markers) 
		{
			if (marker.type.equals("segment")) 
			{
				total_time += marker.time;
				total_distance += marker.distance;
				final Segment seg = new Segment.Journey(marker.name,
													    marker.turn,
													    (marker.walk == 1),
													    total_time,
													    marker.distance,
													    total_distance,
													    grabPoints(marker));
				segments_.add(seg);
			} // if ...
		} // for ...

		for (final Marker marker : journey_.markers) 
		{ 
			if(marker.type.equals("route"))			
			{
				final Segment startSeg = new Segment.Start(marker.name, marker.plan, total_distance, makeList(from_, segments_.get(0).start()));
				final Segment endSeg = new Segment.End(marker.finish, total_time, total_distance, makeList(segments_.get(segments_.size()-1).end(), to_));
				segments_.add(0, startSeg);
				segments_.add(endSeg);
				
				db_.addRoute(marker.itinerary, marker.name, journeyXml);
				break;
			} // if ... 
		} // for ...
		
		activeSegment_ = 0;		
	} // onNewJourney
	
	static private List<GeoPoint> makeList(final GeoPoint g1, final GeoPoint g2)
	{
		final List<GeoPoint> l = new ArrayList<GeoPoint>();
		l.add(g1);
		l.add(g2);
		return l;
	} // makeList
	
	static public GeoPoint from() { return from_; }
	static public GeoPoint to() { return to_; }
	
	static public boolean available() { return journey_ != null; }
	static public void setActiveSegmentIndex(int index) { activeSegment_ = index; }
	static public int activeSegmentIndex() { return activeSegment_; }
	static public Segment activeSegment() { return activeSegment_ >= 0 ? segments_.get(activeSegment_) : null; }
	static public boolean atStart() { return activeSegment_ <= 0; }
	static public boolean atEnd() { return activeSegment_ == segments_.size()-1; }
	static public void regressActiveSegment() 
	{ 
		if(!atStart()) --activeSegment_; 
	} // regressActiveSegment
	static public void advanceActiveSegment() 
	{ 
		if(!atEnd()) 
			++activeSegment_; 
	} // advanceActiveSegment
	
	static public List<Segment> segments()
	{
		return segments_;
	} // segments
	
	static public Iterator<GeoPoint> points()
	{
		return new PointsIterator(segments_);
	} // points
		
	static private List<GeoPoint> grabPoints(final Marker marker)
	{
		final List<GeoPoint> points = new ArrayList<GeoPoint>();
		final String[] coords = marker.points.split(" ");
		for (final String coord : coords) 
		{
			final String[] yx = coord.split(",");
			final GeoPoint p = new GeoPoint(Double.parseDouble(yx[1]), Double.parseDouble(yx[0]));
			points.add(p);
		} // for ...
		return points;
	} // grabPoints
	
	private Route() 
	{
		// don't create one of these
	} // Route
	
	static class PointsIterator implements Iterator<GeoPoint>
	{
 		private final Iterator<Segment> segments_;
 		private Iterator<GeoPoint> points_;
 		
		PointsIterator(final List<Segment> segments)
		{
			segments_ = segments.iterator();
			if(!segments_.hasNext())
				return;
			
			points_ = segments_.next().points();
		} // PointsIterator
		
		@Override
		public boolean hasNext() 
		{
			return points_ != null && points_.hasNext();
		} // hasNext

		@Override
		public GeoPoint next() 
		{
			if(!hasNext())
				throw new IllegalStateException();
			
			final GeoPoint p = points_.next();
			
			if(!hasNext())
			{
				if(segments_.hasNext())
					points_ = segments_.next().points();
				else
					points_ = null;
			} // if ...
			
			return p;
		} // next

		@Override
		public void remove() 
		{
			throw new UnsupportedOperationException();
		} // remove
	} // class PointsIterator
} // class Route
