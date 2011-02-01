package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;

public class Segment 
{
	private final String name_;
	private final String running_time_;
	private final int distance_;
	private final int running_distance_;
	private final List<GeoPoint> points_;
	
	Segment(final String name,
			final int time,
			final int distance,
			final int running_distance,
			final List<GeoPoint> points)
	{	
		name_ = name;
		running_time_ = formatTime(time);
		distance_ = distance;
		running_distance_ = running_distance;
		points_ = points;
	} // Segment
	
	static private String formatTime(int time)
	{
		int hours = time/3600;
		int remainder = time%3600;
		int minutes = remainder/60;
		int seconds = time%60;
		
		if(hours == 0)
			return String.format("%d:%02d", minutes, seconds);
		
		return String.format("%d:%02d:%02d", hours, minutes, seconds);
	} // formatTime
	
	public String street() { return name_; }
	public String runningTime() { return running_time_; }
	public int distance() { return distance_; }
	public int runningDistance() { return running_distance_; }
	Iterator<GeoPoint> points() { return points_.iterator(); }
} // class Segment
