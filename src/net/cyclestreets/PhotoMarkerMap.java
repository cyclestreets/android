package net.cyclestreets;

import org.andnav.osm.views.overlay.MarkerMap;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class PhotoMarkerMap extends MarkerMap<PhotoItem> {
	protected Drawable[] icons;

	public PhotoMarkerMap(Resources res) {
		super(res.getDrawable(R.drawable.icon), new Point(13,47));
		final Drawable[] iconInit = {
			res.getDrawable(R.drawable.icon),						// 0
			res.getDrawable(R.drawable.mm_20_white_wisp),			// 1
			res.getDrawable(R.drawable.mm_20_bike),					// 2
			res.getDrawable(R.drawable.mm_20_sheffield_stands),		// 3
			res.getDrawable(R.drawable.mm_20_cycleway),				// 4
			res.getDrawable(R.drawable.mm_20_directional_signage),	// 5
			res.getDrawable(R.drawable.mm_20_general_sign),			// 6
			res.getDrawable(R.drawable.icon),						// 7
			res.getDrawable(R.drawable.mm_20_obstruction),			// 8
			res.getDrawable(R.drawable.mm_20_destination),			// 9
			res.getDrawable(R.drawable.mm_20_black),				// 10
			res.getDrawable(R.drawable.mm_20_spanner),				// 11
			res.getDrawable(R.drawable.mm_20_car_parking),			// 12
			res.getDrawable(R.drawable.mm_20_enforcement),			// 13
			res.getDrawable(R.drawable.mm_20_roadworks),			// 14
			res.getDrawable(R.drawable.mm_20_cone),					// 15
			res.getDrawable(R.drawable.mm_20_congestion),			// 16
			res.getDrawable(R.drawable.mm_20_road),					// 17
		};
		icons = iconInit;
	}
	
	@Override
	public Drawable getMarker(PhotoItem item) {
		try {
			return icons[item.photo.feature];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return super.getMarker(item);
		}
	}

	@Override
	public Point getHotspot(PhotoItem item) {
		return new Point(13, 47);
	}
}
