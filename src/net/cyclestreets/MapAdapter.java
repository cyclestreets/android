package net.cyclestreets;

import com.nutiteq.components.WgsPoint;
import com.nutiteq.listeners.MapListener;

public abstract class MapAdapter implements MapListener {
	@Override
	public void mapClicked(WgsPoint arg0) {
		// do nothing
	}

	@Override
	public void mapMoved() {
		// do nothing
	}

	@Override
	public void needRepaint(boolean arg0) {
		// do nothing
	}
}
