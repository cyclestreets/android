package net.cyclestreets;

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
import android.widget.RadioGroup;
import android.widget.RelativeLayout.LayoutParams;

public class RouteActivity extends Activity 
						   implements View.OnClickListener
{
	private PlaceView placeFrom_;
	private PlaceView placeTo_;
	private RadioGroup routeTypeGroup;
	private Button routeGo;
	
	@Override
	public void onCreate(Bundle saved)
	{
	  super.onCreate(saved);

	  setContentView(R.layout.route);
	  getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
	  getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	  getWindow().setBackgroundDrawableResource(R.drawable.empty);
	       
	  placeFrom_ = (PlaceView)findViewById(R.id.placeFrom);
	  placeTo_ = (PlaceView)findViewById(R.id.placeTo);
	  
	  final BoundingBoxE6 bounds = GeoIntent.getBoundingBox(getIntent());
	  placeFrom_.setBounds(bounds);
	  placeTo_.setBounds(bounds);

	  final Intent intent = getIntent();
	  final GeoPoint loc = GeoIntent.getGeoPoint(intent);
	  if(loc != null)
	  {
	    placeFrom_.allowCurrentLocation(loc);
	    placeTo_.requestFocus();
    } // if ...
    	
	  final GeoPoint start = GeoIntent.getGeoPoint(intent, "START");
	  final GeoPoint finish = GeoIntent.getGeoPoint(intent, "FINISH");
	  placeFrom_.allowLocation(start, "Start marker");
	  placeTo_.allowLocation(finish, "Finish marker");
	  placeTo_.allowLocation(start, "Start marker");
	  placeFrom_.allowLocation(finish, "Finish marker");
    	
	  routeGo = (Button) findViewById(R.id.routeGo);
	  routeGo.setOnClickListener(this);
	  
	  routeTypeGroup = (RadioGroup) findViewById(R.id.routeTypeGroup);
	  routeTypeGroup.check(RouteTypeMapper.idFromName(CycleStreetsPreferences.routeType()));  	
  } // RouteActivity
    
	private void resolvePlaces()
	{
		placeFrom_.geoPlace(new PlaceView.OnResolveListener() {
			public void onResolve(final GeoPlace f) {
				placeTo_.geoPlace(new PlaceView.OnResolveListener() {
					public void onResolve(final GeoPlace t) {
						findRoute(f, t);
					} // onResolve
				}); // to listener
			} // onResolve
		}); // from listener
	} // resolvePlaces
	
	private void findRoute(final GeoPlace from, final GeoPlace to)
	{
		placeFrom_.addHistory(from);
		placeTo_.addHistory(to);
			
		// return start and finish points to RouteMapActivity and close
		final Intent intent = new Intent(RouteActivity.this, RouteMapActivity.class);
		GeoIntent.setGeoPoint(intent, "FROM", from.coord());
		GeoIntent.setGeoPoint(intent, "TO", to.coord());
		final String routeType = RouteTypeMapper.nameFromId(routeTypeGroup.getCheckedRadioButtonId());
		intent.putExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE, routeType);
		setResult(RESULT_OK, intent);
		finish();
	} // findRoute

	@Override
	public void onClick(final View view)
	{
		resolvePlaces();
	} // onClick
} // RouteActivity
