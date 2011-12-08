package net.cyclestreets.views.overlay;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import net.cyclestreets.CycleStreetsConstants;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.FeedbackActivity;
import net.cyclestreets.R;
import net.cyclestreets.planned.Route;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.Share;
import net.cyclestreets.views.CycleMapView;
import net.cyclestreets.util.Collections;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class TapToRouteOverlay extends Overlay 
                               implements ButtonTapListener,
                                          TapListener,
                                          ContextMenuListener, 
                                          DynamicMenuListener, 
                                          UndoAction
{
  public interface Callback 
  {
    void onRouteNow(final List<GeoPoint> waypoints);
    void reRouteNow(final String plan);
    void onClearRoute();
  } // Callback

  private static int[] Replan_Menu_Ids = { R.string.ic_menu_replan_quietest, 
                                           R.string.ic_menu_replan_balanced, 
                                           R.string.ic_menu_replan_fastest,
                                           R.string.ic_menu_replan_shortest };
  private static Map<Integer, String> Replan_Menu_Plans = 
      Collections.map(R.string.ic_menu_replan_quietest, CycleStreetsConstants.PLAN_QUIETEST)
                 .map(R.string.ic_menu_replan_balanced, CycleStreetsConstants.PLAN_BALANCED)
                 .map(R.string.ic_menu_replan_fastest, CycleStreetsConstants.PLAN_FASTEST)
                 .map(R.string.ic_menu_replan_shortest, CycleStreetsConstants.PLAN_SHORTEST);
  
  private final Drawable greenWisp_;
  private final Drawable orangeWisp_;
  private final Drawable redWisp_;
  private final Point screenPos_ = new Point();
  private final Matrix canvasTransform_ = new Matrix();
  private final float[] transformValues_ = new float[9];
  private final Matrix bitmapTransform_ = new Matrix();
  private final Paint bitmapPaint_ = new Paint();

  private final int offset_;
  private final float radius_;

  private final OverlayButton stepBackButton_;
  private final OverlayButton restartButton_;

  private final CycleMapView mapView_;
  
  private final Callback callback_;

  private List<OverlayItem> waypoints_;
  private Rect tapStateRect_;

  private Paint textBrush_;

  private TapToRoute tapState_;
  
  private OverlayHelper overlays_;
  
  public TapToRouteOverlay(final Context context, 
                           final CycleMapView mapView,
                           final Callback callback) 
  {
    super(context);
    
    mapView_ = mapView;
    callback_ = callback;
    
    final Resources res = context.getResources();
    greenWisp_ = res.getDrawable(R.drawable.green_wisp_shadow_centred_big);
    orangeWisp_ = res.getDrawable(R.drawable.orange_wisp_shadow_centred_big);
    redWisp_ = res.getDrawable(R.drawable.red_wisp_shadow_centred_big);

    offset_ = DrawingHelper.offset(context);
    radius_ = DrawingHelper.cornerRadius(context);

    stepBackButton_ = new OverlayButton(res.getDrawable(R.drawable.ic_menu_revert),
                          offset_,
                          offset_*2,
                          radius_);
    stepBackButton_.bottomAlign();
    restartButton_ = new OverlayButton(res.getDrawable(R.drawable.ic_menu_rotate),
                           0, 
                           offset_*2,
                           radius_);
    restartButton_.centreAlign();
    restartButton_.bottomAlign();
    
    tapStateRect_ = new Rect();
    tapStateRect_.left = stepBackButton_.right() + offset_; 
    tapStateRect_.top = offset_;
    tapStateRect_.bottom = tapStateRect_.top + stepBackButton_.height();
        
    textBrush_ = Brush.createTextBrush(offset_);
        
    waypoints_ = new ArrayList<OverlayItem>();
    
    tapState_ = TapToRoute.start();
    
    overlays_ = new OverlayHelper(mapView);
  } // LocationOverlay
  
  private ControllerOverlay controller()
  {
    return overlays_.controller();
  } // controller
  
  public void setRoute(final GeoPoint start, final GeoPoint end, final boolean complete)
  {
    resetRoute();
    
    addWaypoint(start);
    addWaypoint(end);

    tapState_ = tapState_.reset();
    if(start == null)
      return;    
    tapState_ = tapState_.next(waypointsCount());
    if(end == null)
      return;
    tapState_ = tapState_.next(waypointsCount());
    if(!complete)
      return;
    controller().flushUndo(this);
    tapState_ = TapToRoute.ALL_DONE;
  } // setRoute
  
  public void resetRoute()
  {
    waypoints_.clear();
    tapState_ = tapState_.reset();
    controller().flushUndo(this);
  } // resetRoute
  
  public GeoPoint getStart() 
  { 
    return waypointsCount() > 0 ? waypoints_.get(0).getPoint() : null; 
  } // getStart
  public GeoPoint getFinish() 
  { 
    return waypointsCount() > 1 ? waypoints_.get(waypointsCount()-1).getPoint() : null;
  } // getFinish
  
  private int waypointsCount()
  {
    return waypoints_.size();
  } // waypointsCount
  private List<GeoPoint> geoPoints()
  {
    final List<GeoPoint> p = new ArrayList<GeoPoint>();
    for(final OverlayItem o : waypoints_)
      p.add(o.getPoint());
    return p;
  } // geoPoints

  private void addWaypoint(final GeoPoint point)
  {
    if(point == null)
      return;
    switch(waypointsCount())
    {
    case 0:
      waypoints_.add(addMarker(point, "start", greenWisp_));
      break;
    case 1:
      waypoints_.add(addMarker(point, "finish", redWisp_));
      break;
    default:
      {
        final GeoPoint prevFinished = getFinish();
        waypoints_.remove(waypointsCount()-1);
        waypoints_.add(addMarker(prevFinished, "waypoint", orangeWisp_));
        waypoints_.add(addMarker(point, "finish", redWisp_));
      } // default
    } // switch ...
  } // addWayPoint
  
  private void removeWaypoint()
  {
    switch(waypointsCount())
    {
    case 0:
        break;
    case 1:
    case 2:
        waypoints_.remove(waypointsCount()-1);
        break;
    default:
      {
        waypoints_.remove(waypointsCount()-1);
        final GeoPoint prevFinished = getFinish();
        waypoints_.remove(waypointsCount()-1);
        waypoints_.add(addMarker(prevFinished, "finish", redWisp_));
      } // default
    }
  }
  private OverlayItem addMarker(final GeoPoint point, final String label, final Drawable icon)
  {
    if(point == null)
      return null;
    controller().pushUndo(this);
    final OverlayItem marker = new OverlayItem(label, label, point);
    marker.setMarker(icon);
    marker.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
    return marker;
  } // addMarker

  ////////////////////////////////////////////
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) 
  { 
    menu.add(0, R.string.ic_menu_replan, Menu.NONE, R.string.ic_menu_replan).setIcon(R.drawable.ic_menu_more);
    return true;
  } // onCreateOptionsMenu
  
  @Override
  public boolean onPrepareOptionsMenu(final Menu menu)
  {    
    final MenuItem i = menu.findItem(R.string.ic_menu_replan);
    i.setVisible(tapState_ == TapToRoute.ALL_DONE);
    return true;
  } // onPrepareOptionsMenu
  
  @Override
  public void onCreateContextMenu(final ContextMenu menu) 
  {
    if(tapState_ != TapToRoute.ALL_DONE)
      return;

    final String currentPlan = Route.planned().plan();
    for(int id : Replan_Menu_Ids)
      if(!currentPlan.equals(Replan_Menu_Plans.get(id)))
          add(menu, id);
    if(mapView_.isMyLocationEnabled())
      add(menu, R.string.ic_menu_reroute_from_here);
    add(menu, R.string.ic_menu_reverse);
    add(menu, R.string.ic_menu_share);
    add(menu, R.string.ic_menu_feedback);
  } // onCreateContextMenu
  
  private void add(ContextMenu menu, int id)
  {
    menu.add(0, id, 0, id);
  } // add

  @Override
  public boolean onMenuItemSelected(int featureId, final MenuItem item)
  {
    final int menuId = item.getItemId();
    
    if(menuId == R.string.ic_menu_replan)
    {
      mapView_.showContextMenu();
      return false;
    } // if ...  
    
    if(Replan_Menu_Plans.containsKey(menuId))
    {
      callback_.reRouteNow(Replan_Menu_Plans.get(menuId));
      return false;
    } // if ...
    
    switch(menuId)
    {
      case R.string.ic_menu_reroute_from_here:
      {
        final Location lastFix = mapView_.getLastFix();
        final GeoPoint from = new GeoPoint((int)(lastFix.getLatitude() * 1E6),
                                           (int)(lastFix.getLongitude() * 1E6));
        callback_.onRouteNow(Collections.list(from, getFinish()));
      }
      break;
    case R.string.ic_menu_reverse:
      {
        final List<GeoPoint> points = geoPoints();
        java.util.Collections.reverse(points);
        callback_.onRouteNow(points);
      }
      break;
    case R.string.ic_menu_share:
      Share.Url(mapView_, 
                Route.planned().url(), 
                Route.planned().name(),
                "CycleStreets journey");
      break;
    case R.string.ic_menu_feedback:
      {
        final Context context = mapView_.getContext();
        context.startActivity(new Intent(context, FeedbackActivity.class));
      }
      break;
    } // switch(featureId)
    return false;
  } // onMenuItemSelected
  
  ////////////////////////////////////////////
  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
  {
    final Projection projection = mapView.getProjection();
    for(final OverlayItem waypoint : waypoints_)
      drawMarker(canvas, projection, waypoint);
  } // draw

  @Override
  public void drawButtons(final Canvas canvas, final MapView mapView)
  {
    drawButtons(canvas);
    drawTapState(canvas);
  } // drawButtons
  
  private void drawButtons(final Canvas canvas)
  {
    stepBackButton_.enable(tapState_ == TapToRoute.WAITING_FOR_SECOND || 
                           tapState_ == TapToRoute.WAITING_FOR_NEXT || 
                           tapState_ == TapToRoute.WAITING_TO_ROUTE);
    restartButton_.enable(tapState_ == TapToRoute.ALL_DONE);

    if(tapState_ != TapToRoute.ALL_DONE)
      stepBackButton_.draw(canvas);
    else
      restartButton_.draw(canvas);
  } // drawLocationButton

  private void drawTapState(final Canvas canvas)
  {
    final String msg = tapState_.toString();
    if(msg.length() == 0)
      return;
        
    final Rect screen = canvas.getClipBounds();
    screen.offset(tapStateRect_.left, tapStateRect_.top);
    screen.right -= (tapStateRect_.left + offset_);
    screen.bottom = screen.top + tapStateRect_.height();

    tapStateRect_.right = tapStateRect_.left + screen.width();
    
    if(!DrawingHelper.drawRoundRect(canvas, screen, radius_, Brush.Grey))
      return;

    screen.offset(screen.width()/2, 0);
    Draw.drawTextInRect(canvas, textBrush_, screen, msg);
    //final Rect bounds = new Rect();
    //textBrush_.getTextBounds(msg, 0, msg.length(), bounds);
    //canvas.drawText(msg, screen.centerX(), screen.centerY() + bounds.bottom, textBrush_);
  } // drawTapState
  
  private void drawMarker(final Canvas canvas, 
                          final Projection projection,
                          final OverlayItem marker)
  {
    if(marker == null)
      return;

    projection.toMapPixels(marker.mGeoPoint, screenPos_);
    
    canvas.getMatrix(canvasTransform_);
    canvasTransform_.getValues(transformValues_);
    
    final BitmapDrawable thingToDraw = (BitmapDrawable)marker.getDrawable();
    final int halfWidth = thingToDraw.getIntrinsicWidth()/2;
    final int halfHeight = thingToDraw.getIntrinsicHeight()/2;
    bitmapTransform_.setTranslate(-halfWidth, -halfHeight);
    bitmapTransform_.postScale(1/transformValues_[Matrix.MSCALE_X], 1/transformValues_[Matrix.MSCALE_Y]);
    bitmapTransform_.postTranslate(screenPos_.x, screenPos_.y);
    canvas.drawBitmap(thingToDraw.getBitmap(), bitmapTransform_, bitmapPaint_);
  } // drawMarker

  //////////////////////////////////////////////
  @Override 
  public boolean onSingleTap(final MotionEvent event)
  {
    return tapMarker(event);
  } // onSingleTap
  
  @Override
  public boolean onDoubleTap(final MotionEvent event)
  {
    return false;
  } // onDoubleTap
  
  @Override
  public boolean onButtonTap(final MotionEvent event) 
  {
    return tapStepBack(event) || 
           tapRestart(event);
  } // onSingleTapUp
  
  @Override
  public boolean onButtonDoubleTap(final MotionEvent event)
  {
    return stepBackButton_.hit(event);
  } // onDoubleTap
    
  private boolean tapStepBack(final MotionEvent event)
  {
    if(!stepBackButton_.hit(event))
      return false;
    if(!stepBackButton_.enabled())
      return true;
    
    controller().popUndo(this);
    return stepBack(true);
  } // tapStepBack
  
  private boolean tapRestart(final MotionEvent event)
  {
    if(!restartButton_.enabled() || !restartButton_.hit(event))
      return false;

    if(!CycleStreetsPreferences.confirmNewRoute())
      return stepBack(true);
    
    MessageBox.YesNo(mapView_,
                     "Start a new route?",
                     new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface arg0, int arg1) {
                         stepBack(true);
                       }
                     });
        
    return true;
  } // tapRestart
  
  public void onBackPressed()
  {
    stepBack(false);
  } // onBackPressed
  
  private boolean stepBack(final boolean tap)
  {
    if(!tap && tapState_.isAtEnd())
      return false;
        
    switch(tapState_)
    {
      case WAITING_FOR_START:
        return true;
      case WAITING_FOR_SECOND:
      case WAITING_FOR_NEXT:
        removeWaypoint();
        break;
      case ALL_DONE:
        callback_.onClearRoute();
        break;
    } // switch ...
    
    tapState_ = tapState_.previous(waypointsCount());
    mapView_.invalidate();
    
    return true;
  } // tapStepBack

  private boolean tapMarker(final MotionEvent event)
  {
    final int x = (int)event.getX();
    final int y = (int)event.getY();
    final GeoPoint p = mapView_.getProjection().fromPixels(x, y);
    tapAction(x, y, p, true);    
    return true;
  } // tapMarker
   
  public void setNextMarker(final GeoPoint point)
  {
    tapAction(Integer.MIN_VALUE, Integer.MIN_VALUE, point, false);
  } // setNextMarker
  
  private void tapAction(final int x, final int y, final GeoPoint point, boolean tap)
  {
    switch(tapState_)
    {
      case WAITING_FOR_START:
      case WAITING_FOR_SECOND:
        if(tapStateRect_.contains(x, y))
          return;
        addWaypoint(point);
        break;
      case WAITING_FOR_NEXT:
        if(tapStateRect_.contains(x, y))
          callback_.onRouteNow(geoPoints());
        addWaypoint(point);
        break;
      case WAITING_TO_ROUTE:
        if(!tap)
          return;
        if(!tapStateRect_.contains(x, y))
          return;
        callback_.onRouteNow(geoPoints());
        break;
      case ALL_DONE:
        break;
    } // switch ...

    tapState_ = tapState_.next(waypointsCount());
    mapView_.invalidate();
} // tapMarker
  
  ////////////////////////////////////
  private enum TapToRoute 
  { 
    WAITING_FOR_START, 
    WAITING_FOR_SECOND,
    WAITING_FOR_NEXT, 
    WAITING_TO_ROUTE, 
    ALL_DONE;
    
    static public TapToRoute start()
    {
      return WAITING_FOR_START;
    } // start
    
    public TapToRoute reset()
    {
      return WAITING_FOR_START;
    } // reset
    
    public boolean isAtEnd()
    {
      return (this == WAITING_FOR_START) ||
             (this == ALL_DONE);
    } // isAtEnd
    
    public TapToRoute previous(final int count) 
    {
      switch(this) 
      {
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
      } // switch
      return WAITING_FOR_START;        
    } // previous()

    public TapToRoute next(final int count) 
    {
      switch(this) 
      {
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
      } // switch
      return ALL_DONE;        
    } // next()
    
    public String toString()
    {
      switch(this) 
      {
        case WAITING_FOR_START:
          return "Tap map to set Start";
        case WAITING_FOR_SECOND:
          return "Tap map to Waypoint";
        case WAITING_FOR_NEXT:
          return "Tap map to Waypoint\nTap here to Route";
        case WAITING_TO_ROUTE:
          return "Tap here to Route";
        case ALL_DONE:
          break;
      } // switch
      return "";        
    } // toString
  }; // enum TapToRoute
} // LocationOverlay
