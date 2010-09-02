package net.cyclestreets;

import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.content.Context;
import android.util.AttributeSet;

/*
 * A mapview that dispatches scrollTo events when scrolled
 */
// TODO: add zoom events
public class ScrollListenerMapView extends OpenStreetMapView {
	protected ScrollListener scrollListener = null;
	
	public ScrollListenerMapView(Context context) {
		super(context);
	}
	
	public ScrollListenerMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollListenerMapView(Context context, OpenStreetMapRendererInfo aRendererInfo) {
		super(context, aRendererInfo);
	}

	public ScrollListenerMapView(Context context, OpenStreetMapRendererInfo aRendererInfo, OpenStreetMapView aMapToShareTheTileProviderWith) {
		super(context, aRendererInfo, aMapToShareTheTileProviderWith);
	}

	public void addScrollListener(ScrollListener sl) {
		scrollListener = sl;
	}

	public void removeScrollListener() {
		scrollListener = null;
	}
	
	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		if (scrollListener != null) {
			scrollListener.scrollTo(x, y);
		}
	}
}
