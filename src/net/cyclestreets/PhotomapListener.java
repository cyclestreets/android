package net.cyclestreets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.cyclestreets.api.Photo;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.views.OpenStreetMapView;

import android.os.AsyncTask;
import android.util.Log;

public class PhotomapListener extends MapAdapter implements ScrollListener {
	public Map<Integer,Photo> photoMap = new HashMap<Integer,Photo>();

	protected OpenStreetMapView map;
	protected Set<PhotoItem> photoSet;

	public PhotomapListener(OpenStreetMapView map, Set<PhotoItem> photoSet) {
		this.map = map;
		this.photoSet = photoSet;
	}
	
//	public void mapMoved() {
//		WgsBoundingBox bounds = CycleStreets.mapComponent.getBoundingBox();
//		WgsPoint center = bounds.getBoundingBoxCenter();
//		int zoom = CycleStreets.mapComponent.getZoom();
//		WgsPoint sw = bounds.getWgsMin();
//		WgsPoint ne = bounds.getWgsMax();
//		double n = ne.getLat();
//		double s = sw.getLat();
//		double e = ne.getLon();
//		double w = sw.getLon();
//		new GetPhotosTask().execute(center, zoom, n, s, e, w);
//	}
//	
	@Override
	public void scrollTo(int x, int y) {
		Log.i(getClass().getSimpleName(), "Scrolled to: " + x + "," + y);
		
		BoundingBoxE6 bounds = map.getVisibleBoundingBoxE6();
		double n = bounds.getLatNorthE6() / 1000000.0;
		double s = bounds.getLatSouthE6() / 1000000.0;
		double e = bounds.getLonEastE6() / 1000000.0;
		double w = bounds.getLonWestE6() / 1000000.0;
		Log.i(getClass().getSimpleName(), "Bounding box: " + n + " " + s + " " + e + " " + w);
		
		int zoom = map.getZoomLevel();
		double clat = map.getMapCenterLatitudeE6() / 1000000.0;
		double clon = map.getMapCenterLongitudeE6() / 1000000.0;
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
				// TODO: do incremental processing of photos
				// TODO: reset photos when zoom level changes
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
			Log.d(getClass().getSimpleName(), "photoset contains: [" + photoSet.size() + "] " + photoSet);
			Log.d(getClass().getSimpleName(), "photos contains: [" + photos.size() + "] " + photos);
			for (Photo photo: photos) {
				photoSet.add(new PhotoItem(photo));
				
//				CycleStreets.mapComponent.addPlace(new Place(photo.id,
//						CycleStreetsUtils.truncate(photo.caption),
//						Photomap.ICONS[photo.feature],
//						new WgsPoint(photo.longitude, photo.latitude)));
//				photoMap.put(photo.id, photo);
			}
			Log.d(getClass().getSimpleName(), "photoset contains: [" + photoSet.size() + "] " + photoSet);

			// force map redraw
			Log.d(getClass().getSimpleName(), "invalidating map");
			map.postInvalidate();
		}
	}
}
