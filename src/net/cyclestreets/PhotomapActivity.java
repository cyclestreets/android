package net.cyclestreets;

import java.util.HashSet;
import java.util.Set;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.views.overlay.MyLocationOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewPathOverlay;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import uk.org.invisibility.cycloid.CycloidConstants;
import uk.org.invisibility.cycloid.CycloidResourceProxy;
import uk.org.invisibility.cycloid.MapActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class PhotomapActivity extends Activity implements CycloidConstants {
	protected PhotomapListener photomapListener;
	
	private ScrollListenerMapView map; 
	private OpenStreetMapViewPathOverlay path;
	private MyLocationOverlay location;
	private OpenStreetMapViewItemizedOverlay<PhotoItem> markers;
	protected Set<PhotoItem> photoSet;
	private ResourceProxy proxy;
	private SharedPreferences prefs;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        proxy = new CycloidResourceProxy(getApplicationContext());
        prefs = getSharedPreferences(PREFS_APP_KEY, MODE_PRIVATE);
        photoSet = new HashSet<PhotoItem>();	// in java 1.6, use ConcurrentSkipListArraySet

        map = new ScrollListenerMapView
        (
    		this,
    		OpenStreetMapRendererInfo.values()[prefs.getInt(PREFS_APP_RENDERER, MAPTYPE.ordinal())],
    		MapActivity.map
        );
        map.setResourceProxy(proxy);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(prefs.getInt(PREFS_APP_ZOOM_LEVEL, 14));
        map.scrollTo(prefs.getInt(PREFS_APP_SCROLL_X, 0), prefs.getInt(PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */
        map.addScrollListener(new PhotomapListener(map, photoSet));

        markers = new OpenStreetMapViewItemizedOverlay<PhotoItem>(this, photoSet,
        		new PhotoMarkerMap(getResources()),
        		new OpenStreetMapViewItemizedOverlay.OnItemTapListener<PhotoItem>() {
        			public boolean onItemTap(int i, PhotoItem photo) {
        				return false;
        			}
					public boolean onItemTap(PhotoItem item) {
						// extract thumbnail URL and display it in a DisplayPhotoActivity
						Log.d(getClass().getSimpleName(), "onItemTap: " + item);
						String url = item.photo.thumbnailUrl;
						Log.d(getClass().getSimpleName(), "URL is " + url);
						startActivity(new Intent(Intent.ACTION_VIEW,
								Uri.parse(url),
								PhotomapActivity.this, DisplayPhotoActivity.class));
						return true;
					}
        });
        map.getOverlays().add(markers);
        
        final RelativeLayout rl = new RelativeLayout(this);
        rl.addView(this.map, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        this.setContentView(rl);        
    }
}
