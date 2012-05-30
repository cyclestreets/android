package net.cyclestreets.views.overlay;

import org.osmdroid.views.MapView;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface ButtonTapListener 
{
	boolean onButtonTap(MotionEvent event);
	boolean onButtonDoubleTap(MotionEvent event);
	
	void drawButtons(final Canvas canvas, final MapView mapView);
} // ButtonTapListener
