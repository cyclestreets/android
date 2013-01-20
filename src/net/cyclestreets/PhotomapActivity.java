package net.cyclestreets;

import net.cyclestreets.views.overlay.PhotosOverlay;

import android.os.Bundle;

public class PhotomapActivity extends CycleMapActivity
{
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    overlayPushBottom(new PhotosOverlay(this, mapView()));
  } // onCreate

} // PhotomapActivity
