package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Marker;

public class Route 
{
	//////////////////////////////////////////////////
	private static Journey journey_;
	private static GeoPoint from_;
	private static GeoPoint to_;

	static public Journey journey() { return journey_; }
	static public GeoPoint from() { return from_; }
	static public GeoPoint to() { return to_; }
	
	static public void resetJourney()
	{
		onNewJourney(null, null, null);
	} // resetJourney

	/////////////////////////////////////
	static private List<Segment> segments_ = new ArrayList<Segment>();
	
	static public void onNewJourney(final Journey journey, final GeoPoint from, final GeoPoint to)
	{
		journey_ = journey;
		from_ = from;
		to_ = to;
	
		segments_.clear();
		
		for (final Marker marker : journey.markers) {
			if (marker.type.equals("segment")) 
			{
				final Segment seg = new Segment(marker.name,
												marker.time,
												marker.distance,
												grabPoints(marker));
				segments_.add(seg);
			} // if ...
		} // for ...

	} // onNewJourney
	
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
				if(segments_.hasNext())
					points_ = segments_.next().points();
			
			return p;
		} // next

		@Override
		public void remove() 
		{
			throw new UnsupportedOperationException();
		} // remove
	} // class PointsIterator
} // class Route
