package net.cyclestreets;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.R;
import net.cyclestreets.RouteMapActivity;
import net.cyclestreets.util.RouteTypeMapper;
import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.views.PlaceView;
import net.cyclestreets.api.GeoPlace;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout.LayoutParams;

public class RouteByAddressActivity extends Activity 
						   implements View.OnClickListener
{
  private LinearLayout placeHolder_;
	private List<PlaceView> places_;
	private RadioGroup routeType_;
	private Button routeGo_;
	private Button addWaypoint_;
	
	private BoundingBoxE6 bounds_;
	private GeoPoint currentLoc_;
	private List<GeoPoint> waypoints_;
	
	@Override
	public void onCreate(final Bundle saved)
	{
	  super.onCreate(saved);

	  setContentView(R.layout.routebyaddress);
	  getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
	  getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	  getWindow().setBackgroundDrawableResource(R.drawable.empty);

    final Intent intent = getIntent();
    bounds_ = GeoIntent.getBoundingBox(intent);
    currentLoc_ = GeoIntent.getGeoPoint(intent);
	  
	  placeHolder_ = (LinearLayout)findViewById(R.id.places);
	       
	  places_ = new ArrayList<PlaceView>();
	  
	  waypoints_ = new ArrayList<GeoPoint>();
	  for(int w = 0; ; ++w)
	  {
	    final GeoPoint wp = GeoIntent.getGeoPoint(intent, "WP" + w);

	    if(wp == null)
	      break;

	    waypoints_.add(wp);
	  } // for ...
	    
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
	  final PlaceView pv = new PlaceView(this);
	  
    pv.setBounds(bounds_);

    if(currentLoc_ != null)
      pv.allowCurrentLocation(currentLoc_, places_.size() == 0);
	
    for(int w = 0; w != waypoints_.size(); ++w)
    {
      String label = "Waypoint " + w;
    
      if(w == 0)
        label = "Start marker";
      else if(w+1 == waypoints_.size())
        label = "Finish marker";

      pv.allowLocation(waypoints_.get(w), label);
    } // for ...
        
    places_.add(pv);
    placeHolder_.addView(pv);

    addWaypoint_.setEnabled(places_.size() < 12);
	} // addWaypointBox
	
	private void findRoute(final List<GeoPlace> waypoints)
	{
	  for(final GeoPlace wp : waypoints)
	    for(final PlaceView p : places_)
	      p.addHistory(wp);
			
		// return start and finish points to RouteMapActivity and close
		final Intent intent = new Intent(RouteByAddressActivity.this, RouteMapActivity.class);
	  for(int i = 0; i != waypoints.size(); ++i)      
	    GeoIntent.setGeoPoint(intent, "WP" + i, waypoints.get(i).coord());
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
    if(index != places_.size())
      places_.get(index).geoPlace(new PlaceView.OnResolveListener() {
        @Override
        public void onResolve(GeoPlace place)
        {
          resolvedPlaces.add(place);
          resolveNextPlace(resolvedPlaces, index+1);
        }
      });
    else
      findRoute(resolvedPlaces);
  } // resolveNextPlace
} // RouteActivity
