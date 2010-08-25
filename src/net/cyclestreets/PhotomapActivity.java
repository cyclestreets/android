package net.cyclestreets;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nutiteq.components.OnMapElement;
import com.nutiteq.components.Place;
import com.nutiteq.listeners.MapListener;
import com.nutiteq.listeners.OnMapElementListener;

public class PhotomapActivity extends Activity implements OnMapElementListener {
	protected PhotomapListener photomapListener;
	protected DispatchingMapView photomapView;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create photomap
        photomapView = new DispatchingMapView(this, CycleStreets.mapComponent);
    	photomapListener = new PhotomapListener();
        Log.d(getClass().getSimpleName(), "before: " + CycleStreets.mapComponent.getOnMapElementListener());
    	// create listener for clicks on photos
        CycleStreets.mapComponent.setOnMapElementListener(this);
        Log.d(getClass().getSimpleName(), "after: " + CycleStreets.mapComponent.getOnMapElementListener());

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

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		photomapView.removeMapListener(photomapListener);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// add listener to dynamically add photos as map is moved
	    photomapView.addMapListener(photomapListener);
	    
	    // force initial refresh
	    photomapListener.mapMoved();
	}

    @Override
    protected void onDestroy() {
  	  super.onDestroy();
  	  if (photomapView != null) {
  	      photomapView.clean();
  	      photomapView = null;
  	    }
  	}

	@Override
	public void elementClicked(OnMapElement arg0) {
		Log.d(getClass().getSimpleName(), "elementClicked " + ((Place) arg0).getLabel());
		String url = photomapListener.photoMap.get(((Place) arg0).getId()).thumbnailUrl;
		Log.d(getClass().getSimpleName(), "URL is " + url);
		if (arg0 instanceof Place) {
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse(url),
					this, DisplayPhotoActivity.class));
		}
	}

	@Override
	public void elementEntered(OnMapElement arg0) {
		// TODO: show tool tip
	}

	@Override
	public void elementLeft(OnMapElement arg0) {
		// TODO: hide tool tip
	}
}
