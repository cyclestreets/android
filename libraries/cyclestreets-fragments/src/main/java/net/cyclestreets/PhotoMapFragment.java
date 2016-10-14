package net.cyclestreets;

import net.cyclestreets.views.overlay.PhotosOverlay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoMapFragment extends CycleMapFragment
{
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved)
  {
    final View v = super.onCreateView(inflater, container, saved);

    overlayPushBottom(new PhotosOverlay(getActivity(), mapView()));

    return v;
  } // onCreate
} // PhotomapActivity
