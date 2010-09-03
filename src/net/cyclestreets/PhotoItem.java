package net.cyclestreets;

import net.cyclestreets.api.Photo;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

public class PhotoItem extends OpenStreetMapViewOverlayItem {
	protected Photo photo;
	
	public PhotoItem(Photo photo) {
		super(photo.id + "", photo.caption, new GeoPoint(photo.latitude, photo.longitude));
		this.photo = photo;
	}

	/*
	 * Photos are uniquely identified by their CycleStreets id
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PhotoItem) {
			return photo.id == ((PhotoItem) obj).photo.id;
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return photo.id;
	}
	
	public String toString() {
		return photo.toString();
	}
}
