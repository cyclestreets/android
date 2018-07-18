package net.cyclestreets.routing;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.ShareActionProvider;
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
    private List<Listener> listeners = new ArrayList<>();

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
      if (listeners.contains(listener))
        return false;
      listeners.add(listener);
      return true;
    }
    public void unregister(final Listener listener) {
      listeners.remove(listener);
    }

    public void onNewJourney(final Journey journey, final Waypoints waypoints) {
      for(final Listener l : listeners)
        l.onNewJourney(journey, waypoints);
    }

    public void onReset() {
      for(final Listener l : listeners)
        l.onResetJourney();
    }
  }

  private static final Listeners listeners = new Listeners();

  public static void registerListener(final Listener l) { listeners.register(l); }
  public static void softRegisterListener(final Listener l) { listeners.softRegister(l); }
  public static void unregisterListener(final Listener l) { listeners.unregister(l); }

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
    final ReplanRoutingTask query = new ReplanRoutingTask(plan, db, context);
    query.execute(plannedRoute);
  }

  public static void PlotStoredRoute(final int localId,
                                     final Context context) {
    final StoredRoutingTask query = new StoredRoutingTask(db, context);
    query.execute(localId);
  }

  public static void RenameRoute(final int localId, final String newName) {
    db.renameRoute(localId, newName);
  }

  public static void DeleteRoute(final int localId) {
    db.deleteRoute(localId);
  }

  /////////////////////////////////////////
  private static ShareActionProvider shareRouteActionProvider;
  private static Journey plannedRoute = Journey.NULL_JOURNEY;
  private static Waypoints waypoints = plannedRoute.waypoints();
  private static RouteDatabase db;
  private static Context context;

  public static void initialise(final Context context) {
    Route.context = context;
    db = new RouteDatabase(context);

    if (isLoaded())
      loadLastJourney();
  }

  public static void setShareRouteActionProvider(ShareActionProvider shareRouteActionProvider) {
    Route.shareRouteActionProvider = shareRouteActionProvider;
  }

  public static void setWaypoints(final Waypoints waypoints) {
    Route.waypoints = waypoints;
  }

  public static void resetJourney() {
    onNewJourney(null);
  }

  public static void onResume() {
    Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());
  }

  /////////////////////////////////////
  public static int storedCount() {
    return db.routeCount();
  }

  public static List<RouteSummary> storedRoutes() {
    return db.savedRoutes();
  }

  /////////////////////////////////////
  public static boolean onNewJourney(final RouteData route) {
    try {
      doOnNewJourney(route);
      return true;
    }
    catch (Exception e) {
      Toast.makeText(context, R.string.route_finding_failed, Toast.LENGTH_LONG).show();
    }
    return false;
  }

  private static void doOnNewJourney(final RouteData route) {
    if (route == null) {
      plannedRoute = Journey.NULL_JOURNEY;
      waypoints = Waypoints.NULL_WAYPOINTS;
      shareRouteActionProvider.setShareIntent(null);
      listeners.onReset();
      clearRoutePref();
      return;
    }

    String uri = "https://www.cyclestreets.net/journey/" + String.valueOf(route.itinerary());
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, uri);

    plannedRoute = Journey.loadFromXml(route.xml(), route.points(), route.name());

    db.saveRoute(plannedRoute, route.xml());
    waypoints = plannedRoute.waypoints();
    shareRouteActionProvider.setShareIntent(shareIntent);

    listeners.onNewJourney(plannedRoute, waypoints);
    setRoutePref();
  }

  public static Waypoints waypoints() { return waypoints; }

  public static boolean available() { return plannedRoute != Journey.NULL_JOURNEY; }
  public static Journey journey() { return plannedRoute; }

  private static void loadLastJourney() {
    RouteSummary lastRoute = storedRoutes().get(0);
    RouteData route = db.route(lastRoute.localId());
    Route.onNewJourney(route);
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
    return context.getSharedPreferences("net.cyclestreets.CycleStreets", Context.MODE_PRIVATE);
  }

  private Route() { }
}
