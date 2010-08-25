package net.cyclestreets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.cyclestreets.api.Photo;
import android.util.Log;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.components.Place;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.listeners.MapListener;
import com.nutiteq.utils.Utils;

/*
 * A MapView that dispatches additional listeners on Map events
 */
public class DispatchingMapView extends MapView {
	protected BasicMapComponent mapComponent;
	protected Set<MapListener> listeners = new CopyOnWriteArraySet<MapListener>();		
	
	DispatchingMapView(android.content.Context context, BasicMapComponent component) {
		super(context, component);
		mapComponent = component;
	}

	public void addMapListener(MapListener ml) {
		Log.d(getClass().getSimpleName(), "registering listener: " + ml);
		listeners.add(ml);
		Log.d(getClass().getSimpleName(), "listeners = " + listeners);
	}

	public MapListener[] getMapListeners() {
		return listeners.toArray(new MapListener[0]);
	}
	
	public void removeMapListener(MapListener ml) {
		Log.d(getClass().getSimpleName(), "removing listener: " + ml);
		listeners.remove(ml);
		Log.d(getClass().getSimpleName(), "listeners = " + listeners);
	}
	
	@Override
	public void mapClicked(WgsPoint arg0) {
		Log.d(getClass().getSimpleName(), "map clicked! " + arg0);
		super.mapClicked(arg0);
		for (MapListener ml: listeners) {
			ml.mapClicked(arg0);
		}
	}

	@Override
	public void mapMoved() {
		super.mapMoved();
		for (MapListener ml: listeners) {
			ml.mapMoved();
		}
	}

	@Override
	public void needRepaint(boolean arg0) {
		super.needRepaint(arg0);
		for (MapListener ml: listeners) {
			ml.needRepaint(arg0);
		}
	}
}
