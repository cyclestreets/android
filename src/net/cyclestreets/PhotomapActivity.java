package net.cyclestreets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import net.cyclestreets.api.Photo;
import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nutiteq.android.MapView;
import com.nutiteq.components.WgsPoint;

public class PhotomapActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add another mapview to photomap layout
        MapView photomapView = new MapView(this, CycleStreets.mapComponent);
        RelativeLayout photomapLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        photomapLayout.addView(photomapView, mapViewLayoutParams);
    	setContentView(photomapLayout);

    	Toast.makeText(this, "fetching photos", Toast.LENGTH_LONG).show();
    	try {
        	WgsPoint center = CycleStreets.CAMBRIDGE;
        	int zoom = 7;
            double w=-5.8864316;
            double s=50.1920909;
            double e=4.3967716;
            double n=54.2277333;
        	
        	List<Photo> photos = CycleStreets.apiClient.getPhotos(center, zoom, n, s, e, w);
        	Toast.makeText(this, photos.get(0).caption, Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }
}
