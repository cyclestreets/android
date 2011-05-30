package uk.org.invisibility.cycloid;

import net.cyclestreets.R;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoStaticAdapter;
import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.util.Dialog;

import org.osmdroid.util.BoundingBoxE6;

import android.app.Activity;
import android.app.AlertDialog;
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
						 implements GeoStaticAdapter.OnPopulatedListener
{
	GeoStaticAdapter adapter;
	AlertDialog dialog;
	ProgressDialog busy;
	Intent result = new Intent();
	
	@Override
	protected void onCreate(Bundle saved)
	{
	    super.onCreate(saved);
		
		busy = Dialog.createProgressDialog(this, R.string.geocoding);
		busy.show();

		BoundingBoxE6 bounds = GeoIntent.getBoundingBox(getIntent());				
	    String search = getIntent().getStringExtra("search");
	    adapter = new GeoStaticAdapter(this, search, bounds, this);   
	    
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
	} // GeoActivity

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

	@Override
	public void onPopulated() 
	{
		busy.hide();
	    dialog.show();
	} // onPopulated
} // class GeoActivity
	   
