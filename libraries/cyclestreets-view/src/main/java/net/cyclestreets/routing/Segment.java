package net.cyclestreets.routing;

import java.util.Arrays;
import java.util.List;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.Collections;
import net.cyclestreets.util.GeoHelper;
import net.cyclestreets.util.IterableIterator;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public abstract class Segment {
  protected final String name_;
  protected final String turn_;
  protected final boolean walk_;
  protected final String running_time_;
  protected final int distance_;
  protected final int running_distance_;
  protected final List<IGeoPoint> points_;

  public static DistanceFormatter formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());

  Segment(final String name,
          final String turn,
          final boolean walk,
          final int time,
          final int distance,
          final int running_distance,
          final List<IGeoPoint> points,
          final boolean terminal) {
    this(name,
         turn,
         walk,
         formatTime(time, terminal),
         distance,
         running_distance,
         points,
         terminal);
  }

  Segment(final String name,
          final String turn,
          final boolean walk,
          final String running_time,
          final int distance,
          final int running_distance,
          final List<IGeoPoint> points,
          final boolean terminal) {
    name_ = name;
    turn_ = initCap(turn);
    walk_ = walk;
    running_time_ = running_time;
    distance_ = distance;
    running_distance_ = running_distance;
    points_ = points;
  }

  static protected String initCap(final String s) {
    return s.length() != 0 ? s.substring(0,1).toUpperCase() + s.substring(1) : s;
  }

  private static String formatTime(int time, boolean terminal) {
    if (time == 0)
      return "";

    int hours = time/3600;
    int remainder = time%3600;
    int minutes = remainder/60;
    int seconds = time%60;

    if (terminal)
      return formatTerminalTime(hours, minutes);

    if (hours == 0)
      return String.format("%d:%02d", minutes, seconds);

    return String.format("%d:%02d:%02d", hours, minutes, seconds);
  }

  private static String formatTerminalTime(int hours, int minutes) {
    if (hours == 0)
      return String.format("%d minute%s", minutes, (minutes != 1) ? "s" : "");
    String fraction = "";
    if (minutes > 52)
      ++hours;
    else if (minutes > 37)
      fraction = "\u00BE";
    else if (minutes > 22)
      fraction = "\u00BD";
    else if (minutes > 7)
      fraction = "\u00BC";
    return String.format("%d%s hours", hours, fraction);
  }

  public String toString() {
    String s = name_;
    if (turn_.length() != 0)
      s = turn_ + " into " + name_;
    if (walk())
      s += "\nPlease dismount and walk.";
    return s;
  }

  public IGeoPoint start() { return points_.get(0); }
  public IGeoPoint finish() { return points_.get(points_.size()-1); }

  public int distanceFrom(final GeoPoint location) {
    int ct = crossTrackError(location);
    int at = alongTrackError(location);

    return Math.max(Math.abs(at), ct);
  }

  public int crossTrackError(final GeoPoint location) {
    int minIndex = closestPoint(location);

    int ct0 = (minIndex != 0) ? crossTrack(minIndex - 1, location) : Integer.MAX_VALUE;
    int ct1 = (minIndex+1 != points_.size()) ? crossTrack(minIndex, location) : Integer.MAX_VALUE;

    return Math.min(ct0,  ct1);
  }

  private int crossTrack(final int index, final GeoPoint location) {
    final IGeoPoint p1 = points_.get(index);
    final IGeoPoint p2 = points_.get(index+1);

    double crossTrack = GeoHelper.crossTrack(p1, p2, location);

    return Math.abs((int)crossTrack);
  }

  public int alongTrackError(final GeoPoint location) {
    int minIndex = closestPoint(location);
    final int lastIndex = points_.size() - 1;

    if (minIndex != 0 && minIndex != lastIndex)
      return 0;

    final GeoHelper.AlongTrack at = alongTrack(minIndex == 0 ? minIndex : minIndex-1, location);
    return at.onTrack() ? 0 : at.offset();
  }

  public int alongTrack(final GeoPoint location) {
    int minIndex = closestPoint(location);
    final int lastIndex = points_.size() - 1;

    if (minIndex == lastIndex)
      --minIndex;

    if (minIndex == 0) {
      final GeoHelper.AlongTrack at = alongTrack(minIndex, location);
      return at.onTrack() ? at.offset() : -at.offset();
    }

    GeoHelper.AlongTrack at = alongTrack(minIndex, location);
    if (at.position() == GeoHelper.AlongTrack.Position.BEFORE_START)
      --minIndex;
    if (at.position() == GeoHelper.AlongTrack.Position.OFF_END)
      ++minIndex;
    at = alongTrack(minIndex, location);

    int cumulative = 0;
    for (int i = 1; i <= minIndex; ++i) {
      final IGeoPoint p1 = points_.get(i-1);
      final IGeoPoint p2 = points_.get(i);
      cumulative += ((GeoPoint)p1).distanceTo(p2);
    }

    cumulative += at.offset();

    return at.onTrack() ? cumulative : -cumulative;
  }

  private GeoHelper.AlongTrack alongTrack(final int index, final GeoPoint location) {
    final IGeoPoint p1 = points_.get(index);
    final IGeoPoint p2 = points_.get(index+1);

    GeoHelper.AlongTrack alongTrack = GeoHelper.alongTrackOffset(p1, p2, location);

    return alongTrack;
  }

  private int closestPoint(final GeoPoint location) {
    int minIndex = -1;
    int minDistance = Integer.MAX_VALUE;

    for (int p = 0; p != points_.size(); ++p) {
      int distance = GeoHelper.distanceBetween(points_.get(p), (location));
      if (distance > minDistance)
        continue;

      minDistance = distance;
      minIndex = p;
    }

    return minIndex;
  }

  public int distanceFromEnd(final GeoPoint location) {
    return GeoHelper.distanceBetween(finish(), location);
  }

  public String street() { return name_; }
  public String turn() { return turn_; }
  public boolean walk() { return walk_; }
  public String runningTime() { return running_time_; }
  public String distance() { return formatter.distance(distance_); }
  public String runningDistance() { return formatter.total_distance(running_distance_); }
  public String extraInfo() { return ""; }
  public IterableIterator<IGeoPoint> points() { return new IterableIterator<>(points_.iterator()); }

  public static class Start extends Segment  {
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
          final List<IGeoPoint> points) {
      super(journey, "", false, total_time, 0, total_distance, points, true);
      itinerary_ = itinerary;
      plan_ = plan;
      speed_ = speed;
      calories_ = calories;
      co2_ = co2;
    }

    public String name() { return super.street(); }
    public int itinerary() { return itinerary_; }
    public String plan() { return plan_; }
    public int speed() { return speed_; }

    public String toString() {
      return street();
    }

    public String street() {
      return String.format("%s\n%s route : %s\nJourney time : %s", super.street(), initCap(plan_), super.runningDistance(), super.runningTime());
    }
    public String distance() { return ""; }
    public String runningDistance() { return ""; }
    public String runningTime() { return ""; }
    public String totalTime() { return super.runningTime(); }
    public String calories() { return String.format("%dkcal", calories_); }
    public String co2() {
      int kg = co2_ / 1000;
      int g = (int)((co2_ % 1000) / 10.0);
      return String.format("%d.%02dkg", kg, g);
    }

    public String extraInfo() {
      if (co2_ == 0 && calories_ == 0)
        return "";
      int kg = co2_ / 1000;
      int g = (int)((co2_ % 1000) / 10.0);
      return String.format("Journey number : #%d\nCalories : %dkcal\nCO\u2082 saved : %d.%02dkg",
                           itinerary_, calories_, kg, g);
    }

    public int crossTrackError(final GeoPoint location) { return Integer.MAX_VALUE; }
  }

  public static class End extends Segment  {
    final int total_distance_;

    public End(final String destination,
      final int total_time,
      final int total_distance,
      final List<IGeoPoint> points) {
      super("Destination " + destination, "", false, total_time, 0, total_distance, points, true);
      total_distance_ = total_distance;
    }

    public String toString() { return street(); }
    public String distance() { return ""; }
    public int total_distance() { return total_distance_; }
  }

  public static class Step extends Segment  {
    public Step(final String name,
       final String turn,
       final boolean walk,
       final int time,
       final int distance,
       final int running_distance,
       final List<IGeoPoint> points) {
      super(name,
          turn.length() != 0 ? turn.substring(0,1).toUpperCase() + turn.substring(1) : turn,
          walk,
          time,
          distance,
          running_distance,
          points,
          false);
    }

    public Step(final Segment s1, final Segment s2) {
      super(s2.name_,
            s2.turn_,
            s1.walk_ || s2.walk_,
            s2.running_time_,
            s1.distance_ + s2.distance_,
            s2.running_distance_,
            Collections.concatenate(s1.points_, s2.points_),
            false);
    }
  }

  public static class Waymark extends Segment  {
    public Waymark(final int count,
                   final int running_distance,
                   final IGeoPoint gp) {
      super("Waypoint " + count,
            "Waymark",
            false,
            0,
            0,
            running_distance,
            Arrays.asList(gp, gp),
            false);
    }

    public String distance() { return ""; }
    public String toString() { return street(); }
  }
}
