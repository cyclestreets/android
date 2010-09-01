package uk.org.invisibility.cycloid;

import java.util.ArrayList;
import java.util.TreeSet;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.GeoPoint;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class GeoAdapter extends ArrayAdapter<GeoPlace> implements OnItemClickListener, CycloidConstants
{
	private final GeocodeFilter filter;
	private final GeoQuery query;
	private final LayoutInflater inflater;
	private final int resId;
	private final AutoCompleteTextView textView;
	private final GeoActivity activity;
	private GeoPlace selectedPlace;
	private CharSequence selectedText;
	private SharedPreferences prefs;

	/*
	 * Constructor when used with an AutoCompleteTextView
	 */
	public GeoAdapter(Context context, int rowResourceId, AutoCompleteTextView view, BoundingBoxE6 bounds)
	{
		super(context, rowResourceId);
		filter = new GeocodeFilter();
		query = new GeoQuery(bounds);
		resId = rowResourceId;
		textView = view;
		inflater = LayoutInflater.from(context);
		activity = null;
		
		if (view != null)
			view.setOnItemClickListener(this);
		
		prefs = context.getSharedPreferences(PREFS_GEO_KEY, Application.MODE_PRIVATE);
	}

	/*
	 * Constructor when used with an ListActivity
	 */
	public GeoAdapter(GeoActivity context, int rowResourceId, BoundingBoxE6 bounds)
	{
		super(context, rowResourceId);
		activity = context;
		filter = new GeocodeFilter();
		query = new GeoQuery(bounds);
		resId = rowResourceId;
		inflater = LayoutInflater.from(context);
		textView = null;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = inflater.inflate(resId, parent, false);
		TextView text1 = (TextView) row.findViewById(android.R.id.text1);
		TextView text2 = (TextView) row.findViewById(android.R.id.text2);
		GeoPlace p = getItem(position);
		text1.setText(p.name);
		text2.setText(p.near);
		return row;
	}
	
	@Override
	public Filter getFilter()
	{
		return filter;
	}
	
	private class GeocodeFilter extends Filter
	{
		@Override
		protected FilterResults performFiltering(CharSequence cs)
		{
			FilterResults results = new FilterResults();
			final ArrayList<GeoPlace> list = new ArrayList<GeoPlace>();
			results.values = list;
			
			if (cs != null)
			{
				// Add history hits first
				GeoAdapter.this.filterPrefs(list, cs);

				// Only geocode if more than two characters
				if (cs.length() > 2)
				{
					GeoResults r = query.query(cs.toString());
					if (r.isValid())
					{
						for (GeoPlace p : r.getPlaces())
							list.add(p);
					}
				}
			}
			else
			{
				// Add all prefs
				GeoAdapter.this.filterPrefs(list, "");
			}
			results.count = list.size();
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence cs, FilterResults fr)
		{
			ArrayList<GeoPlace> list = (ArrayList<GeoPlace>)fr.values;
			clear();
            if (list != null)
            {
            	if (activity != null)
            	{
            		activity.publish();
            		if (list.size() == 0)
            			activity.error();
            		else if (list.size() == 1)
		            	activity.select(list.get(0));
            		else
            		{
		            	for (GeoPlace p : list)
		                    add(p);
		            }
            	}
	            else
	            {
	            	for (GeoPlace p : list)
	                    add(p);
	            }
            }
            GeoAdapter.this.notifyDataSetChanged();
		}		
	}

	/*
	 * Return the last GeoPlace selected from the drop down list, assuming
	 * the current textView text hasn't changed
	 * TODO: can this use text changed listener?
	 */
	public GeoPlace getSelected()
	{
		if (textView != null && textView.getText().equals(selectedText))
			return selectedPlace;
		else
			return null;
	}

	/*
	 * Called when a GeoPlace is selected from the drop down. Store the
	 * selected item and the text used at the time it was selected.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id)
	{
		if (textView != null)
		{
			selectedPlace = getItem(position);		
			selectedText = textView.getText();
		}
	}
	
	/*
	 * Add any matching entries from prefs
	 */
	void filterPrefs(ArrayList<GeoPlace> list, CharSequence cs)
	{
		if (prefs == null)
			return;
		
		String match = (PREFS_GEO_NAME_PREFIX + cs).toLowerCase();
		TreeSet<String> sortedKeys = new TreeSet<String>(prefs.getAll().keySet());
	
		for (String s: sortedKeys)
		{		
			if (s.startsWith(match))
			{
				String key = prefs.getString(s, "").toLowerCase();
				
				list.add
				(
					new GeoPlace
					(
						new GeoPoint
						(
							prefs.getInt(PREFS_GEO_LATITUDE_PREFIX + key, 0),
							prefs.getInt(PREFS_GEO_LONGITUDE_PREFIX + key, 0)
						),
						prefs.getString(PREFS_GEO_NAME_PREFIX + key, ""),
						prefs.getString(PREFS_GEO_NEAR_PREFIX + key, "")
					)
				);
			}
		}
	}
	
	/*
	 * Add to geocoding history
	 */
	public void addHistory(GeoPlace p)
	{
		if (prefs == null)
			return;
		
		if (p.name.equals(MY_LOCATION))
			return;
		
		String key = p.name.toLowerCase();
				
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PREFS_GEO_NAME_PREFIX + key, p.name);
        edit.putString(PREFS_GEO_NEAR_PREFIX + key, p.near);
        edit.putInt(PREFS_GEO_LATITUDE_PREFIX + key, p.coord.getLatitudeE6());
        edit.putInt(PREFS_GEO_LONGITUDE_PREFIX + key, p.coord.getLongitudeE6());
        edit.commit();
	}
}
