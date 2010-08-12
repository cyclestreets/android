package net.cyclestreets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// create the rows
        // just test data for now
		List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
		rows.add(createRowMap(R.drawable.icon, "Coldhams Lane Cycle Bridge", "01m24", "213m", "(0.638km)"));
		rows.add(createRowMap(R.drawable.icon, "Cromwell Road", "02m30", "7m", "(0.801km)"));
 
		// set up SimpleAdapter for itinerary_item
		setListAdapter(new SimpleAdapter(this, rows, R.layout.itinerary_item, fromKeys, toIds));
    }
    
	public static Map<String, Object> createRowMap(Object... items) {
		Map<String,Object> row = new HashMap<String,Object>();
		for (int i = 0; i < items.length; i++) {
			row.put(fromKeys[i], items[i]);
		}
		return row;
	}    
}
