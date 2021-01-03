package net.cyclestreets.api;

import net.cyclestreets.routing.Waypoints;

public class JourneyPlanner {

  public static String getJourneyJson(final String plan,
                                      final int speed,
                                      final Waypoints waypoints) {
    return ApiClient.INSTANCE.getJourneyJson(plan,
                                             null,
                                             null,
                                             speed,
                                             lonLat(waypoints));
  }

  public static String getJourneyJson(final String plan,
                                      final long itinerary) {
    return ApiClient.INSTANCE.getJourneyJson(plan, itinerary);
  }

  public static String getCircularJourneyJson(final Waypoints waypoints,
                                      Integer distance,
                                      Integer duration,
                                      String pois) {
    return ApiClient.INSTANCE.getCircularJourneyJson(lonLat(waypoints), distance, duration, pois);
  }

  private static double[] lonLat(Waypoints waypoints) {
    final double[] lonLat = new double[waypoints.count() * 2];
    for (int i = 0; i != waypoints.count(); ++i) {
      int l = i * 2;
      lonLat[l] = waypoints.get(i).getLongitude();
      lonLat[l + 1] = waypoints.get(i).getLatitude();
    }
    return lonLat;
  }

  private JourneyPlanner() {}
}
