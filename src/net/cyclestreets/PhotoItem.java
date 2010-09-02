package net.cyclestreets;

import net.cyclestreets.api.Photo;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

public class PhotoItem extends OpenStreetMapViewOverlayItem {
	public PhotoItem(String aTitle, String aDescription, GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
	}
	
	public PhotoItem(Photo photo) {
		super(photo.id + "", photo.caption, new GeoPoint(photo.latitude, photo.longitude));
	}
}
