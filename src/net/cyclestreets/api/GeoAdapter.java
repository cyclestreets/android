package net.cyclestreets.api;

import java.util.Collections;
import java.util.List;

import net.cyclestreets.R;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.GeoPlace;

import org.osmdroid.util.BoundingBoxE6;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GeoAdapter extends ArrayAdapter<GeoPlace> 
{
	static private final int AdapterViewId = R.layout.geo_item_2line;
	
	private final LayoutInflater inflater;

	protected GeoAdapter(final Context context)
	{
		super(context, AdapterViewId);
		inflater = LayoutInflater.from(context);
	} // GeoAdapter
	
	@Override
	public View getView(int position, 
						final View convertView, 
						final ViewGroup parent)
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

	@SuppressWarnings("unchecked")
	protected List<GeoPlace> geoCode(final String search, 
									 final BoundingBoxE6 bounds)
	{
		try {
			return ApiClient.geoCoder(search, bounds).places;				
		}
		catch(Exception e) {
			return (List<GeoPlace>)Collections.EMPTY_LIST;
		} // catch
	} // geoCode

	protected void addAll(final List<GeoPlace> list)
	{
		for(final GeoPlace p : list)
			add(p);
	} // addAll
} // class GeoAdapter
