package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;

public class Segment 
{
	private final String name_;
	private final String time_;
	private final int distance_;
	private final List<GeoPoint> points_;
	
	Segment(final String name,
			final int time,
			final int distance,
			final List<GeoPoint> points)
	{	
		name_ = name;
		time_ = time/60 + ":" + String.format("%02d", time%60);
		distance_ = distance;
		points_ = points;
	} // Segment
	
	public String street() { return name_; }
	public String time() { return time_; }
	public int distance() { return distance_; }
	Iterator<GeoPoint> points() { return points_.iterator(); }
} // class Segment
