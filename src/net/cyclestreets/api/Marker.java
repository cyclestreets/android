package net.cyclestreets.api;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Marker {
   @Attribute(required=false)
   public String name, points, flow, turn, elevations, distances, provisionName, color, type;

   @Attribute(required=false)
   public String start, finish, event, whence, plan, note, leaving, arriving, coordinates;
   	
   @Attribute(required=false)
   public double start_longitude, start_latitude, finish_longitude, finish_latitude,
   	north, south, east, west;
   
   @Attribute(required=false)
   public int startBearing, startSpeed, crow_fly_distance, speed, itinerary, clientRouteId,
   		length;

   @Attribute(required=false)
   public int distance, time, busynance, quietness, walk, signalledJunctions, signalledCrossings;
}
