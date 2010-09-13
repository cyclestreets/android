package net.cyclestreets;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class PhotoMarkers {
	protected Drawable[] markers;
	protected Drawable defaultMarker;
	protected Point defaultMarkerHotspot;

	public PhotoMarkers(Resources res) {
		defaultMarker = res.getDrawable(R.drawable.icon);
		defaultMarkerHotspot = new Point(13,47);

		Drawable[] markersInit = {
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
		markers = markersInit;
	}
	
	public Drawable getMarker(int feature, int stateBitset) {
		try {
			return markers[feature];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return defaultMarker;
		}
	}

	public Point getMarkerHotspot(int feature, int stateBitset) {
		return defaultMarkerHotspot;
	}
}
