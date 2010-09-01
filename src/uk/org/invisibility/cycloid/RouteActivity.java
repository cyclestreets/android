package uk.org.invisibility.cycloid;

import net.cyclestreets.R;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;


public class RouteActivity extends Activity implements CycloidConstants, View.OnClickListener
{
	private static final int MENU_REVERSE_ID = Menu.FIRST;

	private static final int DIALOG_NO_FROM_ID = 1;
	private static final int DIALOG_NO_TO_ID = 2;
	
	private GeoAutoCompleteView routeFrom;
	private GeoAutoCompleteView routeTo;
	private RadioGroup routeTypeGroup;
	private String routeType;
	private Button routeGet;
	private RouteQuery routeQuery = new RouteQuery();
	private GeoPlace placeFrom;
	private GeoPlace placeTo;
	private ProgressDialog progress; 
	private GeoAdapter adapterFrom;
	private GeoAdapter adapterTo;
	private BoundingBoxE6 bounds;
	private GeoPlace myLocation;
	
    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);

        setContentView(R.layout.route);
    	getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        getWindow().setBackgroundDrawableResource(R.drawable.empty);
	
       
        bounds = GeoIntent.getBoundingBoxFromExtras(getIntent());

    	routeFrom = (GeoAutoCompleteView) findViewById(R.id.routeFrom);
    	routeTo   = (GeoAutoCompleteView) findViewById(R.id.routeTo);
        adapterFrom = new GeoAdapter(this, R.layout.geo_item_2line, routeFrom, bounds);
        adapterTo = new GeoAdapter(this, R.layout.geo_item_2line, routeTo, bounds);
    	routeFrom.setAdapter(adapterFrom);
    	routeTo.setAdapter(adapterTo);
    	
    	/*
    	 * If intent was supplied with a current location accept this as the
    	 * empty value for routeFrom
    	 */
    	Intent intent = getIntent();
    	if (intent.hasExtra(GEO_LATITUDE) && intent.hasExtra(GEO_LONGITUDE))
    	{
    		myLocation = new GeoPlace
    		(
				new GeoPoint
				(
					intent.getIntExtra(GEO_LATITUDE, 0),
					intent.getIntExtra(GEO_LONGITUDE, 0)
				),
				MY_LOCATION,
				""
    		);
    		routeFrom.setHint(R.string.my_location);
    		routeTo.requestFocus();
    	}
    	
    	routeGet = (Button) findViewById(R.id.routeGet);
    	routeGet.setOnClickListener(this);
    	
    	routeTypeGroup = (RadioGroup) findViewById(R.id.routeTypeGroup);
     	routeTypeGroup.setOnCheckedChangeListener(new TypeChangedListener());
    	routeTypeGroup.check(R.id.routeBalanced);
    	
    	progress = new ProgressDialog(RouteActivity.this);
		progress.setMessage(getString(R.string.finding_route));
		progress.setIndeterminate(true);  	
    }
    
    /*
     * Geocode results are handled here
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent  data)
    {
    	if (requestCode == GEO_REQUEST_FROM || requestCode == GEO_REQUEST_TO)
    	{
    		if (resultCode != Activity.RESULT_OK)
    		{
				if (requestCode == GEO_REQUEST_FROM)
					showDialog(DIALOG_NO_FROM_ID);
				else if (requestCode == GEO_REQUEST_TO)
					showDialog(DIALOG_NO_TO_ID);
     		}
    		else if (data != null && data.hasExtra(GEO_LATITUDE) && data.hasExtra(GEO_LONGITUDE))
			{
				int lat = data.getIntExtra(GEO_LATITUDE, 0);
				int lon = data.getIntExtra(GEO_LONGITUDE, 0);
				String near = data.getStringExtra(GEO_NEAR);
				
				//Log.e(LOGTAG, "Geocode result: lat: " + lat + " lon: " + lon);
	
				if (requestCode == GEO_REQUEST_FROM)
				{
					placeFrom = new GeoPlace(new GeoPoint(lat, lon), routeFrom.getText().toString(), near);
					findRoute();
				}
				else if (requestCode == GEO_REQUEST_TO)
				{
					placeTo = new GeoPlace(new GeoPoint(lat, lon), routeTo.getText().toString(), near);
					findRoute();
				}
			}
    	}
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
    	int msg = R.string.app_name;
        
    	switch (id)
        {
        case DIALOG_NO_FROM_ID:
            msg = R.string.no_from;
            break;
        case DIALOG_NO_TO_ID:
            msg = R.string.no_to;
            break;
        }
       	
        Dialog dialog = new AlertDialog.Builder(this)
        .setMessage(msg)        
        .setPositiveButton
        (
    		"OK",
    		new DialogInterface.OnClickListener()
    		{
    			@Override
    			public void onClick(DialogInterface dialog, int whichButton) {}
    		}
        ).create();
        return dialog;
    }
    	       	
    @Override
	public boolean onCreateOptionsMenu(final Menu pMenu)
    {
    	pMenu.add(0, MENU_REVERSE_ID, Menu.NONE, "Reverse").setIcon(android.R.drawable.ic_menu_rotate);
    	return true;
	}
    	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_REVERSE_ID:
				String tmp = routeFrom.getText().toString();
				routeFrom.setText(routeTo.getText());
				routeTo.setText(tmp);				
				return true;
		}
		return false;
	}

	/*
	 * Perform the next step in finding a route
	 */
	private void findRoute()
	{
		if (placeFrom == null || placeFrom.coord == null)
		{
			Intent intent = new Intent(RouteActivity.this, GeoActivity.class);
			intent.putExtra(GEO_SEARCH, routeFrom.getText().toString());
			intent.putExtra(GEO_TYPE, GEO_REQUEST_FROM);			
	    	startActivityForResult(intent, GEO_REQUEST_FROM);		
		}
		else if (placeTo == null || placeTo.coord == null)
		{
			Intent intent = new Intent(RouteActivity.this, GeoActivity.class);
			intent.putExtra(GEO_SEARCH, routeTo.getText().toString());
			intent.putExtra(GEO_TYPE, GEO_REQUEST_TO);			
	    	startActivityForResult(intent, GEO_REQUEST_TO);		
		}
		else
		{
			/*
			 * Store the route locations in the adapter history.
			 */
			this.adapterFrom.addHistory(placeFrom);
			this.adapterTo.addHistory(placeTo);
			
			/*
			 * Start async route query
			 */
			new RouteQueryTask();
		}
	}

	/*
	 * User clicked on "Go"
	 */
	@Override
	public void onClick(final View view)
	{
		String from = routeFrom.getText().toString();
		String to = routeTo.getText().toString();

		if (from.equals(""))
		{
			if (myLocation != null)
				placeFrom = myLocation;				
			else
			{
				Toast.makeText(RouteActivity.this, R.string.choose_from, Toast.LENGTH_SHORT).show();
				return;
			}
		}
		else
		{
			placeFrom = adapterFrom.getSelected();
		}
		
		if (to.equals(""))
		{
			Toast.makeText(RouteActivity.this, R.string.choose_to, Toast.LENGTH_SHORT).show();
			return;
		}
		else
		{
			placeTo = adapterTo.getSelected();
		}
		
		/*
		 * Initiate route finding. This may require geocoding etc
		 */
		findRoute();
	}

	public class RouteQueryTask extends AsyncTask<GeoPlace, Integer, RouteResult>
	{
		public RouteQueryTask()
		{
			progress.show();
			execute(RouteActivity.this.placeFrom, RouteActivity.this.placeTo);
		}

	    protected RouteResult doInBackground(GeoPlace... ps)
	    {
    		return RouteActivity.this.routeQuery.query(ps[0].coord, ps[1].coord, routeType);
	    }

	    protected void onPostExecute(RouteResult result)
	    {
    		progress.dismiss();
	    	if (!result.isValid())
	    	{
	    		Toast.makeText(RouteActivity.this, R.string.route_failed, Toast.LENGTH_SHORT).show();
	    		Log.e(LOGTAG, "Route result error: " + result.getError());
	    	}
	    	else
	    	{
		    	/* 
		    	 * Start a new activity to display the result
		    	 */
		    	Intent intent = new Intent(RouteActivity.this, MapActivity.class);
		    	intent.putExtra("route", result);
		    	startActivity(intent);
	    	}
	    }
	}
	

    private class TypeChangedListener implements RadioGroup.OnCheckedChangeListener
    {
		@Override
		public void onCheckedChanged(RadioGroup group, int checked) {
			if (checked == R.id.routeQuietest)
				routeType = RouteQuery.PLAN_QUIETEST;
			else if (checked == R.id.routeBalanced)
				routeType = RouteQuery.PLAN_BALANCED;
			else if (checked == R.id.routeFastest)
				routeType = RouteQuery.PLAN_FASTEST;	
		}
    };
}
