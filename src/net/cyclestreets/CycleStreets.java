package net.cyclestreets;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

public class CycleStreets extends TabActivity {
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    // initialize TabSpecs
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab

	    // Plan route
	    spec = tabHost.newTabSpec("Plan route").setIndicator("Plan Route");
	    spec.setContent(new Intent(this, PlanRouteActivity.class));
	    tabHost.addTab(spec);

	    // Itinerary
	    spec = tabHost.newTabSpec("Itinerary").setIndicator("Itinerary");
	    spec.setContent(new Intent(this, ItineraryActivity.class));
	    tabHost.addTab(spec);

	    // Photomap
	    spec = tabHost.newTabSpec("Photomap").setIndicator("Photomap");
	    spec.setContent(new Intent(this, PhotomapActivity.class));
	    tabHost.addTab(spec);

	    // Add photo
	    spec = tabHost.newTabSpec("Add photo").setIndicator("Add photo");
	    spec.setContent(new Intent(this, AddPhotoActivity.class));
	    tabHost.addTab(spec);

	    // More
	    spec = tabHost.newTabSpec("More").setIndicator("More");
	    spec.setContent(new Intent(this, MoreActivity.class));
	    tabHost.addTab(spec);

	    // start with first tab
	    tabHost.setCurrentTab(0);

	    // set up radio button icons
		((RadioButton) findViewById(R.id.radio0)).setButtonDrawable(R.drawable.icon);
		((RadioButton) findViewById(R.id.radio1)).setButtonDrawable(R.drawable.icon);
		((RadioButton) findViewById(R.id.radio2)).setButtonDrawable(R.drawable.icon);
		((RadioButton) findViewById(R.id.radio3)).setButtonDrawable(R.drawable.icon);
		((RadioButton) findViewById(R.id.radio4)).setButtonDrawable(R.drawable.icon);

		// set up radio button listeners
	    // when button pressed, switch to the appropriate tab
		RadioGroup rg = (RadioGroup) findViewById(R.id.radiogroup);
		rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, final int checkedId) {
				if (checkedId == R.id.radio0)
					getTabHost().setCurrentTab(0);
				else if (checkedId == R.id.radio1)
					getTabHost().setCurrentTab(1);
				else if (checkedId == R.id.radio2)
					getTabHost().setCurrentTab(2);
				else if (checkedId == R.id.radio3)
					getTabHost().setCurrentTab(3);
				else if (checkedId == R.id.radio4)
					getTabHost().setCurrentTab(4);
			}
		});	
	}
}
