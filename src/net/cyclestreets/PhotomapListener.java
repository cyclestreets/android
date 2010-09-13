package net.cyclestreets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.cyclestreets.api.Photo;
import net.cyclestreets.api.PhotomapCategories;

import org.andnav.osm.events.MapAdapter;
import org.andnav.osm.events.ScrollEvent;
import org.andnav.osm.events.ZoomEvent;
import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.views.OpenStreetMapView;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PhotomapListener extends MapAdapter {
	public Map<Integer,Photo> photoMap = new HashMap<Integer,Photo>();
	
	protected OpenStreetMapView map;
	protected List<PhotoItem> photoList;
	protected Set<PhotoItem> photoSet;
	protected PhotoMarkers photoMarkers;

	public PhotomapListener(Context ctx, OpenStreetMapView map, List<PhotoItem> photoList) {
		this.map = map;
		this.photoList = photoList;
		this.photoSet = new HashSet<PhotoItem>();
		this.photoMarkers = new PhotoMarkers(ctx.getResources());
	}
	
	@Override
	public boolean onScroll(ScrollEvent event) {
		int x = event.getX();
		int y = event.getY();
		Log.i(getClass().getSimpleName(), "scroll to: " + x + "," + y);
		
		refreshPhotos();
		return true;
	}
	
	@Override
	public boolean onZoom(ZoomEvent event) {
		int z = event.getZoomLevel();
		Log.i(getClass().getSimpleName(), "zoom to: " + z);

		// clear photos for new zoom level
		photoSet.clear();
		photoList.clear();
		refreshPhotos();
		return true;
	}

	protected void refreshPhotos() {
		BoundingBoxE6 bounds = map.getVisibleBoundingBoxE6();
		double n = bounds.getLatNorthE6() / 1E6;
		double s = bounds.getLatSouthE6() / 1E6;
		double e = bounds.getLonEastE6() / 1E6;
		double w = bounds.getLonWestE6() / 1E6;
		Log.i(getClass().getSimpleName(), "Bounding box: " + n + " " + s + " " + e + " " + w);
		
		int zoom = map.getZoomLevel();
		double clat = map.getMapCenterLatitudeE6() / 1E6;
		double clon = map.getMapCenterLongitudeE6() / 1E6;
		new GetPhotosTask().execute(clat, clon, zoom, n, s, e, w);		
	}

	private class GetPhotosTask extends AsyncTask<Object,Void,List<Photo>> {
		protected List<Photo> doInBackground(Object... params) {
			double clat = (Double) params[0];
			double clon = (Double) params[1];
			int zoom = (Integer) params[2];
			double n = (Double) params[3];
			double s = (Double) params[4];
			double e = (Double) params[5];
			double w = (Double) params[6];
			List<Photo> photos;
			try {
				PhotomapCategories photomapCategories = CycleStreets.apiClient.getPhotomapCategories();
				Log.d(getClass().getSimpleName(), "photomapcategories: " + photomapCategories);
				
				photos = CycleStreets.apiClient.getPhotos(clat, clon, zoom, n, s, e, w);
				Log.d(getClass().getSimpleName(), "got photos: " + photos.size());
				if (!photos.isEmpty()) {
					Log.d(getClass().getSimpleName(), photos.get(0).caption);
				}
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return photos;
		}
		
		@Override
		protected void onPostExecute(List<Photo> photos) {
			Log.d(getClass().getSimpleName(), "photolist contains: [" + photoList.size() + "] " + photoList);
			Log.d(getClass().getSimpleName(), "photos contains: [" + photos.size() + "] " + photos);
			for (Photo photo: photos) {
				// check for duplicates
				// photoSet is only used internally for duplicate checking
				// photoList is exported to the ItemizedOverlay
				//
				// This is needed since ItemizedOverlay takes a List but there is no way to 
				// enforce uniqueness on a list.
				PhotoItem item = new PhotoItem(photo, photoMarkers);
				if (!photoSet.contains(item)) {
					photoSet.add(item);
					photoList.add(item);
				}
			}
			Log.d(getClass().getSimpleName(), "photolist contains: [" + photoList.size() + "] " + photoList);

			// force map redraw
			Log.d(getClass().getSimpleName(), "invalidating map");
			map.postInvalidate();
		}
	}
}
