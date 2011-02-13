package net.cyclestreets;

import net.cyclestreets.api.Photo;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class PhotoItem extends OverlayItem 
{
	protected Photo photo;
	private PhotoMarkers photoMarkers;
	
	public PhotoItem(final Photo photo, final PhotoMarkers photoMarkers) 
	{
		super(photo.id + "", photo.caption, new GeoPoint(photo.latitude, photo.longitude));
		this.photo = photo;
		this.photoMarkers = photoMarkers;
	} // PhotoItem

	// Markers
	@Override
	public Drawable getMarker(int stateBitset) { return photoMarkers.getMarker(photo.feature, stateBitset);	}

	@Override
	public Point getMarkerHotspot(int stateBitset) { return photoMarkers.getMarkerHotspot(photo.feature, stateBitset); }

	// Equality testing
	@Override
	public int hashCode() { 
		return ((photo == null) ? 0 : photo.id);
	}
	
	/*
	 * PhotoItems are equal if underlying Photos have the same id
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhotoItem other = (PhotoItem) obj;
		if ((photo == null) && (other.photo != null))
			return false;

		if (other.photo == null)
			return false;

		return (photo.id == other.photo.id);
	}

	@Override
	public String toString() {
		return "PhotoItem [photo=" + photo + "]";
	}	
}
