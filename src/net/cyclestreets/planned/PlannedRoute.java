package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Segment;

public class PlannedRoute 
{
	static public final PlannedRoute NULL_ROUTE;
	static {
		NULL_ROUTE = new PlannedRoute();
		NULL_ROUTE.activeSegment_ = -1;
	}
	
	static PlannedRoute load(final String journeyXml, 
    			                 final GeoPoint from, 
    			                 final GeoPoint to,
    			                 final String name) 
		throws Exception
	{
		final Journey journey = Journey.loadFromXml(journeyXml, from, to, name);
		if(journey.isEmpty())
			throw new RuntimeException();

    final PlannedRoute pr = new PlannedRoute();
    pr.segments_.addAll(journey.segments());
		return pr;
	} // PlannedRoute
	
	private final List<Segment> segments_;
	private int activeSegment_;
	
	private PlannedRoute() 
	{
		segments_ = new ArrayList<Segment>();
		activeSegment_ = 0;		
	} // PlannedRoute
	
	/////////////////////////////////////////
	private Segment.Start s() { return (Segment.Start)segments_.get(0); }
	private Segment.End e() { return (Segment.End)segments_.get(segments_.size()-1); }
	
	public GeoPoint start() { return s().start(); }
	public GeoPoint finish() { return e().end(); }
	
	public String url() { return "http://cycle.st/j" + itinerary(); }
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
