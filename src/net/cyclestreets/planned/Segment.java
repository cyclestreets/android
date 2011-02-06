package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;

public abstract class Segment 
{
	private final String name_;
	private final String turn_;
	private final String running_time_;
	private final int distance_;
	private final int running_distance_;
	private final List<GeoPoint> points_;
	
	Segment(final String name,
			final String turn,
			final int time,
			final int distance,
			final int running_distance,
			final List<GeoPoint> points)
	{	
		name_ = name;
		turn_ = turn.length() != 0 ? turn.substring(0,1).toUpperCase() + turn.substring(1) : turn;
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
	
	public String toString() 
	{
		if(turn_.length() == 0)
			return street();
		return turn() + " into " + street();
	} // toString
	
	public GeoPoint start() { return points_.get(0); }
	public GeoPoint end() { return points_.get(points_.size()-1); }
	
	public String street() { return name_; }
	public String turn() { return turn_; }
	public String runningTime() { return running_time_; }
	public int distance() { return distance_; }
	public int runningDistance() { return running_distance_; }
	public Iterator<GeoPoint> points() { return points_.iterator(); }

	static public class Start extends Segment 
	{
		Start(final String journey, final List<GeoPoint> points)
		{
			super(journey, "", 0, 0, 0, points);
		} // Start
	} // class Start
	
	static public class End extends Segment
	{
		End(final String destination, 
			final int total_time, 
			final int total_distance, 
			final List<GeoPoint> points)	
		{
			super("Destination " + destination, "", total_time, 0, total_distance, points);
		} // End
	} // End
	
	static public class Journey extends Segment
	{
		Journey(final String name,
				final String turn,
				final int time,
				final int distance,
				final int running_distance,
				final List<GeoPoint> points)
		{
			super(name, 
				  turn.length() != 0 ? turn.substring(0,1).toUpperCase() + turn.substring(1) : turn,
				  time,
				  distance,
				  running_distance,
				  points);
		} // JourneySegment
	} // class Journey
} // class Segment
