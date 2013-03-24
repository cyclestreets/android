package net.cyclestreets.routing;

import java.util.List;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.Collections;
import net.cyclestreets.util.GeoHelper;
import net.cyclestreets.util.IterableIterator;

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
      fraction = "\u00BC";
    return String.format("%d%s hours", hours, fraction);
  } // formatTerminalTime
  
  public String toString() 
  {
    String s = name_;
    if(turn_.length() != 0)
      s = turn_ + " into " + name_;
    if(walk())
      s += "\nPlease dismount and walk.";
    return s;
  } // toString
  
  public GeoPoint start() { return points_.get(0); }
  public GeoPoint finish() { return points_.get(points_.size()-1); }
      
  public int distanceFrom(final GeoPoint location)
  {
    int ct = crossTrack(location);
    int at = alongTrack(location);
    
    if(at < 0) // off end
      return Math.max(Math.abs(at), ct);
    return ct;
  } // distanceFrom
  
  public int crossTrack(final GeoPoint location) 
  {
    int minIndex = -1;
    int minDistance = Integer.MAX_VALUE;
    for(int p = 0; p != points_.size(); ++p) 
    {
      int distance = points_.get(p).distanceTo(location);
      if(distance > minDistance)
        continue;

      minDistance = distance;
      minIndex = p;
    } // for ...
    
    int ct0 = (minIndex != 0) ? crossTrack(minIndex - 1, location) : Integer.MAX_VALUE;
    int ct1 = (minIndex+1 != points_.size()) ? crossTrack(minIndex, location) : Integer.MAX_VALUE;

    return Math.min(ct0,  ct1);
  } // crossTrack
  
  private int crossTrack(final int index, final GeoPoint location)
  {
    final GeoPoint p1 = points_.get(index);
    final GeoPoint p2 = points_.get(index+1);
    
    double crossTrack = GeoHelper.crossTrack(p1, p2, location);

    return Math.abs((int)crossTrack); 
  } // crossTrack
  
  public int alongTrack(final GeoPoint location) 
  {
    int minIndex = -1;
    int minDistance = Integer.MAX_VALUE;
    for(int p = 0; p != points_.size(); ++p) 
    {
      int distance = points_.get(p).distanceTo(location);
      if(distance > minDistance)
        continue;

      minDistance = distance;
      minIndex = p;
    } // for ...
    
    int at0 = (minIndex != 0) ? alongTrack(minIndex - 1, location) : Integer.MAX_VALUE;
    int at1 = (minIndex+1 != points_.size()) ? alongTrack(minIndex, location) : Integer.MAX_VALUE;

    return (Math.abs(at0) < Math.abs(at1)) ? at0 : at1;
  } // alongTrack
  
  private int alongTrack(final int index, final GeoPoint location)
  {
    final GeoPoint p1 = points_.get(index);
    final GeoPoint p2 = points_.get(index+1);
  
    double alongTrack = GeoHelper.alongTrackOffset(p1, p2, location);

    return (int)alongTrack; 
  } // alongTrack

  public int distanceFromEnd(final GeoPoint location)
  {
    return finish().distanceTo(location);
  } // distanceFromEnd
  
  public String street() { return name_; }
  public String turn() { return turn_; }
  public boolean walk() { return walk_; }
  public String runningTime() { return running_time_; }
  public String distance() { return formatter.distance(distance_); }
  public String runningDistance() { return formatter.total_distance(running_distance_); }
  public String extraInfo() { return ""; }
  public IterableIterator<GeoPoint> points() { return new IterableIterator<GeoPoint>(points_.iterator()); }

  static public class Start extends Segment 
  {
    private final int itinerary_;
    private final String plan_;
    private final int speed_;
    private final int calories_;
    private final int co2_;
    
    public Start(final int itinerary,
          final String journey, 
          final String plan, 
          final int speed,
          final int total_time,
          final int total_distance, 
          final int calories,
          final int co2,
          final List<GeoPoint> points)
    {
      super(journey, "", false, total_time, 0, total_distance, points, true);
      itinerary_ = itinerary;
      plan_ = plan;
      speed_ = speed;
      calories_ = calories;
      co2_ = co2;
    } // Start
    
    public String name() { return super.street(); }
    public int itinerary() { return itinerary_; }
    public String plan() { return plan_; }
    public int speed() { return speed_; }
    
    public String toString() 
    {
      return street();
    } // toString
    
    public String street() 
    {
      return String.format("%s\n%s route : %s\nJourney time : %s", super.street(), initCap(plan_), super.runningDistance(), super.runningTime());
    } // street
    public String distance() { return ""; }
    public String runningDistance() { return ""; }
    public String runningTime() { return ""; }
    public String extraInfo() 
    { 
      if(co2_ == 0 && calories_ == 0)
        return "";
      int kg = co2_ / 1000;
      int g = (int)((co2_ % 1000) / 10.0);
      return String.format("Journey number : #%d\nCalories : %dkcal\nCO\u2082 saved : %d.%02dkg", 
                           itinerary_, calories_, kg, g); 
    } // extraInfo

    public int crossTrack(final GeoPoint location) { return Integer.MAX_VALUE; } 
  } // class Start
  
  static public class End extends Segment
  {
    final int total_distance_; 
    
    public End(final String destination, 
      final int total_time, 
      final int total_distance, 
      final List<GeoPoint> points)  
    {
      super("Destination " + destination, "", false, total_time, 0, total_distance, points, true);
      total_distance_ = total_distance;
    } // End

    public String toString() { return street(); }
    public String distance() { return ""; }
    public int total_distance() { return total_distance_; }
  } // End
  
  static public class Step extends Segment
  {
    public Step(final String name,
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
    } // Step
  } // class Step
  
  static public class Waymark extends Segment
  {
    public Waymark(final int count,
                   final int running_distance,
                   final GeoPoint gp)
    {
      super("Waypoint " + count,
            "Waymark",
            false, 
            0, 
            0,
            running_distance,
            Collections.list(gp, gp),
            false);
    } // Waymark

    public String distance() { return ""; }
    public String toString() { return street(); } 
  } // class Waymark
} // class Segment
