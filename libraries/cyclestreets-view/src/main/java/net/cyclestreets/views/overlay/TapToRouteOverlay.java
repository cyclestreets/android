package net.cyclestreets.views.overlay;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import net.cyclestreets.RoutePlans;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.FeedbackActivity;
import net.cyclestreets.util.Logging;
import net.cyclestreets.util.Theme;
import net.cyclestreets.view.R;
import net.cyclestreets.Undoable;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.Share;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.api.IProjection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.MenuHelper.showMenuItem;

public class TapToRouteOverlay extends WaymarkOverlay
                               implements TapListener,
                                          ContextMenuListener,
                                          Undoable,
                                          PauseResumeListener,
                                          Route.Listener
{
  private static final String TAG = Logging.getTag(TapToRouteOverlay.class);
  private static final int[] REPLAN_MENU_IDS = { R.string.route_menu_change_replan_quietest,
                                                 R.string.route_menu_change_replan_balanced,
                                                 R.string.route_menu_change_replan_fastest,
                                                 R.string.route_menu_change_replan_shortest };
  private static final Map<Integer, String> REPLAN_MENU_PLANS = new HashMap<Integer, String>() {{
      put(R.string.route_menu_change_replan_quietest, RoutePlans.PLAN_QUIETEST);
      put(R.string.route_menu_change_replan_balanced, RoutePlans.PLAN_BALANCED);
      put(R.string.route_menu_change_replan_fastest, RoutePlans.PLAN_FASTEST);
      put(R.string.route_menu_change_replan_shortest, RoutePlans.PLAN_SHORTEST);
    }};
  private static final int MAX_WAYPOINTS = 30;

  private final Button routingInfoRect;
  private final ImageView routeNowIcon;
  private final FloatingActionButton restartButton;

  private final CycleMapView mapView;

  private final Context context;

  private final Drawable shareDrawable;
  private final Drawable commentDrawable;

  private List<OverlayItem> waymarkers;

  private int highlightColour;
  private int lowlightColour;

  private TapToRoute tapState;

  private OverlayHelper overlays;

  public TapToRouteOverlay(final CycleMapView mapView) {
    super(mapView);

    context = mapView.getContext();
    this.mapView = mapView;

    shareDrawable = new IconicsDrawable(context)
            .icon(GoogleMaterial.Icon.gmd_share)
            .color(Theme.lowlightColorInverse(context))
            .sizeDp(24);
    commentDrawable = new IconicsDrawable(context)
            .icon(GoogleMaterial.Icon.gmd_comment)
            .color(Theme.lowlightColorInverse(context))
            .sizeDp(24);

    // The view is shared, and has already been added by the RouteHighlightOverlay.
    // So find that, and don't inflate a second copy.
    View routeView = mapView.findViewById(R.id.route_view);

    routingInfoRect = routeView.findViewById(R.id.routing_info_rect);
    routingInfoRect.setOnClickListener(view -> onRouteNow(waypoints()));
    routeNowIcon = routeView.findViewById(R.id.route_now_icon);
    restartButton = routeView.findViewById(R.id.restartbutton);
    restartButton.setOnClickListener(view -> tapRestart());

    lowlightColour = Theme.lowlightColor(context) | 0xFF000000;
    highlightColour = Theme.highlightColor(context) | 0xFF000000;

    waymarkers = new ArrayList<>();

    tapState = TapToRoute.start();

    overlays = new OverlayHelper(mapView);
  }

  private ControllerOverlay controller() {
    return overlays.controller();
  }

  private void setRoute(final boolean complete) {
    tapState = complete ? TapToRoute.ALL_DONE : tapState.reset();
    controller().flushUndo(this);
  }

  private void resetRoute() {
    tapState = tapState.reset();
    controller().flushUndo(this);
  }

  private void onRouteNow(final Waypoints waypoints) {
    Route.PlotRoute(CycleStreetsPreferences.routeType(),
                    CycleStreetsPreferences.speed(),
                    context,
                    waypoints);
  }

  ////////////////////////////////////////////
  @Override
  public void onCreateOptionsMenu(final Menu menu) {
    createMenuItem(menu, R.string.route_menu_change, Menu.FIRST, R.drawable.ic_menu_more);
    createMenuItem(menu, R.string.route_menu_change_share, Menu.NONE, shareDrawable);
    createMenuItem(menu, R.string.route_menu_change_comment, Menu.NONE, commentDrawable);
  }

  @Override
  public void onPrepareOptionsMenu(final Menu menu) {
    showMenuItem(menu, R.string.route_menu_change, tapState == TapToRoute.ALL_DONE);
    showMenuItem(menu, R.string.route_menu_change_share, tapState == TapToRoute.ALL_DONE);
    showMenuItem(menu, R.string.route_menu_change_comment, tapState == TapToRoute.ALL_DONE);
  }

  @Override
  public void onCreateContextMenu(final ContextMenu menu) {
    if (tapState != TapToRoute.ALL_DONE)
      return;

    final String currentPlan = Route.journey().plan();
    for (int id : REPLAN_MENU_IDS)
      if (!currentPlan.equals(REPLAN_MENU_PLANS.get(id)))
        createMenuItem(menu, id);
    if (mapView.isMyLocationEnabled())
      createMenuItem(menu, R.string.route_menu_change_reroute_from_here);
    createMenuItem(menu, R.string.route_menu_change_reverse);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, final MenuItem item) {
    final int menuId = item.getItemId();

    if (menuId == R.string.route_menu_change) {
      mapView.showContextMenu();
      return true;
    }

    if (REPLAN_MENU_PLANS.containsKey(menuId)) {
      Route.RePlotRoute(REPLAN_MENU_PLANS.get(menuId), context);
      return true;
    }

    if (R.string.route_menu_change_reroute_from_here == menuId) {
      final Location lastFix = mapView.getLastFix();
      if (lastFix == null) {
        Toast.makeText(mapView.getContext(), R.string.route_no_location, Toast.LENGTH_LONG).show();
        return true;
      }

      final GeoPoint from = new GeoPoint(lastFix.getLatitude(), lastFix.getLongitude());
      onRouteNow(Waypoints.Companion.fromTo(from, finish()));
    }
    if (R.string.route_menu_change_reverse == menuId) {
      onRouteNow(waypoints().reversed());
      return true;
    }
    if (R.string.route_menu_change_share == menuId) {
      Share.Url(mapView,
                Route.journey().url(),
                Route.journey().name(),
                "CycleStreets journey");
      return true;
    }
    if (R.string.route_menu_change_comment == menuId) {
      final Context context = mapView.getContext();
      context.startActivity(new Intent(context, FeedbackActivity.class));
      return true;
    }

    return false;
  }

  ////////////////////////////////////////////
  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    super.draw(canvas, mapView, shadow);

    drawRoutingInfoRect();
    drawRestartButton();
  }

  private void drawRestartButton() {
    if (tapState == TapToRoute.ALL_DONE) {
      restartButton.show();
    } else {
      restartButton.hide();
    }
  }

  private void drawRoutingInfoRect() {
    if (tapState == TapToRoute.ALL_DONE) {
      // In this case, populating the routing info is done by the RouteHighlightOverlay
      return;
    }

    if (tapState == TapToRoute.WAITING_FOR_NEXT || tapState == TapToRoute.WAITING_TO_ROUTE) {
      routeNowIcon.setVisibility(View.VISIBLE);
    } else {
      routeNowIcon.setVisibility(View.INVISIBLE);
    }

    int bgColour = (tapState == TapToRoute.WAITING_FOR_START ||
                    tapState == TapToRoute.WAITING_FOR_SECOND) ? lowlightColour : highlightColour;
    routingInfoRect.setBackgroundColor(bgColour);
    routingInfoRect.setGravity(Gravity.CENTER);
    routingInfoRect.setText(tapState.actionDescription);
    routingInfoRect.setEnabled(tapState == TapToRoute.WAITING_FOR_NEXT ||
                               tapState == TapToRoute.WAITING_TO_ROUTE);
  }

  //////////////////////////////////////////////
  @Override
  public boolean onSingleTap(final MotionEvent event) {
    return tapMarker(event);
  }

  @Override
  public boolean onDoubleTap(final MotionEvent event) {
    return false;
  }

  private boolean tapRestart() {
    if (!CycleStreetsPreferences.confirmNewRoute())
      return stepBack(true);

    MessageBox.YesNo(mapView,
                     "Start a new route?",
                     (arg0, arg1) -> stepBack(true));

    return true;
  }

  @Override
  public boolean onBackPressed() {
    return stepBack(false);
  }

  private boolean stepBack(final boolean tap) {
    if (!tap && !tapState.waypointingInProgress)
      return false;

    switch(tapState) {
      case WAITING_FOR_START:
        return true;
      case WAITING_TO_ROUTE:
      case WAITING_FOR_SECOND:
      case WAITING_FOR_NEXT:
        removeWaypoint();
        break;
      case ALL_DONE:
        Route.resetJourney();
        break;
    }

    tapState = tapState.previous(waymarkersCount());
    mapView.postInvalidate();

    return true;
  }

  private boolean tapMarker(final MotionEvent event) {
    final IGeoPoint p = mapView.getProjection().fromPixels((int) event.getX(), (int)event.getY());
    tapAction(p);
    return true;
  }

  public void setNextMarker(final IGeoPoint point) {
    tapAction(point);
  }

  private void tapAction(final IGeoPoint point) {
    if (tapState == TapToRoute.WAITING_TO_ROUTE || tapState == TapToRoute.ALL_DONE) {
      // Any taps that hit the overlay shouldn't do anything - we can't accept more waypoints
      return;
    }

    addWaypoint(point);
    controller().pushUndo(this);
    tapState = tapState.next(waymarkersCount());
    mapView.invalidate();
  }

  ////////////////////////////////////
  private enum TapToRoute  {
    WAITING_FOR_START(false, "Tap map to set Start"),
    WAITING_FOR_SECOND(true, "Tap map to set Waypoint"),
    WAITING_FOR_NEXT(true, "Tap map to set Waypoint\nTap here to Route"),
    WAITING_TO_ROUTE(true, "Tap here to Route"),
    ALL_DONE(false, "");

    public final String actionDescription;
    public final boolean waypointingInProgress;

    TapToRoute(boolean waypointingInProgress, String actionDescription) {
      this.waypointingInProgress = waypointingInProgress;
      this.actionDescription = actionDescription;
    }

    public static TapToRoute start() {
      return WAITING_FOR_START;
    }

    public TapToRoute reset() {
      return WAITING_FOR_START;
    }

    public TapToRoute previous(final int count) {
      TapToRoute previous;
      switch(this) {
        case WAITING_FOR_START:
        case WAITING_FOR_SECOND:
        case ALL_DONE:
          previous = WAITING_FOR_START;
          break;
        case WAITING_FOR_NEXT:
          previous = (count == 1) ? WAITING_FOR_SECOND : WAITING_FOR_NEXT;
          break;
        case WAITING_TO_ROUTE:
        default:
          previous = WAITING_FOR_NEXT;
          break;
      }
      Log.d(TAG, "Moving to previous TapToRoute state=" + previous.name() + " with waypoints=" + count);
      return previous;
    }

    public TapToRoute next(final int count) {
      TapToRoute next;
      switch(this) {
        case WAITING_FOR_START:
          next = WAITING_FOR_SECOND;
          break;
        case WAITING_FOR_SECOND:
          next = WAITING_FOR_NEXT;
          break;
        case WAITING_FOR_NEXT:
          next = (count == MAX_WAYPOINTS) ? WAITING_TO_ROUTE : WAITING_FOR_NEXT;
          break;
        case WAITING_TO_ROUTE:
        case ALL_DONE:
        default:
          next = ALL_DONE;
      }
      Log.d(TAG, "Moving to next TapToRoute state=" + next.name() + " with waypoints=" + count);
      return next;
    }
  }

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    super.onNewJourney(journey, waypoints);
    setRoute(!journey.isEmpty());
  }

  @Override
  public void onResetJourney() {
    super.onResetJourney();
    resetRoute();
  }
}
