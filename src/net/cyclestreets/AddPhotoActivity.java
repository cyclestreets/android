package net.cyclestreets;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AddPhotoActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Add photo tab");
        setContentView(textview);
    }
}
