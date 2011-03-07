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
import android.widget.RelativeLayout.LayoutParams;

public class StoredRoutesActivity extends ListActivity 
{
	private static final int MENU_OPEN = 1;
	private static final int MENU_DELETE = 2;
	
	private RouteSummaryAdaptor listAdaptor_;
	
	@Override
    public void onCreate(final Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        setContentView(R.layout.storedroutes);
    	getWindow().setGravity(Gravity.CENTER);       
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        getWindow().setBackgroundDrawableResource(R.drawable.empty);

        listAdaptor_ = new RouteSummaryAdaptor(this, Route.storedRoutes());
        setListAdapter(listAdaptor_);
        registerForContextMenu(getListView());
    } // onCreate
	
	@Override
	public void onCreateContextMenu(final ContextMenu menu, 
									final View v, 
									final ContextMenu.ContextMenuInfo menuInfo) 
	{
		 menu.add(0, MENU_OPEN, Menu.NONE, "Open");
		 menu.add(0, MENU_DELETE, Menu.NONE, "Delete");
	}  // onCreateContextMenu

	@Override
	public boolean onContextItemSelected(final MenuItem item) 
	{
		try {
			final AdapterView.AdapterContextMenuInfo info 
					= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		    int id = (int)getListAdapter().getItemId(info.position);

		    switch(item.getItemId())
		    {
		    case MENU_OPEN:
		    	openRoute(id);
		    	break;
		    case MENU_DELETE:
		    	deleteRoute(id);
		    	break;
		    } // switch
		    
		    return true;
	    } catch (ClassCastException e) {
	    	 return false;
	    }
	} // onContextItemSelected
	 
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		openRoute((int)id);
	} // onListItemClick
	
	private void openRoute(final int id)
	{
		Intent intent = new Intent();
    	intent.putExtra(CycleStreetsConstants.ROUTE_ID, id);
    	setResult(RESULT_OK, intent);
    	finish();
	} // routeSelected
	
	private void deleteRoute(final int id)
	{
		Route.DeleteRoute(id);
		listAdaptor_.refresh(Route.storedRoutes());
	} // deleteRoute
	 
	//////////////////////////////////
	static class RouteSummaryAdaptor extends BaseAdapter
	{
		private final LayoutInflater inflater_;
		private List<RouteSummary> routes_;
    		
		RouteSummaryAdaptor(final Context context, final List<RouteSummary> routes)
		{
			inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			routes_ = routes;
		} // SegmentAdaptor   
		
		public void refresh(final List<RouteSummary> routes)
		{
			routes_ = routes;
			notifyDataSetChanged();
		} // refresh

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
