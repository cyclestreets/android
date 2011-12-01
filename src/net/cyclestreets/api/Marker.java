package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

public class Marker 
{
  public String name() { return name_; }
  public List<GeoPoint> points()
  {
    final List<GeoPoint> points = new ArrayList<GeoPoint>();
    final String[] coords = points_.split(" ");
    for (final String coord : coords) 
    {
      final String[] yx = coord.split(",");
      final GeoPoint p = new GeoPoint(Double.parseDouble(yx[1]), Double.parseDouble(yx[0]));
      points.add(p);
    } // for ...
    return points;
  } // points
  public String turn() { return turn_; }
  public boolean isSegment() { return type_.equals("segment"); }
  public boolean isRoute() { return type_.equals("route"); }
  public int calories() { return calories_; }
  public int co2Saved() { return grammesCO2saved_; }
  public String finish() { return finish_; }
  public String plan() { return plan_; }
  public int distance() { return distance_; }
  public int time() { return time_; }
  public boolean shouldWalk() { return walk_ == 1; }
  public int itinerary() { return itinerary_; }
  public int speed() { return speed_; }

  ///////////////////////////////////////////////
  String name_; 
  String points_;
  String turn_;
  String type_;

  int grammesCO2saved_;
  int calories_;
   
  String finish_;
  String plan_;
   	
  int speed_;
  int itinerary_;
  int distance_;
  int time_;
  int walk_;
} // class Marker
