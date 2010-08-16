package net.cyclestreets.api;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Marker {
   @Attribute(required=false)
   public String name, points, flow, turn, elevations, distances, provisionName, color, type;

   @Attribute(required=false)
   public int distance, time, busynance, walk, startBearing, signalledJunctions, signalledCrossings;
}
