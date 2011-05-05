package net.cyclestreets.planned;

import java.util.Iterator;
import java.util.List;

import net.cyclestreets.CycleStreetsPreferences;

import org.osmdroid.util.GeoPoint;

public abstract class Segment 
{
	private final String name_;
	private final String turn_;
	private final boolean walk_;
	private final String running_time_;
	private final int distance_;
	private final int running_distance_;
	private final List<GeoPoint> points_;

	static public DistanceFormatter formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());
	
	Segment(final String name,
			final String turn,
			final boolean walk,
			final int time,
			final int distance,
			final int running_distance,
			final List<GeoPoint> points,
			final boolean terminal)
	{	
		name_ = name;
		turn_ = initCap(turn);
		walk_ = walk;
		running_time_ = formatTime(time, terminal);
		distance_ = distance;
		running_distance_ = running_distance;
		points_ = points;
	} // Segment
	
	static protected String initCap(final String s)
	{
		return s.length() != 0 ? s.substring(0,1).toUpperCase() + s.substring(1) : s;
	} // initCap
	
	static private String formatTime(int time, boolean terminal)
	{
		if(time == 0)
			return "";
		
		int hours = time/3600;
		int remainder = time%3600;
		int minutes = remainder/60;
		int seconds = time%60;
		
		if(terminal)
			return formatTerminalTime(hours, minutes);
		
		if(hours == 0)
			return String.format("%d:%02d", minutes, seconds);
		
		return String.format("%d:%02d:%02d", hours, minutes, seconds);
	} // formatTime
	
	static private String formatTerminalTime(int hours, int minutes)
	{
		if(hours == 0)
			return String.format("%d minutes", minutes);
		String fraction = "";
		if(minutes > 52)
			++hours;
		else if(minutes > 37)
			fraction = "\u00BE";
		else if(minutes > 22)
			fraction = "\u00BD";
		else if(minutes > 7)
			fraction = "\u00BE";
		return String.format("%d%s hours", hours, fraction);
	} // formatTerminalTime
	
	public String toString() 
	{
		String s = turn() + " into " + street();
		if(walk())
			s += "\nPlease dismount and walk.";
		return s;
	} // toString
	
	public GeoPoint start() { return points_.get(0); }
	public GeoPoint end() { return points_.get(points_.size()-1); }
	
	public String street() { return name_; }
	public String turn() { return turn_; }
	public boolean walk() { return walk_; }
	public String runningTime() { return running_time_; }
	public String distance() { return formatter.distance(distance_); }
	public String runningDistance() { return formatter.total_distance(running_distance_); }
	public Iterator<GeoPoint> points() { return points_.iterator(); }

	static public class Start extends Segment 
	{
		private final String type_;
		
		Start(final String journey, 
			  final String type, 
			  final int total_time,
			  final int total_distance, 
			  final List<GeoPoint> points)
		{
			super(journey, "", false, total_time, 0, total_distance, points, true);
			type_ = initCap(type);
		} // Start
		
		public String toString() 
		{
			return street();
		} // toString
		
		public String street() 
		{
			return String.format("%s\n%s route %s\nJourney time %s", super.street(), type_, super.runningDistance(), super.runningTime());
		} // street
		public String distance() { return ""; }
		public String runningDistance() { return ""; }
		public String runningTime() { return ""; }
	} // class Start
	
	static public class End extends Segment
	{
		End(final String destination, 
			final int total_time, 
			final int total_distance, 
			final List<GeoPoint> points)	
		{
			super("Destination " + destination, "", false, total_time, 0, total_distance, points, true);
		} // End

		public String toString() { return street(); }
		public String distance() { return ""; }
	} // End
	
	static public class Journey extends Segment
	{
		Journey(final String name,
				final String turn,
				final boolean walk,
				final int time,
				final int distance,
				final int running_distance,
				final List<GeoPoint> points)
		{
			super(name, 
				  turn.length() != 0 ? turn.substring(0,1).toUpperCase() + turn.substring(1) : turn,
				  walk,
				  time,
				  distance,
				  running_distance,
				  points,
				  false);
		} // JourneySegment
	} // class Journey
} // class Segment
