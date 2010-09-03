package net.cyclestreets;

import net.cyclestreets.api.Photo;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

public class PhotoItem extends OpenStreetMapViewOverlayItem {
	protected int id;
	
	public PhotoItem(Photo photo) {
		super(photo.id + "", photo.caption, new GeoPoint(photo.latitude, photo.longitude));
		id = photo.id;
	}

	/*
	 * Photos are uniquely identified by their CycleStreets id
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PhotoItem) {
			return id == ((PhotoItem) obj).id;
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	public String toString() {
		return mTitle + ":" + CycleStreetsUtils.truncate(mDescription);
	}
}
