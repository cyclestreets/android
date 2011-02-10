package net.cyclestreets;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.cyclestreets.overlay.LocationOverlay;
import net.cyclestreets.overlay.TapOverlay;
import net.cyclestreets.overlay.ZoomButtonsOverlay;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.views.MapView;
//import org.andnav.osm.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
//import org.andnav.osm.views.overlay.OpenStreetMapViewPathOverlay;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import uk.org.invisibility.cycloid.CycloidConstants;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class PhotomapActivity extends Activity {
	protected PhotomapListener photomapListener;
	
	private MapView map; 
	//private OpenStreetMapViewPathOverlay path;
	//private MyLocationOverlay location;
	private ItemizedOverlay<PhotoItem> markers;
	protected List<PhotoItem> photoList;
	private SharedPreferences prefs;

	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(CycloidConstants.PREFS_APP_KEY, MODE_PRIVATE);
        photoList = new CopyOnWriteArrayList<PhotoItem>();
        
        map = new MapView(this, null);
        map.setTileSource(TileSourceFactory.getTileSource(prefs.getString(CycloidConstants.PREFS_APP_RENDERER, CycloidConstants.DEFAULT_MAPTYPE)));
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.getController().setZoom(prefs.getInt(CycloidConstants.PREFS_APP_ZOOM_LEVEL, 14));
        map.scrollTo(prefs.getInt(CycloidConstants.PREFS_APP_SCROLL_X, 0), prefs.getInt(CycloidConstants.PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */
        map.setMapListener(new DelayedMapListener(new PhotomapListener(this, map, photoList)));

        markers = new ItemizedOverlay<PhotoItem>(
        		this, photoList,
        		getResources().getDrawable(R.drawable.icon),
        		new Point(10,10),
        		new PhotoTapListener(photoList),
        		new DefaultResourceProxyImpl(this));
        map.getOverlays().add(markers);
    
        map.getOverlays().add(new ZoomButtonsOverlay(getApplicationContext(), map));

        map.getOverlays().add(new LocationOverlay(getApplicationContext(), map));
        
        map.getOverlays().add(new TapOverlay(getApplicationContext(), map));

        
        final RelativeLayout rl = new RelativeLayout(this);
        rl.addView(this.map, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        this.setContentView(rl);        
    }

	private class PhotoTapListener implements ItemizedOverlay.OnItemGestureListener<PhotoItem> {
		private List<PhotoItem> photoList;
		
		public PhotoTapListener(List<PhotoItem> photoList) {
			this.photoList = photoList;
		}

		public boolean onItemLongPress(int i, PhotoItem photo) {
			return false;
		}
		
		public boolean onItemSingleTapUp(int i, PhotoItem photo) {
			// extract thumbnail URL and display it in a DisplayPhotoActivity
			PhotoItem item = photoList.get(i);
			Log.d(getClass().getSimpleName(), "onItemTap: " + item);
			String url = item.photo.thumbnailUrl;
			Log.d(getClass().getSimpleName(), "URL is " + url);
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse(url),
					PhotomapActivity.this, DisplayPhotoActivity.class));
			return true;
		}
	}
}
