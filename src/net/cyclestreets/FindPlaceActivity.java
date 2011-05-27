package net.cyclestreets;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.R;

import uk.org.invisibility.cycloid.CycloidConstants;
import uk.org.invisibility.cycloid.GeoActivity;
import uk.org.invisibility.cycloid.GeoAutoCompleteView;
import uk.org.invisibility.cycloid.GeoIntent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class FindPlaceActivity extends Activity 
	implements View.OnClickListener, DialogInterface.OnClickListener 
{
    private static final int DIALOG_NO_FROM_ID = 1;
    
    private GeoAutoCompleteView routeFrom_;
	
    @Override
    public void onCreate(final Bundle saved)
    {
        super.onCreate(saved);

        setContentView(R.layout.findplace);
    	getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        getWindow().setBackgroundDrawableResource(R.drawable.empty);

    	routeFrom_ = (GeoAutoCompleteView)findViewById(R.id.place);
    	routeFrom_.setBounds(GeoIntent.getBoundingBox(getIntent()));
    	
    	final Button findButton = (Button)findViewById(R.id.find_place);
    	findButton.setOnClickListener(this);
    } // onCreate
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent  data)
    {
    	if (requestCode != CycloidConstants.GEO_REQUEST_FROM)
    		return;
    	
    	if (resultCode != Activity.RESULT_OK)
    	{
    		if (requestCode == CycloidConstants.GEO_REQUEST_FROM)
    			showDialog(DIALOG_NO_FROM_ID);
    	}	
    	else if (data != null)
    	{	
    		final GeoPlace place = GeoIntent.getGeoPlace(data);
    		if((place != null) && (requestCode == CycloidConstants.GEO_REQUEST_FROM))
    			findPlace(place);
    	}
    } // onActivityResult
 
    @Override
    protected Dialog onCreateDialog(int id) 
    {
    	if(id == DIALOG_NO_FROM_ID)
    	{
        	final Dialog dialog = new AlertDialog.Builder(this)
        		.setMessage(R.string.lbl_no_place)        
        		.setPositiveButton("OK", this)
        		.create();

            return dialog;
    	} // if ... 

    	return null;
    } // onCreateDialog
    
    protected Dialog createChoosePointDialog(int title)
    {
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(title);
    	builder.setItems(R.array.point_type, this);
    	return builder.create();
    } // createchoosePointDialog
    
    @Override
	public void onClick(DialogInterface dialog, int which) { }


	private void findPlace(final GeoPlace place)
	{
		if (place == null || place.coord() == null)
		{
			Intent intent = new Intent(this, GeoActivity.class);
			intent.putExtra(CycloidConstants.GEO_SEARCH, routeFrom_.getText().toString());
			intent.putExtra(CycloidConstants.GEO_TYPE, CycloidConstants.GEO_REQUEST_FROM);			
	    	startActivityForResult(intent, CycloidConstants.GEO_REQUEST_FROM);		
		}
		else
		{
			routeFrom_.addHistory(place);
			
        	Intent intent = new Intent(this, RouteMapActivity.class);
        	GeoIntent.setGeoPoint(intent, place.coord());
        	setResult(RESULT_OK, intent);
        	finish();
		} // if ...
	} // findRoute

	@Override
	public void onClick(final View view)
	{
		final String from = routeFrom_.getText().toString();	
		if (from.equals(""))
		{
			Toast.makeText(this, R.string.lbl_choose_place, Toast.LENGTH_SHORT).show();
			return;
		}

		findPlace(routeFrom_.geoPlace());
	} // onClick
} // class FindPlaceActivity
