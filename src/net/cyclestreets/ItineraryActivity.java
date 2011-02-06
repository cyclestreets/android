package net.cyclestreets;

import java.util.List;

import net.cyclestreets.planned.Route;
import net.cyclestreets.planned.Segment;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ItineraryActivity extends ListActivity 
{
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
		
		setSelection(Route.activeSegmentIndex());
	} // onResume	

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
    	Route.setActiveSegmentIndex(position);
    	((CycleStreets)getParent()).showMap();
    } // onListItemClick
    
    //////////////////////////////////
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
		} // getCount

		@Override
		public Object getItem(int position) 
		{ 
			return segments_.get(position); 
		} // getItem

		@Override
		public long getItemId(int position) 
		{ 
			return position; 
		} // getItemId

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) 
		{
			final Segment seg = segments_.get(position);
			final View v = inflater_.inflate(R.layout.itinerary_item, parent, false);

			final boolean highlight = (position == Route.activeSegmentIndex());
		
			setText(v, R.id.segment_street, seg.street(), highlight);
			setText(v, R.id.segment_distance, formatDistance(seg.distance()), highlight);
			setText(v, R.id.segment_cumulative_distance, formatRunningDistance(seg.runningDistance()), highlight);
			setText(v, R.id.segment_time, seg.runningTime(), highlight);

			if(highlight && (position != 0) && (position != getCount()-1))
				v.setBackgroundColor(Color.GREEN);
			else
				v.setBackgroundColor(getColour(seg));
			
			return v;
		} // getView
		
		private String formatDistance(int distance)
		{
			if(distance < 2000)
				return String.format("%dm", distance);
			return formatRunningDistance(distance);
		} // formatDistance
		
		private String formatRunningDistance(int distance)
		{
			int km = distance / 1000;
			int frackm = (int)((distance % 1000) / 10.0);
			return String.format("%d.%02dkm", km, frackm);
		} // formatRunningDistance
		
		private void setText(final View v, final int id, final String t, final boolean highlight)
		{
			final TextView n = (TextView)v.findViewById(id);
			n.setText(t);
			if(highlight)
				n.setTextColor(Color.BLACK);
		} // setText
		
		private int getColour(final Segment s)
		{
			if(s instanceof Segment.Start)
				return Color.rgb(0, 128, 0);
			if(s instanceof Segment.End)
				return Color.rgb(128, 0, 0);
			return Color.BLACK;
		} // getColour
    } // class SegmentAdaptor
} // ItineraryActivity
