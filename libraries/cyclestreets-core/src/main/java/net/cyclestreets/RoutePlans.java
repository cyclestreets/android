package net.cyclestreets;

public class RoutePlans {
  // Route types
  public final static String PLAN_BALANCED = "balanced";
  public final static String PLAN_FASTEST = "fastest";
  public final static String PLAN_QUIETEST = "quietest";
  public final static String PLAN_SHORTEST = "shortest";

  private final static String[] Plans = new String[] {
      PLAN_QUIETEST, PLAN_BALANCED, PLAN_FASTEST, PLAN_SHORTEST
  };

  public static String[] allPlans() {
    return Plans;
  } // plans
} // RoutePlans
