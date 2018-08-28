package net.cyclestreets.routing;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.view.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.content.RouteDatabase;
import net.cyclestreets.content.RouteSummary;

public class Route
{
  public interface Listener {
    void onNewJourney(final Journey journey, final Waypoints waypoints);
    void onResetJourney();
  }

  private static class Listeners {
    private List<Listener> listeners_ = new ArrayList<>();

    public void register(final Listener listener) {
      if (!doRegister(listener))
        return;

      if ((Route.journey() != Journey.NULL_JOURNEY) || (Route.waypoints() != Waypoints.NULL_WAYPOINTS))
        listener.onNewJourney(Route.journey(), Route.waypoints());
      else
        listener.onResetJourney();
    }

    public void softRegister(final Listener listener) {
      doRegister(listener);
    }

    private boolean doRegister(final Listener listener) {
      if (listeners_.contains(listener))
        return false;
      listeners_.add(listener);
      return true;
    }
    public void unregister(final Listener listener) {
      listeners_.remove(listener);
    }

    public void onNewJourney(final Journey journey, final Waypoints waypoints) {
      for (final Listener l : listeners_)
        l.onNewJourney(journey, waypoints);
    }

    public void onReset() {
      for (final Listener l : listeners_)
        l.onResetJourney();
    }
  }

  private static final Listeners listeners_ = new Listeners();

  public static void registerListener(final Listener l) { listeners_.register(l); }
  public static void softRegisterListener(final Listener l) { listeners_.softRegister(l); }
  public static void unregisterListener(final Listener l) { listeners_.unregister(l); }

  public static void PlotRoute(final String plan,
                               final int speed,
                               final Context context,
                               final Waypoints waypoints) {
    final CycleStreetsRoutingTask query = new CycleStreetsRoutingTask(plan, speed, context);
    query.execute(waypoints);
  }

  public static void FetchRoute(final String plan,
                                final long itinerary,
                                final int speed,
                                final Context context) {
    final FetchCycleStreetsRouteTask query = new FetchCycleStreetsRouteTask(plan, speed, context);
    query.execute(itinerary);
  }

  public static void RePlotRoute(final String plan,
                                 final Context context) {
    final ReplanRoutingTask query = new ReplanRoutingTask(plan, db_, context);
    query.execute(plannedRoute_);
  }

  public static void PlotStoredRoute(final int localId,
                                     final Context context) {
    final StoredRoutingTask query = new StoredRoutingTask(db_, context);
    query.execute(localId);
  }

  public static void RenameRoute(final int localId, final String newName) {
    db_.renameRoute(localId, newName);
  }

  public static void DeleteRoute(final int localId) {
    db_.deleteRoute(localId);
  }

  /////////////////////////////////////////
  private static Journey plannedRoute_ = Journey.NULL_JOURNEY;
  private static Waypoints waypoints_ = plannedRoute_.waypoints();
  private static RouteDatabase db_;
  private static Context context_;

  public static void initialise(final Context context) {
    context_ = context;
    db_ = new RouteDatabase(context);

    if (isLoaded())
      loadLastJourney();
  }

  public static void setWaypoints(final Waypoints waypoints) {
    waypoints_ = waypoints;
  }

  public static void resetJourney() {
    onNewJourney(null);
  }

  public static void onResume() {
    Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());
  }

  /////////////////////////////////////
  public static int storedCount() {
    return db_.routeCount();
  }

  public static List<RouteSummary> storedRoutes() {
    return db_.savedRoutes();
  }

  /////////////////////////////////////
  public static boolean onNewJourney(final RouteData route) {
    try {
      doOnNewJourney(route);
      return true;
    }
    catch (Exception e) {
      Toast.makeText(context_, R.string.route_finding_failed, Toast.LENGTH_LONG).show();
    }
    return false;
  }

  private static void doOnNewJourney(final RouteData route) {
    if (route == null) {
      plannedRoute_ = Journey.NULL_JOURNEY;
      waypoints_ = Waypoints.NULL_WAYPOINTS;
      listeners_.onReset();
      clearRoutePref();
      return;
    }

    plannedRoute_ = Journey.loadFromJson(route.json(), route.points(), route.name());

    db_.saveRoute(plannedRoute_, route.json());
    waypoints_ = plannedRoute_.waypoints();
    listeners_.onNewJourney(plannedRoute_, waypoints_);
    setRoutePref();
  }

  public static Waypoints waypoints() { return waypoints_; }

  public static boolean available() { return plannedRoute_ != Journey.NULL_JOURNEY; }
  public static Journey journey() { return plannedRoute_; }

  private static void loadLastJourney() {
    List<RouteSummary> routeSummaries = storedRoutes();
    if (!storedRoutes().isEmpty()) {
      RouteSummary lastRoute = routeSummaries.get(0);
      RouteData route = db_.route(lastRoute.localId());
      Route.onNewJourney(route);
    }
  }

  private static void clearRoutePref() {
    prefs().edit().remove(routePref).commit();
  }
  private static void setRoutePref() {
    prefs().edit().putBoolean(routePref, true).commit();
  }

  private static boolean isLoaded() {
    return prefs().getBoolean(routePref, false);
  }

  private static final String routePref = "route";

  private static SharedPreferences prefs() {
    return context_.getSharedPreferences("net.cyclestreets.CycleStreets", Context.MODE_PRIVATE);
  }

  private Route() { }
}
