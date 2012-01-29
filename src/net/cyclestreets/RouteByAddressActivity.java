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
import android.view.LayoutInflater;
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
	
	@Override
	public void onCreate(final Bundle saved)
	{
	  super.onCreate(saved);

	  setContentView(R.layout.routebyaddress);
	  getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
	  getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	  getWindow().setBackgroundDrawableResource(R.drawable.empty);

    final Intent intent = getIntent();
    final BoundingBoxE6 bounds = GeoIntent.getBoundingBox(intent);
    final GeoPoint loc = GeoIntent.getGeoPoint(intent);
	  
	  placeHolder_ = (LinearLayout)findViewById(R.id.places);
	       
	  places_ = new ArrayList<PlaceView>();
	  
	  for(int i = 0; i != 2; ++i)
	  {
	    places_.add(new PlaceView(this));
	  
	    placeHolder_.addView(places_.get(i));
	  
	    places_.get(i).setBounds(bounds);

	    if(loc != null)
	      places_.get(i).allowCurrentLocation(loc);
	  } // for ...
	  
	  for(int waypoints = 0; ; ++waypoints )
	  {
	    final GeoPoint wp = GeoIntent.getGeoPoint(intent, "WP" + waypoints);
	    final GeoPoint wpNext = GeoIntent.getGeoPoint(intent, "WP" + (waypoints+1));

	    if(wp == null)
	      break;

	    String label = "Waypoint " + waypoints;
	    
	    if(waypoints == 0)
	      label = "Start marker";
	    else if(wpNext == null)
	      label = "Finish marker";

	    for(final PlaceView p : places_)
	      p.allowLocation(wp, label);
	  } // for ...
	  
	  routeGo_ = (Button) findViewById(R.id.routeGo);
	  routeGo_.setOnClickListener(this);
	  
	  routeType_ = (RadioGroup) findViewById(R.id.routeTypeGroup);
	  routeType_.check(RouteTypeMapper.idFromName(CycleStreetsPreferences.routeType()));  	
  } // RouteActivity
    
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
		resolvePlaces();
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
