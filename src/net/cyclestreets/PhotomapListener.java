package net.cyclestreets;

import java.util.List;

import net.cyclestreets.api.Photo;
import android.util.Log;

import com.nutiteq.components.Place;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;

public class PhotomapListener extends MapAdapter {
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
			// remove previous places, to prevent duplicates
			// TODO: do incremental processing of photos
			CycleStreets.mapComponent.removeAllPlaces();
			
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
}
