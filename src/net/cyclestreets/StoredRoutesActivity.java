package net.cyclestreets;

import java.util.List;

import net.cyclestreets.content.RouteSummary;
import net.cyclestreets.planned.Route;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class StoredRoutesActivity extends ListActivity 
{
	@Override
    public void onCreate(final Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        setContentView(R.layout.storedroutes);
    	getWindow().setGravity(Gravity.CENTER);       
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        getWindow().setBackgroundDrawableResource(R.drawable.empty);

        setListAdapter(new RouteSummaryAdaptor(this, Route.storedRoutes()));
        registerForContextMenu(getListView());
    } // onCreate
	
	@Override
	public void onCreateContextMenu(final ContextMenu menu, 
									final View v, 
									final ContextMenu.ContextMenuInfo menuInfo) 
	{
		 menu.add(0, Menu.NONE, Menu.NONE, "Open");
		 menu.add(0, Menu.NONE, Menu.NONE, "Delete");
	}  // onCreateContextMenu

	@Override
	public boolean onContextItemSelected(final MenuItem item) 
	{
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	    } catch (ClassCastException e) {
	    	 return false;
	    }
	    long id = getListAdapter().getItemId(info.position);
	    Toast.makeText(this, "id = " + id, Toast.LENGTH_SHORT).show();
	    return true;
	} // onContextItemSelected

	 
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		routeSelected((int)id);
	} // onListItemClick
	
	private void routeSelected(final int id)
	{
		Intent intent = new Intent();
    	intent.putExtra(CycleStreetsConstants.ROUTE_ID, id);
    	setResult(RESULT_OK, intent);
    	finish();
	} // routeSelected
	 
	//////////////////////////////////
	static class RouteSummaryAdaptor extends BaseAdapter
	{
		private final LayoutInflater inflater_;
		private final List<RouteSummary> routes_;
    		
		RouteSummaryAdaptor(final Context context, final List<RouteSummary> routes)
		{
			inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			routes_ = routes;
		} // SegmentAdaptor    	

		@Override
		public int getCount() { return routes_.size(); }
		
		@Override
		public Object getItem(int position) { return routes_.get(position); }

		@Override
		public long getItemId(int position) { return routes_.get(position).id(); } 
		
		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) 
		{
			final RouteSummary summary = routes_.get(position);
			final View v = inflater_.inflate(R.layout.storedroutes_item, parent, false);

			final TextView n = (TextView)v.findViewById(R.id.route_title);	
			n.setText(summary.title());
			
			return v;
		} // getView
	} // class RouteSummaryAdaptor
} // class StoredRoutesActivity
