package net.cyclestreets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Segment;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import com.nutiteq.components.WgsPoint;

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

        try {
        	WgsPoint start = new WgsPoint(0.117950, 52.205302);
        	WgsPoint finish = new WgsPoint(0.147324, 52.199650);
        	Journey journey = CycleStreets.apiClient.getJourney("quietest", start, finish);        

        	// create the rows
        	List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
        	double cumdist = 0.0;
        	for (Segment segment : journey.segments) {
        		String type = segment.provisionName;
        		cumdist += segment.distance;
        		rows.add(createRowMap(R.drawable.icon, segment.name, segment.time + "m", segment.distance + "m", "(" + (cumdist/1000) + "km)"));
        	}

        	// set up SimpleAdapter for itinerary_item
        	setListAdapter(new SimpleAdapter(this, rows, R.layout.itinerary_item, fromKeys, toIds));
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }
    
	public static Map<String, Object> createRowMap(Object... items) {
		Map<String,Object> row = new HashMap<String,Object>();
		for (int i = 0; i < items.length; i++) {
			row.put(fromKeys[i], items[i]);
		}
		return row;
	}    
}
