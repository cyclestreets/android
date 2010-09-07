package net.cyclestreets;

import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.content.Context;
import android.util.AttributeSet;

/*
 * A mapview that dispatches scrollTo events when scrolled
 */
public class MapListenerMapView extends OpenStreetMapView {
	protected MapListener mapListener = null;
	
	public MapListenerMapView(Context context) {
		super(context);
	}
	
	public MapListenerMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MapListenerMapView(Context context, OpenStreetMapRendererInfo aRendererInfo) {
		super(context, aRendererInfo);
	}

	public MapListenerMapView(Context context, OpenStreetMapRendererInfo aRendererInfo, OpenStreetMapView aMapToShareTheTileProviderWith) {
		super(context, aRendererInfo, aMapToShareTheTileProviderWith);
	}

	public void addMapListener(MapListener sl) {
		mapListener = sl;
	}

	public void removeMapListener() {
		mapListener = null;
	}
	
	public MapListener[] getMapListeners() {
		return mapListeners.toArray(new MapListener[0]);
	}
	
	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		if (mapListener != null) {
			mapListener.mapScrolled(new ScrollEvent(this, x, y));
		}
	}
	
	@Override
	protected boolean zoomIn() {
		if (super.zoomIn()) {
			if (mapListener != null) {
				mapListener.mapZoomed(new ZoomEvent(this, ZoomEvent.IN, getZoomLevel()));
			}
		}
	}

	@Override
	protected boolean zoomOut() {
		if (super.zoomOut()) {
			if (mapListener != null) {
				mapListener.mapZoomed(new ZoomEvent(this, ZoomEvent.OUT, getZoomLevel()));
			}
		}
	}
}
