package net.cyclestreets;

//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.List;
import java.util.Map;

import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Marker;

//import net.cyclestreets.api.Journey;
//import net.cyclestreets.api.Marker;

//import org.andnav.osm.util.GeoPoint;

import android.app.ListActivity;
//import android.app.ProgressDialog;
//import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.SimpleAdapter;

public class ItineraryActivity extends ListActivity {
	/** Keys used to map data to view id's */
    /** The specific values don't actually matter, as long as they're used consistently */
	protected static String[] fromKeys = new String[] { "type", "street", "time", "dist", "cumdist" };
	protected static int[] toIds = new int[] {
		R.id.segment_type, R.id.segment_street, R.id.segment_time,
		R.id.segment_distance, R.id.segment_cumulative_distance
	};

	private List<Map<String,Object>> rows_;

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rows_ = new ArrayList<Map<String, Object>>(); 
        setupRows(CycleStreets.journey);
    	// set up SimpleAdapter for itinerary_item
    	setListAdapter(new SimpleAdapter(ItineraryActivity.this, rows_, R.layout.itinerary_item, fromKeys, toIds));
    } // onCreate

    @Override
	protected void onResume() {
		super.onResume();
		
		onContentChanged();
	} // onResume

	// utility method to convert segments into rows
	private static Map<String,Object> createRowMap(Object... items) {
		Map<String,Object> row = new HashMap<String,Object>();
		for (int i = 0; i < items.length; i++) {
			row.put(fromKeys[i], items[i]);
		}
		return row;
	} // createRowMap
	
	private void setupRows(final Journey journey)
	{
		rows_.clear();
		
		if(journey == null)
			return;
		
		// Parse route into itinerary rows
		double cumdist = 0.0;
		for (Marker marker : journey.markers) {
			if (marker.type.equals("segment")) {
				cumdist += marker.distance;
				rows_.add(createRowMap(R.drawable.icon,		// TODO: use icon based on provision type
									   marker.name,
									   marker.time + "m",
									   marker.distance + "m", "(" + (cumdist/1000) + "km)"));
			} // if ...
		} // for ...
	} // setupRows
} // ItineraryActivity
