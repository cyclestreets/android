package net.cyclestreets;

import net.cyclestreets.R;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;

import net.cyclestreets.util.GeoIntent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

 public class CycleMapActivity extends Activity 
 {
	private CycleMapView map_; 
	
    @Override
    public void onCreate(final Bundle saved)
    {
        super.onCreate(saved);

		map_ = new CycleMapView(this, this.getClass().getName());

        final RelativeLayout rl = new RelativeLayout(this);
        rl.addView(map_, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        setContentView(rl);
    } // onCreate
    
    protected CycleMapView mapView() { return map_; }
    protected Overlay overlayPushBottom(final Overlay overlay) { return map_.overlayPushBottom(overlay); }
    protected Overlay overlayPushTop(final Overlay overlay) { return map_.overlayPushTop(overlay); }
    
    protected void findPlace() { launchFindDialog(); }

    @Override
    protected void onPause()
    {
    	map_.onPause();
        super.onPause();
    } // onPause

    @Override
    protected void onResume()
    {
    	super.onResume();
    	map_.onResume();
    } // onResume

    @Override
	public boolean onCreateOptionsMenu(final Menu menu)
    {
    	map_.onCreateOptionsMenu(menu);
    	menu.add(0, R.string.ic_menu_findplace, Menu.NONE, R.string.ic_menu_findplace).setIcon(R.drawable.ic_menu_search);
    	menu.add(0, R.string.ic_menu_settings, Menu.NONE, R.string.ic_menu_settings).setIcon(R.drawable.ic_menu_settings);
    	return true;
	} // onCreateOptionsMenu
    
    @Override
	public boolean onPrepareOptionsMenu(final Menu menu)
    {
    	map_.onPrepareOptionsMenu(menu);
    	return true;
    } // onPrepareOptionsMenu
    
    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    } // onCreateContextMenu
   
	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
		if(map_.onMenuItemSelected(featureId, item))
			return true;
		
		if(item.getItemId() == R.string.ic_menu_findplace)
		{
			launchFindDialog();
			return true;
		} // if ...

		if(item.getItemId() == R.string.ic_menu_settings)
		{
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		} // if ...

		return false;
	} // onMenuItemSelected
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode != RESULT_OK)
			return;
		
		if(requestCode != R.string.ic_menu_findplace)
			return;
		
		final GeoPoint place = GeoIntent.getGeoPoint(data);
		// we're in the wrong thread, so pop this away for later and force a refresh
		map_.centreOn(place);
	} // onActivityResult
    
	private void launchFindDialog()
	{
		final Intent intent = new Intent(this, FindPlaceActivity.class);
    	GeoIntent.setBoundingBox(intent, map_.getBoundingBox());
    	startActivityForResult(intent, R.string.ic_menu_findplace);
	} // launchFindDialog
	
   @Override
   public boolean onTrackballEvent(MotionEvent event)
   {
       return map_.onTrackballEvent(event);
   } // onTrackballEvent
  
   @Override
   public boolean onTouchEvent(MotionEvent event)
   {
       if (event.getAction() == MotionEvent.ACTION_MOVE)
           map_.disableFollowLocation();
       return super.onTouchEvent(event);
   } // onTouchEvent   
} // class MapActivity