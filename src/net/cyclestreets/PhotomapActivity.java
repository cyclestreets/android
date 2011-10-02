package net.cyclestreets;

import net.cyclestreets.views.overlay.PhotoItemOverlay;

import android.os.Bundle;

public class PhotomapActivity extends CycleMapActivity
{
	public void onCreate(Bundle savedInstanceState) 
	{
	  super.onCreate(savedInstanceState);

	  overlayPushBottom(new PhotoItemOverlay(this, mapView()));
  } // onCreate

} // PhotomapActivity
