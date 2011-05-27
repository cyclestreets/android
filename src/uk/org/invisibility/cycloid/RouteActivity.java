package uk.org.invisibility.cycloid;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.R;
import net.cyclestreets.RouteMapActivity;
import net.cyclestreets.util.RouteTypeMapper;
import net.cyclestreets.api.GeoLiveAdapter;
import net.cyclestreets.api.GeoPlace;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class RouteActivity extends Activity implements
		View.OnClickListener, DialogInterface.OnClickListener {
	private static final int MENU_REVERSE_ID = Menu.FIRST;

	private static final int DIALOG_NO_FROM_ID = 1;
	private static final int DIALOG_NO_TO_ID = 2;
	protected static final int DIALOG_CHOOSE_START = 3;
	protected static final int DIALOG_CHOOSE_END = 4;
	
	private GeoAutoCompleteView routeFrom;
	private GeoAutoCompleteView routeTo;
	private ImageButton optionsFrom;
	private ImageButton optionsTo;
	private RadioGroup routeTypeGroup;
	private Button routeGo;
	private GeoPlace myLocation;
	
    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);

        setContentView(R.layout.route);
    	getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        getWindow().setBackgroundDrawableResource(R.drawable.empty);
	       
    	routeFrom = (GeoAutoCompleteView)findViewById(R.id.routeFrom);
    	routeTo   = (GeoAutoCompleteView)findViewById(R.id.routeTo);
    	
    	final BoundingBoxE6 bounds = GeoIntent.getBoundingBox(getIntent());
    	routeFrom.setBounds(bounds);
    	routeTo.setBounds(bounds);

    	optionsFrom = (ImageButton) findViewById(R.id.optionsFrom);
    	optionsTo = (ImageButton) findViewById(R.id.optionsTo);
    	
    	/*
    	 * If intent was supplied with a current location accept this as the
    	 * empty value for routeFrom
    	 */
    	Intent intent = getIntent();
    	GeoPoint loc = GeoIntent.getGeoPoint(intent);
    	if(loc != null)
    	{
    		myLocation = new GeoPlace(loc, GeoLiveAdapter.MY_LOCATION, "");
    		routeFrom.setHint(GeoLiveAdapter.MY_LOCATION);
    		routeTo.requestFocus();
    	}

    	optionsFrom.setOnClickListener(new EntryOptionListener());
    	optionsTo.setOnClickListener(new EntryOptionListener());
    	
    	routeGo = (Button) findViewById(R.id.routeGo);
    	routeGo.setOnClickListener(this);

    	routeTypeGroup = (RadioGroup) findViewById(R.id.routeTypeGroup);
    	routeTypeGroup.check(RouteTypeMapper.idFromName(CycleStreetsPreferences.routeType()));  	
    }
    
    /*
     * Geocode results are handled here
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent  data)
    {
    	if (requestCode == CycloidConstants.GEO_REQUEST_FROM || requestCode == CycloidConstants.GEO_REQUEST_TO)
    	{
    		if (resultCode != Activity.RESULT_OK)
    		{
				if (requestCode == CycloidConstants.GEO_REQUEST_FROM)
					showDialog(DIALOG_NO_FROM_ID);
				else if (requestCode == CycloidConstants.GEO_REQUEST_TO)
					showDialog(DIALOG_NO_TO_ID);
     		}
    		else if (data != null)
			{
    			GeoPlace place = GeoIntent.getGeoPlace(data);
    			if(place != null)
    			{
    				if (requestCode == CycloidConstants.GEO_REQUEST_FROM)
    					routeFrom.setGeoPlace(place); 
    				if (requestCode == CycloidConstants.GEO_REQUEST_TO)
    					routeTo.setGeoPlace(place);
    			} // point
			}
    	}
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DIALOG_NO_FROM_ID:
    		return createErrorDialog(R.string.no_from);

    	case DIALOG_NO_TO_ID:
    		return createErrorDialog(R.string.no_to);
    		
    	case DIALOG_CHOOSE_START:
    		return createChoosePointDialog(R.string.choose_start, true);

    	case DIALOG_CHOOSE_END:
    		return createChoosePointDialog(R.string.choose_end, false);
    	}

    	// should not be reached
    	return null;
    }

    protected Dialog createErrorDialog(int msg) {
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
    
    protected Dialog createChoosePointDialog(int title, boolean allowCurrentLocation) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(title);
    	if (allowCurrentLocation) {
    		builder.setItems(R.array.point_type3, this);
    	}
    	else {
    		builder.setItems(R.array.point_type, this);
    	}
    	return builder.create();
    }
    
    @Override
	public void onClick(DialogInterface dialog, int which) {
    	Log.d(getClass().getSimpleName(), "selected: " + which);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu)
    {
    	pMenu.add(0, MENU_REVERSE_ID, Menu.NONE, "Reverse").setIcon(R.drawable.ic_menu_rotate);
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
		if (routeFrom.geoPlace() == null)
		{
			Intent intent = new Intent(RouteActivity.this, GeoActivity.class);
			intent.putExtra(CycloidConstants.GEO_SEARCH, routeFrom.getText().toString());
			GeoIntent.setBoundingBox(intent, routeFrom.bounds());
			intent.putExtra(CycloidConstants.GEO_TYPE, CycloidConstants.GEO_REQUEST_FROM);			
	    	startActivityForResult(intent, CycloidConstants.GEO_REQUEST_FROM);
	    	return;
		}
		
		if (routeTo.geoPlace() == null)
		{
			Intent intent = new Intent(RouteActivity.this, GeoActivity.class);
			intent.putExtra(CycloidConstants.GEO_SEARCH, routeTo.getText().toString());
			GeoIntent.setBoundingBox(intent, routeTo.bounds());
			intent.putExtra(CycloidConstants.GEO_TYPE, CycloidConstants.GEO_REQUEST_TO);			
	    	startActivityForResult(intent, CycloidConstants.GEO_REQUEST_TO);		
		}
		else
		{
			/*
			 * Store the route locations in the adapter history.
			 */
			routeFrom.addHistory(routeFrom.geoPlace());
			routeTo.addHistory(routeTo.geoPlace());
			
			// return start and finish points to RouteMapActivity and close
        	Intent intent = new Intent(RouteActivity.this, RouteMapActivity.class);
        	GeoIntent.setGeoPoint(intent, "FROM", routeFrom.geoPlace().coord());
        	GeoIntent.setGeoPoint(intent, "TO", routeTo.geoPlace().coord());
        	final String routeType = RouteTypeMapper.nameFromId(routeTypeGroup.getCheckedRadioButtonId());
        	intent.putExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE, routeType);
        	setResult(RESULT_OK, intent);
        	finish();
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
				routeFrom.setGeoPlace(myLocation);				
			else
			{
				Toast.makeText(RouteActivity.this, R.string.choose_from, Toast.LENGTH_SHORT).show();
				return;
			}
		}
		
		if (to.equals(""))
		{
			Toast.makeText(RouteActivity.this, R.string.choose_to, Toast.LENGTH_SHORT).show();
			return;
		}

		findRoute();
	} // onClick
	
    private class EntryOptionListener implements Button.OnClickListener 
    {
		@Override
		public void onClick(View button) {
			if (button == optionsFrom) {
				RouteActivity.this.showDialog(DIALOG_CHOOSE_START);
			}
			else if (button == optionsTo) {
				RouteActivity.this.showDialog(DIALOG_CHOOSE_END);
			}
		}    	
    } // EntryOptionListener
} // RouteActivity
