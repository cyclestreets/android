package net.cyclestreets;

import android.app.TabActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


// superclass for all top-level Activities - provides the options menu
public class CycleStreetsActivity extends TabActivity {
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.toplevel_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.savedroutes:
	        //
	        return true;
	    case R.id.settings:
	        //
	        return true;
	    case R.id.credits:
	    	//
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
