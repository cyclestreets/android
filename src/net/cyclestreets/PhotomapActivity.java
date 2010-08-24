package net.cyclestreets;

import java.util.List;

import net.cyclestreets.api.Photo;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nutiteq.components.Place;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.utils.Utils;

public class PhotomapActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create photomap
        DispatchingMapView photomapView = new DispatchingMapView(this, CycleStreets.mapComponent);

        // add listener to dynamically add photos as map is moved
        photomapView.addMapListener(new MapAdapter() {
        	public void mapMoved() {
        		WgsBoundingBox bounds = CycleStreets.mapComponent.getBoundingBox();
                WgsPoint center = bounds.getBoundingBoxCenter();
                int zoom = CycleStreets.mapComponent.getZoom();
                WgsPoint sw = bounds.getWgsMin();
                WgsPoint ne = bounds.getWgsMax();
                double n = ne.getLat();
                double s = sw.getLat();
                double e = ne.getLon();
                double w = sw.getLon();
                Log.d(getClass().getSimpleName(), "north: " + n);
                Log.d(getClass().getSimpleName(), "south: " + s);
                Log.d(getClass().getSimpleName(), "east: " + e);
                Log.d(getClass().getSimpleName(), "west: " + w);

                try {
                	List<Photo> photos = CycleStreets.apiClient.getPhotos(center, zoom, n, s, e, w);
                	Log.d(getClass().getSimpleName(), "got photos: " + photos.size());
                	Log.d(getClass().getSimpleName(), photos.get(0).caption);
                	for (Photo photo: photos) {
                		CycleStreets.mapComponent.addPlace(new Place(photo.id, photo.caption, Photomap.ICONS[photo.feature], new WgsPoint(photo.longitude, photo.latitude)));
                	}
                }
                catch (Exception ex) {
                	throw new RuntimeException(ex);
                }
        	}        	
        });        
        
        // create ZoomControls
        ZoomControls zoomControls = new ZoomControls(this);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
        	public void onClick(final View v) {
        		CycleStreets.mapComponent.zoomIn();
        	}
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
        	public void onClick(final View v) {
        		CycleStreets.mapComponent.zoomOut();
        	}
        });

        RelativeLayout photomapLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        photomapLayout.addView(photomapView, mapViewLayoutParams);
    	setContentView(photomapLayout);

        // add Zoom controls to the RelativeLayout
        RelativeLayout.LayoutParams zoomControlsLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        zoomControlsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        zoomControlsLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        photomapLayout.addView(zoomControls, zoomControlsLayoutParams);  
    	
//    	Toast.makeText(this, "fetching photos", Toast.LENGTH_LONG).show();
//    	try {
//        	WgsPoint center = CycleStreets.CAMBRIDGE;
//        	int zoom = 7;
//            double w=-5.8864316;
//            double s=50.1920909;
//            double e=4.3967716;
//            double n=54.2277333;
//        	
//        	List<Photo> photos = CycleStreets.apiClient.getPhotos(center, zoom, n, s, e, w);
//        	Toast.makeText(this, photos.get(0).caption, Toast.LENGTH_LONG).show();
//        }
//        catch (Exception e) {
//        	throw new RuntimeException(e);
//        }
    }
}
