package net.cyclestreets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cyclestreets.planned.Route;
import net.cyclestreets.planned.Segment;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
    	private final Map<String, Drawable> iconMappings_;
    	private final Drawable footprints_;
    	private final LayoutInflater inflater_;
    	private final List<Segment> segments_;
    	
    	SegmentAdapter(final Context context, final List<Segment> segments)
    	{
    		iconMappings_ = loadIconMappings(context);
    		footprints_ = context.getResources().getDrawable(R.drawable.footprints);
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
			setText(v, R.id.segment_distance, seg.distance(), highlight);
			setText(v, R.id.segment_cumulative_distance, seg.runningDistance(), highlight);
			setText(v, R.id.segment_time, seg.runningTime(), highlight);

			setTurnIcon(v, R.id.segment_type, seg.turn(), seg.walk());
			
			if(highlight && (position != 0) && (position != getCount()-1))
				v.setBackgroundColor(Color.GREEN);
			else
				v.setBackgroundColor(getColour(seg));
			
			return v;
		} // getView
		
		private void setText(final View v, final int id, final String t, final boolean highlight)
		{
			final TextView n = (TextView)v.findViewById(id);
			n.setText(t);
			if(highlight)
				n.setTextColor(Color.BLACK);
		} // setText
		
		private void setTurnIcon(final View v, final int id, final String turn, final boolean walk)		
		{
			final ImageView iv = (ImageView)v.findViewById(id);

			final Drawable icon = iconMappings_.get(turn.toLowerCase()); 
			if(icon != null)
				iv.setImageDrawable(icon);
			if(walk)
				iv.setBackgroundDrawable(footprints_);
		} // setTurnIcon
		
		private int getColour(final Segment s)
		{
			if(s instanceof Segment.Start)
				return Color.rgb(0, 128, 0);
			if(s instanceof Segment.End)
				return Color.rgb(128, 0, 0);
			return Color.BLACK;
		} // getColour
    
		static private Map<String, Drawable> loadIconMappings(final Context context)
		{
			final Resources res = context.getResources();
			
			final Map<String, Drawable> map = new HashMap<String, Drawable>();
			map.put("straight on", res.getDrawable(R.drawable.straight_on));
			map.put("bear left", res.getDrawable(R.drawable.bear_left));
			map.put("turn left", res.getDrawable(R.drawable.turn_left));
			map.put("sharp left", res.getDrawable(R.drawable.sharp_left));
			map.put("bear right", res.getDrawable(R.drawable.bear_right));
			map.put("turn right", res.getDrawable(R.drawable.turn_right));
			map.put("sharp right", res.getDrawable(R.drawable.sharp_right));
			map.put("double-back", res.getDrawable(R.drawable.double_back));
			return map;
		
			// 'straight on', 'sharp left', 'turn left', 'bear left'
			// 'bear right', 'turn right', 'sharp right', 'double-back',  'unknown'
		} // loadIconMappings
	
    } // class SegmentAdaptor

} // ItineraryActivity
