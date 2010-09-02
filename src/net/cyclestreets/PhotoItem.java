package net.cyclestreets;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

public class PhotoItem extends OpenStreetMapViewOverlayItem {
	public PhotoItem(String aTitle, String aDescription, GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
	}
}
