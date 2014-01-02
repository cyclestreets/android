package net.cyclestreets.api;

import net.cyclestreets.routing.Waypoints;

public class JourneyPlanner
{
  private final static int DEFAULT_SPEED = 20;
  
  /////////////////////////////////////////////////////////////////
  static public String getJourneyXml(final String plan, 
                                     final Waypoints waypoints)
    throws Exception
  {
    return getJourneyXml(plan, DEFAULT_SPEED, waypoints);
  } // getJourneyXml
  
  static public String getJourneyXml(final String plan, 
                                     final int speed,
                                     final Waypoints waypoints) 
    throws Exception 
  {
    final double[] lonLat = new double[waypoints.count()*2];
    for(int i = 0; i != waypoints.count(); ++i)
    {
      int l = i*2;
      lonLat[l] = waypoints.get(i).getLongitudeE6() / 1E6;
      lonLat[l+1] = waypoints.get(i).getLatitudeE6() / 1E6;
    } // for ...
    return ApiClient.getJourneyXml(plan,
                                   null, 
                                   null, 
                                   speed,
                                   lonLat);
  } // getJourneyXml
  
  static public String getJourneyXml(final String plan, 
                                     final long itinerary) 
    throws Exception
  {
    return ApiClient.getJourneyXml(plan, itinerary);
  } // getJourneyXml
    
  private JourneyPlanner() { }
} // class JourneyPlanner
