package uk.org.invisibility.cycloid;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.TreeSet;

import net.cyclestreets.R;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.GeoPlace;

import org.osmdroid.util.BoundingBoxE6;

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

public class GeoAdapter extends ArrayAdapter<GeoPlace> 
						implements OnItemClickListener
{
	static private final int AdapterViewId = R.layout.geo_item_2line;
	
	private final GeocodeFilter filter;
	private final BoundingBoxE6 bounds_;
	private final LayoutInflater inflater;
	private final AutoCompleteTextView textView;
	private final GeoActivity activity;
	private GeoPlace selectedPlace;
	private CharSequence selectedText;
	private SharedPreferences prefs;

	/*
	 * Constructor when used with an AutoCompleteTextView
	 */
	public GeoAdapter(final AutoCompleteTextView view, 
					  final BoundingBoxE6 bounds)
	{
		this(null, view, view.getContext(), bounds);
	} // GeoAdapter

	/*
	 * Constructor when used with an ListActivity
	 */
	public GeoAdapter(final GeoActivity context, 
					  final BoundingBoxE6 bounds)
	{
		this(context, null, context, bounds);
	} // GeoAdapter
	
	private GeoAdapter(final GeoActivity geoActivity,
					   final AutoCompleteTextView view, 
					   final Context context,
					   final BoundingBoxE6 bounds)
	{
		super(context, AdapterViewId);
		bounds_ = bounds;
		inflater = LayoutInflater.from(context);
		
		activity = geoActivity;
		textView = view;
		
		if (textView != null)
		{
			textView.setOnItemClickListener(this);
			prefs = context.getSharedPreferences(CycloidConstants.PREFS_GEO_KEY, Application.MODE_PRIVATE);
		} // if ...	
		else
		{
			prefs = null;
		} // if ..
		
		filter = new GeocodeFilter(prefs);
	} // GeoAdapter
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final View row = inflater.inflate(AdapterViewId, parent, false);
		final GeoPlace p = getItem(position);
		
		setText(row, android.R.id.text1, p.name);
		setText(row, android.R.id.text2, p.near);

		return row;
	} // getView
	
	private void setText(final View parent, final int id, final String text)
	{
		((TextView)parent.findViewById(id)).setText(text);
	} // setText
	
	@Override
	public Filter getFilter()
	{
		return filter;
	} // getFilter
	
	/*
	 * Return the last GeoPlace selected from the drop down list, assuming
	 * the current textView text hasn't changed
	 */
	public GeoPlace getSelected()
	{
		if (textView != null && textView.getText().equals(selectedText))
			return selectedPlace;
		else
			return null;
	} // getSelected

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
	} // GeoPlace
	
	/*
	 * Add to geocoding history
	 */
	public void addHistory(final GeoPlace p)
	{
		if (prefs == null)
			return;
		
		if (p.name.equals(CycloidConstants.MY_LOCATION))
			return;
		
		String key = p.name.toLowerCase();
				
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(CycloidConstants.PREFS_GEO_NAME_PREFIX + key, p.name);
        edit.putString(CycloidConstants.PREFS_GEO_NEAR_PREFIX + key, p.near);
        edit.putInt(CycloidConstants.PREFS_GEO_LATITUDE_PREFIX + key, p.coord().getLatitudeE6());
        edit.putInt(CycloidConstants.PREFS_GEO_LONGITUDE_PREFIX + key, p.coord().getLongitudeE6());
        edit.commit();
	} // addHistory
	
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	private class GeocodeFilter extends Filter
	{
		private SharedPreferences prefs_;
		
		public GeocodeFilter(final SharedPreferences prefs)
		{
			prefs_ = prefs;
		} // GeocodeFilter
		
		@Override
		protected FilterResults performFiltering(CharSequence cs)
		{
			final List<GeoPlace> list = new ArrayList<GeoPlace>();
			
			if (cs != null)
			{
				// Add history hits first
				filterPrefs(list, cs);

				// Only geocode if more than two characters
				if (cs.length() > 2)
				{
					List<GeoPlace> r = geoCode(cs.toString());
					if (r != null)
						list.addAll(r);
				}
			}
			else
			{
				// Add all prefs
				filterPrefs(list, "");
			}

			final FilterResults results = new FilterResults();
			results.values = list;
			results.count = list.size();
			return results;
		} // performFiltering
		
		@SuppressWarnings("unchecked")
		private List<GeoPlace> geoCode(final String search)
		{
			try {
				return ApiClient.geoCoder(search, bounds_).places;				
			}
			catch(Exception e) {
				return (List<GeoPlace>)Collections.EMPTY_LIST;
			} // catch
		} // geoCode

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence cs, FilterResults fr)
		{
			clear();

			final List<GeoPlace> list = (List<GeoPlace>)fr.values;
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
            			addAll(list);
            	}
	            else
	            	addAll(list);
            } // if ...
            
            GeoAdapter.this.notifyDataSetChanged();
		} // publishResults

		private void addAll(final List<GeoPlace> list)
		{
			for(final GeoPlace p : list)
				add(p);
		} // addAll
		
		/*
		 * Add any matching entries from prefs
		 */
		private void filterPrefs(final List<GeoPlace> list, 
								 final CharSequence cs)
		{
			if (prefs_ == null)
				return;
			
			final String match = (CycloidConstants.PREFS_GEO_NAME_PREFIX + cs).toLowerCase();
			final Set<String> sortedKeys = new TreeSet<String>(prefs.getAll().keySet());
		
			for (final String s: sortedKeys)
			{		
				if (!s.startsWith(match))
					continue;
				
				final String key = prefs.getString(s, "").toLowerCase();
					
				list.add(new GeoPlace(prefs.getInt(CycloidConstants.PREFS_GEO_LATITUDE_PREFIX + key, 0),
									  prefs.getInt(CycloidConstants.PREFS_GEO_LONGITUDE_PREFIX + key, 0),
									  prefs.getString(CycloidConstants.PREFS_GEO_NAME_PREFIX + key, ""),
									  prefs.getString(CycloidConstants.PREFS_GEO_NEAR_PREFIX + key, "")));
			} // for ...
		} // filterPrefs

	} // class GeocodeFilter
} // class GeoAdapter
