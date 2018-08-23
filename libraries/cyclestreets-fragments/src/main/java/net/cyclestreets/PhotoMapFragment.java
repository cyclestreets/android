package net.cyclestreets;

import net.cyclestreets.views.overlay.PhotosOverlay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoMapFragment extends CycleMapFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved) {
    final View v = super.onCreateView(inflater, container, saved);

    overlayPushBottom(new PhotosOverlay(mapView()));

    return v;
  }
}
