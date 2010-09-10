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

	@Override
	public int hashCode() {
		return ((photo == null) ? 0 : photo.id);
	}
	
	/*
	 * PhotoItems are equal if underlying Photos have the same id
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhotoItem other = (PhotoItem) obj;
		if (photo == null) {
			if (other.photo != null)
				return false;
		} else {
			if (other.photo == null)
				return false;
			else {
				if (photo.id != other.photo.id)
					return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "PhotoItem [photo=" + photo + "]";
	}	
}
