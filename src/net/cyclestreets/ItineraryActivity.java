package net.cyclestreets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Segment;

import org.andnav.osm.util.GeoPoint;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	// set up SimpleAdapter for itinerary_item
    	setListAdapter(new SimpleAdapter(ItineraryActivity.this, CycleStreets.itineraryRows, R.layout.itinerary_item, fromKeys, toIds));
    }

    @Override
	protected void onResume() {
		super.onResume();
		
		// TODO: only redisplay if journey changed
		onContentChanged();
		
//    	GeoPoint start = new GeoPoint(52.205302, 0.117950);
//    	GeoPoint finish = new GeoPoint(52.199650, 0.147324);
//        new GetJourneyTask().execute(start, finish);
	}

	// utility method to convert segments into rows
	public static Map<String,Object> createRowMap(Object... items) {
		Map<String,Object> row = new HashMap<String,Object>();
		for (int i = 0; i < items.length; i++) {
			row.put(fromKeys[i], items[i]);
		}
		return row;
	}

	private class GetJourneyTask extends AsyncTask<GeoPoint,Void,List<Map<String,Object>>> {
		protected ProgressDialog dialog = new ProgressDialog(ItineraryActivity.this);

		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog.setMessage("Calculating journey");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}

		protected List<Map<String,Object>> doInBackground(GeoPoint... params) {
			GeoPoint start = params[0];
			GeoPoint finish = params[1];
        	List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
	        try {
	        	Journey journey = CycleStreets.apiClient.getJourney("quietest", start, finish);

	        	// create the rows
	        	double cumdist = 0.0;
	        	for (Segment segment : journey.segments) {
	        		String type = segment.provisionName;
	        		cumdist += segment.distance;
	        		rows.add(createRowMap(R.drawable.icon, segment.name, segment.time + "m", segment.distance + "m", "(" + (cumdist/1000) + "km)"));
	        	}
	        }
	        catch (Exception e) {
	        	throw new RuntimeException(e);
	        }
			return rows;
		}

		@Override
		protected void onPostExecute(List<Map<String,Object>> rows) {
        	// set up SimpleAdapter for itinerary_item
        	setListAdapter(new SimpleAdapter(ItineraryActivity.this, rows, R.layout.itinerary_item, fromKeys, toIds));
        	dialog.dismiss();
		}    	
	}
}
