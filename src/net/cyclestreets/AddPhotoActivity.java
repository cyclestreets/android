package net.cyclestreets;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AddPhotoActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	TextView tv = new TextView(this);
    	tv.setText("This is the add photo tab.");
    	setContentView(tv);	
	}
}
