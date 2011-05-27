package uk.org.invisibility.cycloid;

import net.cyclestreets.R;

import net.cyclestreets.api.GeoPlace;
import org.osmdroid.util.BoundingBoxE6;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/*
 * Geocode activity. Used if user manually enters a location without selecting
 * GeoAdapter results.
 */
public class GeoActivity extends ListActivity
{
	GeoAdapter adapter;
	Dialog dialog;
	Dialog busy;
	Intent result = new Intent();
	
	@Override
	protected void onCreate(Bundle saved)
	{
	    super.onCreate(saved);
		
		busy = ProgressDialog.show
		(
			this,
			"", 
            getString(R.string.geocoding),
            true
        );

		BoundingBoxE6 bounds = GeoIntent.getBoundingBox(getIntent());				
	    adapter = new GeoAdapter(this, bounds);   
	    String search = getIntent().getStringExtra("search");
		adapter.getFilter().filter(search);
	    
        int msg = 0; 
		if (getIntent().getIntExtra(CycloidConstants.GEO_TYPE, 0) == CycloidConstants.GEO_REQUEST_FROM)
			msg = R.string.select_route_from;
		else
			msg = R.string.select_route_to;

		dialog = new AlertDialog.Builder(this)
        .setTitle(msg)        
        .setAdapter
        (
    		adapter,
    		new DialogInterface.OnClickListener()
    		{
    			@Override
    			public void onClick(DialogInterface dialog, int whichButton)
    			{
    				select(adapter.getItem(whichButton));
    			}
    		}
        ).create();	    
	}

	/*
	 * Results are available: display text asking user to select one.
	 */
	public void publish()
	{
		busy.hide();
	    dialog.show();
	}

	/*
	 * No results: return error.
	 */
	public void error()
	{
		setResult(Activity.RESULT_FIRST_USER, result);
		finish();		
	}

	/*
	 * Select result and return
	 */	
	public void select(GeoPlace p)
	{
		GeoIntent.setGeoPlace(result, p);
		setResult(Activity.RESULT_OK, result);
		finish();		
	}
	
	@Override
	protected void onListItemClick(ListView  l, View  v, int position, long id)
	{
		GeoPlace p = adapter.getItem(position);
		select(p);
	}
} // class GeoActivity
	   
