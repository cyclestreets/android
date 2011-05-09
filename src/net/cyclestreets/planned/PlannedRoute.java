package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Marker;
import net.cyclestreets.util.Collections;

public class PlannedRoute 
{
	static public final PlannedRoute NULL_ROUTE;
	static {
		NULL_ROUTE = new PlannedRoute();
		NULL_ROUTE.activeSegment_ = -1;
	}
	
	static PlannedRoute load(final String journeyXml, final GeoPoint from, final GeoPoint to) 
		throws Exception
	{
		final PlannedRoute pr = new PlannedRoute();
		
		final Journey journey = ApiClient.loadRaw(Journey.class, journeyXml);
		if(journey.markers.isEmpty())
			throw new RuntimeException();
				
		int total_time = 0;
		int total_distance = 0;
		
		Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());
		
		for (final Marker marker : journey.markers) 
		{
			if (marker.type.equals("segment")) 
			{
				total_time += marker.time;
				total_distance += marker.distance;
				final Segment seg = new Segment.Step(marker.name,
													 marker.turn,
													 (marker.walk == 1),
													 total_time,
													 marker.distance,
													 total_distance,
													 grabPoints(marker));
				pr.add(seg);
			} // if ...
		} // for ...

		for (final Marker marker : journey.markers) 
		{ 
			if(marker.type.equals("route"))			
			{
				final Segment startSeg = new Segment.Start(marker.itinerary,
														   marker.name, 
														   marker.plan, 
														   marker.speed,
														   total_time, 
														   total_distance, 
														   Collections.list(from, pr.segments_.get(0).start()));
				final Segment endSeg = new Segment.End(marker.finish, 
													   total_time, 
													   total_distance, 
													   Collections.list(pr.segments_.get(pr.segments_.size()-1).end(), to));
				pr.add(startSeg);
				pr.add(endSeg);
				break;
			} // if ... 
		} // for ...
		
		return pr;
	} // PlannedRoute
	
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
	
	private final List<Segment> segments_;
	private int activeSegment_;
	
	private PlannedRoute() 
	{
		segments_ = new ArrayList<Segment>();
		activeSegment_ = 0;
	} // PlannedRoute
	
	private void add(final Segment segment)
	{
		if(segment instanceof Segment.Start)
			segments_.add(0, segment);
		else
			segments_.add(segment);
	} // add
	
	/////////////////////////////////////////
	private Segment.Start s() { return (Segment.Start)segments_.get(0); }
	private Segment.End e() { return (Segment.End)segments_.get(segments_.size()-1); }
	
	public GeoPoint start() { return s().start(); }
	public GeoPoint finish() { return e().end(); }
	
	public int itinerary() { return s().itinerary(); }
	public String name() { return s().name(); }
	public String plan() { return s().plan(); }
	public int speed() { return s().speed(); }
	public int total_distance() { return e().total_distance(); }
	
	/////////////////////////////////////////
	public void setActiveSegmentIndex(int index) { activeSegment_ = index; }
	public int activeSegmentIndex() { return activeSegment_; }
	
	public Segment activeSegment() { return activeSegment_ >= 0 ? segments_.get(activeSegment_) : null; }
	
	public boolean atStart() { return activeSegment_ <= 0; }
	public boolean atEnd() { return activeSegment_ == segments_.size()-1; }
	
	public void regressActiveSegment() 
	{ 
		if(!atStart()) 
			--activeSegment_; 
	} // regressActiveSegment
	public void advanceActiveSegment() 
	{ 
		if(!atEnd()) 
			++activeSegment_; 
	} // advanceActiveSegment
	
	public List<Segment> segments()
	{
		return segments_;
	} // segments
	
	public Iterator<GeoPoint> points()
	{
		return new PointsIterator(segments_);
	} // points
		

	
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
} // PlannedRoute
