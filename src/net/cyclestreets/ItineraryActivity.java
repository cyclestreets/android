package net.cyclestreets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;

public class ItineraryActivity extends ListActivity {
    /** Keys used for row Map */
    private static final String ICON_KEY = "icon";
    private static final String LABEL_KEY = "label";
    private static final String DETAIL_KEY = "detail";

    private static final int[] ICONS = new int[] { R.drawable.icon,
    	R.drawable.icon};
    private static final String[] LABELS = new String[] { "Apple", "Lime" };
    private static final String[] DETAILS = new String[] {
    	"The apple is the pomaceous fruit of the apple tree, species Malus domestica in the rose family Rosaceae. It is one of the most widely cultivated tree fruits.",
    	"Lime is a term referring to a number of different fruits, both species and hybrids, citruses, which have their origin in the Himalayan region of India and which are typically round, green to yellow in color, 3–6 cm in diameter, and containing sour and acidic pulp."
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// create the rows
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < ICONS.length; i++) {
			rows.add(createIconDetailListItemMap(ICONS[i], LABELS[i],
					DETAILS[i]));
		}
 
		// set up SimpleAdapter for icon_detail_list_item
		String[] fromKeys = new String[] { ICON_KEY, LABEL_KEY, DETAIL_KEY };
		int[] toIds = new int[] { R.id.icon, R.id.firstLine, R.id.secondLine };
		setListAdapter(new SimpleAdapter(this, rows,
				R.layout.itinerary_item, fromKeys, toIds));
    }
    
	public static Map<String, Object> createIconDetailListItemMap(int icon,
			CharSequence label, CharSequence detail) {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put(ICON_KEY, icon);
		row.put(LABEL_KEY, label);
		row.put(DETAIL_KEY, detail);
		return row;
	}    
}
