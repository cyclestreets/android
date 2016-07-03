package net.cyclestreets.api;

import net.cyclestreets.routing.Waypoints;

import java.io.IOException;

public class JourneyPlanner {
  private static final int DEFAULT_SPEED = 20;
  
  /////////////////////////////////////////////////////////////////
  public static String getJourneyXml(final String plan,
                                     final Waypoints waypoints) throws IOException {
    return getJourneyXml(plan, DEFAULT_SPEED, waypoints);
  }
  
  public static String getJourneyXml(final String plan,
                                     final int speed,
                                     final Waypoints waypoints) throws IOException {
    final double[] lonLat = new double[waypoints.count()*2];
    for (int i = 0; i != waypoints.count(); ++i) {
      int l = i*2;
      lonLat[l] = waypoints.get(i).getLongitudeE6() / 1E6;
      lonLat[l+1] = waypoints.get(i).getLatitudeE6() / 1E6;
    }
    return ApiClient.getJourneyXml(plan,
                                   null, 
                                   null, 
                                   speed,
                                   lonLat);
  }
  
  public static String getJourneyXml(final String plan,
                                     final long itinerary) throws IOException {
    return ApiClient.getJourneyXml(plan, itinerary);
  }
    
  private JourneyPlanner() {}
}
