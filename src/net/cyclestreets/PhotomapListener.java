package net.cyclestreets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cyclestreets.api.Photo;
import android.os.AsyncTask;
import android.util.Log;

import com.nutiteq.components.Place;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;

public class PhotomapListener extends MapAdapter {
	public Map<Integer,Photo> photoMap = new HashMap<Integer,Photo>();
	
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
		new GetPhotosTask().execute(center, zoom, n, s, e, w);
	}
	
	private class GetPhotosTask extends AsyncTask<Object,Void,List<Photo>> {
		protected List<Photo> doInBackground(Object... params) {
			WgsPoint center = (WgsPoint) params[0];
			int zoom = (Integer) params[1];
			double n = (Double) params[2];
			double s = (Double) params[3];
			double e = (Double) params[4];
			double w = (Double) params[5];
			List<Photo> photos;
			try {
				// TODO: do incremental processing of photos
				// TODO: reset photos when zoom level changes
				photos = CycleStreets.apiClient.getPhotos(center, zoom, n, s, e, w);
				Log.d(getClass().getSimpleName(), "got photos: " + photos.size());
				Log.d(getClass().getSimpleName(), photos.get(0).caption);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return photos;
		}
	
		@Override
		protected void onPostExecute(List<Photo> photos) {
			for (Photo photo: photos) {
				CycleStreets.mapComponent.addPlace(new Place(photo.id, photo.caption, Photomap.ICONS[photo.feature], new WgsPoint(photo.longitude, photo.latitude)));
				photoMap.put(photo.id, photo);
			}
		}
	}
}
