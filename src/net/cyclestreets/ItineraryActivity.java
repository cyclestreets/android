package net.cyclestreets;

import java.util.List;
import net.cyclestreets.planned.Route;
import net.cyclestreets.planned.Segment;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ItineraryActivity extends ListActivity {
	/** Keys used to map data to view id's */
    /** The specific values don't actually matter, as long as they're used consistently */
	protected static String[] fromKeys = new String[] { "type", "street", "time", "dist", "cumdist" };
	protected static int[] toIds = new int[] {
		R.id.segment_type, R.id.segment_street, R.id.segment_time,
		R.id.segment_distance, R.id.segment_cumulative_distance
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        setListAdapter(new SegmentAdapter(this, Route.segments()));
    } // onCreate

    @Override
	protected void onResume() 
    {
		super.onResume();
		
		onContentChanged();
	} // onResume	
    
    static class SegmentAdapter extends BaseAdapter
    {
    	private final LayoutInflater inflater_;
    	private final List<Segment> segments_;
    	
    	SegmentAdapter(final Context context, final List<Segment> segments)
    	{
    		inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		segments_ = segments;
    	} // SegmentAdaptor    	

		@Override
		public int getCount() 
		{ 
			return (segments_ != null) ? segments_.size() : 0; 
		}

		@Override
		public Object getItem(int position) 
		{ 
			return segments_.get(position); 
		}

		@Override
		public long getItemId(int position) 
		{ 
			return position; 
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) 
		{
			final Segment seg = segments_.get(position);
			final View v = inflater_.inflate(R.layout.itinerary_item, parent, false);
			
			setText(v, R.id.segment_street, seg.street());
			setText(v, R.id.segment_distance, seg.distance() + "m");
			setText(v, R.id.segment_cumulative_distance, seg.runningDistance() + "m");
			setText(v, R.id.segment_time, seg.runningTime());
			
			return v;
		} // getView
		
		private void setText(final View v, final int id, final String t)
		{
			final TextView n = (TextView)v.findViewById(id);
			n.setText(t);
		} // setText
    } // class SegmentAdaptor
} // ItineraryActivity
