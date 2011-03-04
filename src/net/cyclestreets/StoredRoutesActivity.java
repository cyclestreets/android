package net.cyclestreets;

import java.util.List;

import net.cyclestreets.content.RouteSummary;
import net.cyclestreets.planned.Route;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
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
    } // onCreate

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
		public int getCount() 
		{ 
			return routes_.size(); 
		} // getCount

		@Override
		public Object getItem(int position) 
		{ 
			return routes_.get(position); 
		} // getItem

		@Override
		public long getItemId(int position) 
		{ 
			return routes_.get(position).id(); 
		} // getItemId

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) 
		{
			final RouteSummary summary = routes_.get(position);
			final View v = inflater_.inflate(R.layout.storedroutes_item, parent, false);

			final TextView n = (TextView)v.findViewById(R.id.route_title);
			n.setText(summary.title());
			
			return v;
		} // getView
    } // class SegmentAdaptor


} // class StoredRoutesActivity
