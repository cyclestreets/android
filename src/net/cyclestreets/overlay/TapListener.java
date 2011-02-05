package net.cyclestreets.overlay;

import android.view.MotionEvent;

public interface TapListener 
{
	boolean onSingleTap(MotionEvent event);
	boolean onDoubleTap(MotionEvent event);
} // SingletapListener
