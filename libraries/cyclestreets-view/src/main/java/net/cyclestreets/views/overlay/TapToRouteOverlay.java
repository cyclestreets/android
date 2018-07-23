package net.cyclestreets.views.overlay;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import net.cyclestreets.RoutePlans;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.FeedbackActivity;
import net.cyclestreets.view.R;
import net.cyclestreets.Undoable;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.Share;
import net.cyclestreets.views.CycleMapView;
import net.cyclestreets.util.Collections;

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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.MenuHelper.showMenuItem;

public class TapToRouteOverlay extends Overlay
                               implements ButtonTapListener,
                                          TapListener,
                                          ContextMenuListener,
                                          Undoable,
                                          PauseResumeListener,
                                          Route.Listener
{
  private static int[] Replan_Menu_Ids = { R.string.route_menu_change_replan_quietest,
                                           R.string.route_menu_change_replan_balanced,
                                           R.string.route_menu_change_replan_fastest,
                                           R.string.route_menu_change_replan_shortest};
  private static Map<Integer, String> Replan_Menu_Plans =
      Collections.map(R.string.route_menu_change_replan_quietest, RoutePlans.PLAN_QUIETEST)
                 .map(R.string.route_menu_change_replan_balanced, RoutePlans.PLAN_BALANCED)
                 .map(R.string.route_menu_change_replan_fastest, RoutePlans.PLAN_FASTEST)
                 .map(R.string.route_menu_change_replan_shortest, RoutePlans.PLAN_SHORTEST);

  private final Drawable greenWisp;
  private final Drawable orangeWisp;
  private final Drawable redWisp;
  private final Bitmap canRoute;
  private final Point screenPos = new Point();
  private final Matrix canvasTransform = new Matrix();
  private final float[] transformValues = new float[9];
  private final Matrix bitmapTransform = new Matrix();
  private final Paint bitmapPaint = new Paint();

  private final float radius;

  private final FloatingActionButton restartButton;

  private final CycleMapView mapView;

  private final Context context;

  private List<OverlayItem> waymarkers;
  private Rect tapStateRect;

  private Paint textBrush;
  private Paint highlightBrush;
  private Paint lowlightBrush;

  private TapToRoute tapState;

  private OverlayHelper overlays;

  public TapToRouteOverlay(final CycleMapView mapView) {
    super();

    context = mapView.getContext();
    this.mapView = mapView;

    final Resources res = context.getResources();
    greenWisp = ResourcesCompat.getDrawable(res, R.drawable.greep_wisp, null);
    orangeWisp = ResourcesCompat.getDrawable(res, R.drawable.orange_wisp, null);
    redWisp = ResourcesCompat.getDrawable(res, R.drawable.red_wisp, null);
    canRoute = ((BitmapDrawable) ResourcesCompat.getDrawable(res, R.drawable.ic_route_now, null)).getBitmap();

    int offset = DrawingHelper.offset(context);
    radius = DrawingHelper.cornerRadius(context);

    View restartButtonView = LayoutInflater.from(mapView.getContext()).inflate(R.layout.restart_planning_button, null);
    restartButton = restartButtonView.findViewById(R.id.restartbutton);
    restartButton.setVisibility(View.INVISIBLE);
    restartButton.setOnClickListener(view -> tapRestart());
    mapView.addView(restartButtonView);

    tapStateRect = new Rect();
    tapStateRect.bottom = tapStateRect.top + canRoute.getHeight();

    textBrush = Brush.createTextBrush(offset);
    highlightBrush = Brush.HighlightBrush(context);
    lowlightBrush = Brush.LowlightBrush(context);

    waymarkers = new ArrayList<>();

    tapState = TapToRoute.start();

    overlays = new OverlayHelper(mapView);
  }

  private ControllerOverlay controller() {
    return overlays.controller();
  }

  private void setRoute(final Waypoints waypoints, final boolean complete) {
    resetRoute();

    for (final IGeoPoint waypoint : waypoints) {
      addWaypoint(waypoint);
      tapState = tapState.next(waymarkersCount());
    }

    if (!complete)
      return;
    controller().flushUndo(this);
    tapState = TapToRoute.ALL_DONE;
  }

  private void resetRoute() {
    waymarkers.clear();
    tapState = tapState.reset();
    controller().flushUndo(this);
  }

  private int waymarkersCount() {
    return waymarkers.size();
  }

  public Waypoints waypoints() {
    final Waypoints p = new Waypoints();
    for (final OverlayItem o : waymarkers)
      p.add(o.getPoint());
    return p;
  }

  private IGeoPoint finish() {
    return waymarkersCount() > 1 ? waymarkers.get(waymarkersCount()-1).getPoint() : null;
  }

  private void addWaypoint(final IGeoPoint point) {
    if (point == null)
      return;
    switch(waymarkersCount()) {
    case 0:
      waymarkers.add(addMarker(point, "start", greenWisp));
      break;
    case 1:
      waymarkers.add(addMarker(point, "finish", redWisp));
      break;
    default:  {
        final IGeoPoint prevFinished = finish();
        waymarkers.remove(waymarkersCount()-1);
        waymarkers.add(addMarker(prevFinished, "waypoint", orangeWisp));
        waymarkers.add(addMarker(point, "finish", redWisp));
      }
    }
  }

  private void removeWaypoint() {
    switch(waymarkersCount()) {
    case 0:
        break;
    case 1:
    case 2:
        waymarkers.remove(waymarkersCount()-1);
        break;
    default:  {
        waymarkers.remove(waymarkersCount()-1);
        final IGeoPoint prevFinished = finish();
        waymarkers.remove(waymarkersCount()-1);
        waymarkers.add(addMarker(prevFinished, "finish", redWisp));
      }
    }
  }

  private OverlayItem addMarker(final IGeoPoint point, final String label, final Drawable icon) {
    if (point == null)
      return null;
    controller().pushUndo(this);
    final OverlayItem marker = new OverlayItem(label, label, new GeoPoint(point.getLatitude(), point.getLongitude()));
    marker.setMarker(icon);
    marker.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
    return marker;
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
  }

  @Override
  public void onPrepareOptionsMenu(final Menu menu) {
    showMenuItem(menu, R.string.route_menu_change, tapState == TapToRoute.ALL_DONE);
  }

  @Override
  public void onCreateContextMenu(final ContextMenu menu) {
    if (tapState != TapToRoute.ALL_DONE)
      return;

    final String currentPlan = Route.journey().plan();
    for (int id : Replan_Menu_Ids)
      if (!currentPlan.equals(Replan_Menu_Plans.get(id)))
        createMenuItem(menu, id);
    if (mapView.isMyLocationEnabled())
      createMenuItem(menu, R.string.route_menu_change_reroute_from_here);
    createMenuItem(menu, R.string.route_menu_change_reverse);
    createMenuItem(menu, R.string.route_menu_change_share);
    createMenuItem(menu, R.string.route_menu_change_comment);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, final MenuItem item) {
    final int menuId = item.getItemId();

    if (menuId == R.string.route_menu_change) {
      mapView.showContextMenu();
      return true;
    }

    if (Replan_Menu_Plans.containsKey(menuId)) {
      Route.RePlotRoute(Replan_Menu_Plans.get(menuId), context);
      return true;
    }

    if (R.string.route_menu_change_reroute_from_here == menuId) {
      final Location lastFix = mapView.getLastFix();
      if (lastFix == null) {
        Toast.makeText(mapView.getContext(), R.string.route_no_location, Toast.LENGTH_LONG).show();
        return true;
      }

      final GeoPoint from = new GeoPoint(lastFix.getLatitude(), lastFix.getLongitude());
      onRouteNow(Waypoints.fromTo(from, finish()));
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
    final IProjection projection = mapView.getProjection();
    for (final OverlayItem waypoint : waymarkers)
      drawMarker(canvas, projection, waypoint);
  }

  @Override
  public void drawButtons(final Canvas canvas, final MapView mapView) {
    drawTheButtons();
    drawTapState(canvas);
  }

  private void drawTheButtons() {
    if (tapState == TapToRoute.ALL_DONE) {
      restartButton.show();
    } else {
      restartButton.hide();
    }
  }

  private void drawTapState(final Canvas canvas) {
    final String msg = tapState.actionDescription;
    if (msg.length() == 0)
      return;

    final Rect screen = canvas.getClipBounds();
    screen.offset(tapStateRect.left, tapStateRect.top);
    screen.right -= tapStateRect.left;
    screen.bottom = screen.top + tapStateRect.height();

    tapStateRect.right = tapStateRect.left + screen.width();

    DrawingHelper.drawRoundRect(canvas, screen, radius, fillBrush());

    if (tapState == TapToRoute.WAITING_FOR_NEXT ||
       tapState == TapToRoute.WAITING_TO_ROUTE) {
      final Rect btn = new Rect(screen);
      btn.left = btn.right - canRoute.getWidth();
      DrawingHelper.drawBitmap(canvas, canRoute, btn);
      screen.right -= canRoute.getWidth();
    }

    screen.offset(screen.width()/2, 0);

    if (msg.indexOf('\n') == -1) {
      final Rect bounds = new Rect();
      textBrush.getTextBounds(msg, 0, 1, bounds);
      screen.offset(0, bounds.height());
    }

    Draw.drawTextInRect(canvas, textBrush, screen, msg);
  }

  private void drawMarker(final Canvas canvas,
                          final IProjection projection,
                          final OverlayItem marker) {
    if (marker == null)
      return;

    projection.toPixels(marker.getPoint(), screenPos);

    canvas.getMatrix(canvasTransform);
    canvasTransform.getValues(transformValues);

    final BitmapDrawable thingToDraw = (BitmapDrawable)marker.getDrawable();
    final int halfWidth = thingToDraw.getIntrinsicWidth()/2;
    final int halfHeight = thingToDraw.getIntrinsicHeight()/2;
    bitmapTransform.setTranslate(-halfWidth, -halfHeight);
    bitmapTransform.postScale(1/ transformValues[Matrix.MSCALE_X], 1/ transformValues[Matrix.MSCALE_Y]);
    bitmapTransform.postTranslate(screenPos.x, screenPos.y);
    canvas.drawBitmap(thingToDraw.getBitmap(), bitmapTransform, bitmapPaint);
  }

  private Paint fillBrush() {
    if (tapState == TapToRoute.WAITING_FOR_START ||
        tapState == TapToRoute.WAITING_FOR_SECOND)
      return lowlightBrush;
    return highlightBrush;
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

  @Override
  public boolean onButtonTap(final MotionEvent event) {
    // We're handling restart button taps via an onClickListener instead.
    return false;
  }

  @Override
  public boolean onButtonDoubleTap(final MotionEvent event) {
    return false;
  }

  private boolean tapRestart() {
    if (!restartButton.isShown())
      return false;

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
    final int x = (int)event.getX();
    final int y = (int)event.getY();
    final IGeoPoint p = mapView.getProjection().fromPixels(x, y);
    tapAction(x, y, p, true);
    return true;
  }

  public void setNextMarker(final IGeoPoint point) {
    tapAction(Integer.MIN_VALUE, Integer.MIN_VALUE, point, false);
  }

  private void tapAction(final int x, final int y, final IGeoPoint point, boolean tap) {
    switch(tapState) {
      case WAITING_FOR_START:
      case WAITING_FOR_SECOND:
        if (tapStateRect.contains(x, y))
          return;
        addWaypoint(point);
        break;
      case WAITING_FOR_NEXT:
        if (tapStateRect.contains(x, y)) {
          onRouteNow(waypoints());
          return;
        }
        addWaypoint(point);
        break;
      case WAITING_TO_ROUTE:
        if (!tap)
          return;
        if (!tapStateRect.contains(x, y))
          return;
        onRouteNow(waypoints());
        break;
      case ALL_DONE:
        break;
    }

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
      switch(this) {
        case WAITING_FOR_START:
          break;
        case WAITING_FOR_SECOND:
          return WAITING_FOR_START;
        case WAITING_FOR_NEXT:
          return (count == 1) ? WAITING_FOR_SECOND : WAITING_FOR_NEXT;
        case WAITING_TO_ROUTE:
          return WAITING_FOR_NEXT;
        case ALL_DONE:
          break;
      }
      return WAITING_FOR_START;
    }

    public TapToRoute next(final int count) {
      switch(this) {
        case WAITING_FOR_START:
          return WAITING_FOR_SECOND;
        case WAITING_FOR_SECOND:
          return WAITING_FOR_NEXT;
        case WAITING_FOR_NEXT:
          return (count == 12) ? WAITING_TO_ROUTE : WAITING_FOR_NEXT;
        case WAITING_TO_ROUTE:
          return ALL_DONE;
        case ALL_DONE:
          break;
      }
      return ALL_DONE;
    }
  }

  @Override
  public void onResume(SharedPreferences prefs) {
    Route.registerListener(this);
  }

  @Override
  public void onPause(Editor prefs) {
    Route.unregisterListener(this);
  }

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    setRoute(waypoints, !journey.isEmpty());
  }

  @Override
  public void onResetJourney() {
    resetRoute();
  }
}
