package net.cyclestreets.overlay;

import org.osmdroid.views.MapView;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface TapListener 
{
	boolean onSingleTap(MotionEvent event);
	boolean onDoubleTap(MotionEvent event);
	
	void drawButtons(final Canvas canvas, final MapView mapView);
} // SingletapListener
