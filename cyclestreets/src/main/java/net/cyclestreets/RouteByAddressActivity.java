package net.cyclestreets;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.views.PlaceViewWithCancel;
import net.cyclestreets.api.GeoPlace;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout.LayoutParams;

public class RouteByAddressActivity extends Activity 
               implements View.OnClickListener
{
  private LinearLayout placeHolder_;
  private RadioGroup routeType_;
  private Button routeGo_;
  private Button addWaypoint_;
  
  private BoundingBoxE6 bounds_;
  private GeoPoint currentLoc_;
  private Waypoints waypoints_;
  
  private String START_MARKER_LABEL;
  private String FINISH_MARKER_LABEL;
  private String WAYPOINT_LABEL;
  
  @Override
  public void onCreate(final Bundle saved)
  {
    super.onCreate(saved);

    START_MARKER_LABEL = getResources().getString(R.string.rba_start);
    FINISH_MARKER_LABEL = getResources().getString(R.string.rba_finish);
    WAYPOINT_LABEL = getResources().getString(R.string.rba_waypoint);
    
    setContentView(R.layout.routebyaddress);
    getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    getWindow().setBackgroundDrawableResource(R.drawable.empty);

    final Intent intent = getIntent();
    bounds_ = GeoIntent.getBoundingBox(intent);
    currentLoc_ = GeoIntent.getGeoPoint(intent);
    
    placeHolder_ = (LinearLayout)findViewById(R.id.places);
         
    waypoints_ = GeoIntent.getWaypoints(intent);
    
    routeGo_ = (Button) findViewById(R.id.routeGo);
    routeGo_.setOnClickListener(this);
    
    addWaypoint_ = (Button)findViewById(R.id.addVia);
    addWaypoint_.setOnClickListener(this);
    
    routeType_ = (RadioGroup) findViewById(R.id.routeTypeGroup);
    routeType_.check(RouteTypeMapper.idFromName(CycleStreetsPreferences.routeType()));
    
    addWaypointBox();
    addWaypointBox();
  } // RouteActivity

  private void addWaypointBox()
  {
    final PlaceViewWithCancel pv = new PlaceViewWithCancel(this);
    pv.setBounds(bounds_);

    if(currentLoc_ != null)
      pv.allowCurrentLocation(currentLoc_, placeHolder_.getChildCount() == 0);
      
    for(int w = 0; w != waypoints_.count(); ++w)
    {
      String label = String.format(WAYPOINT_LABEL, w);
    
      if(w == 0)
        label = START_MARKER_LABEL;
      else if(w+1 == waypoints_.count())
        label = FINISH_MARKER_LABEL;

      pv.allowLocation(waypoints_.get(w), label);
    } // for ...

    pv.setCancelOnClick(new OnRemove(pv));
    
    placeHolder_.addView(pv);
    pv.requestFocus();

    enableRemoveButtons();
  } // addWaypointBox
  
  private void removeWaypointBox(final PlaceViewWithCancel pv)
  {
   	placeHolder_.removeView(pv);
   	enableRemoveButtons();
  } // removeWaypointBox
  
  private void enableRemoveButtons()
  {
    final boolean enable = placeHolder_.getChildCount() > 2;

    for(int i = 0; i != placeHolder_.getChildCount(); ++i)
    {
      final PlaceViewWithCancel p = (PlaceViewWithCancel)placeHolder_.getChildAt(i);
      p.enableCancel(enable);
    } // for ...
    
    addWaypoint_.setEnabled(placeHolder_.getChildCount() < 12);
} // enableRemoveButtons
  
  private void findRoute(final List<GeoPlace> waypoints)
  {
    for(final GeoPlace wp : waypoints)
      for(int i = 0; i != placeHolder_.getChildCount(); ++i)
      {
        final PlaceViewWithCancel p = (PlaceViewWithCancel)placeHolder_.getChildAt(i);
        p.addHistory(wp);
      } // for ...
      
    // return start and finish points to RouteMapActivity and close
    final Intent intent = new Intent(RouteByAddressActivity.this, RouteMapFragment.class);
    GeoIntent.setWaypointsFromPlaces(intent, waypoints);
    final String routeType = RouteTypeMapper.nameFromId(routeType_.getCheckedRadioButtonId());
    intent.putExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE, routeType);
    setResult(RESULT_OK, intent);
    finish();
  } // findRoute

  @Override
  public void onClick(final View view)
  {
    switch(view.getId())
    {
    case R.id.routeGo:
      resolvePlaces();
      break;
    case R.id.addVia:
      addWaypointBox();
      break;
    } // switch
  } // onClick

  private void resolvePlaces()
  {
    resolveNextPlace(new ArrayList<GeoPlace>(), 0);
  } // resolvePlaces
  
  private void resolveNextPlace(final List<GeoPlace> resolvedPlaces, final int index)
  {
    if(index != placeHolder_.getChildCount())
    {
      final PlaceViewWithCancel pv = (PlaceViewWithCancel)placeHolder_.getChildAt(index);
      pv.geoPlace(new PlaceViewWithCancel.OnResolveListener() {
        @Override
        public void onResolve(GeoPlace place)
        {
          resolvedPlaces.add(place);
          resolveNextPlace(resolvedPlaces, index+1);
        }
      });
    }
    else
      findRoute(resolvedPlaces);
  } // resolveNextPlace
  
  private class OnRemove implements OnClickListener
  {
	private final PlaceViewWithCancel pv_;
	public OnRemove(final PlaceViewWithCancel pv)
	{
	  pv_ = pv;
	} // OnRemove
	
	@Override
	public void onClick(final View view) 
	{
	  removeWaypointBox(pv_);
	} // onClick
  } // class OnRemove
} // RouteActivity
