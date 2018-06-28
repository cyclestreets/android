package net.cyclestreets;

import net.cyclestreets.fragments.R;

import net.cyclestreets.views.CycleMapView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.Overlay;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.MenuHelper.enableMenuItem;

public class CycleMapFragment extends Fragment implements Undoable
{
  private CycleMapView map_;
  private boolean forceMenuRebuild_;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved) {
    super.onCreate(saved);

    forceMenuRebuild_ = true;

    map_ = new CycleMapView(getActivity(), this.getClass().getName());

    return map_;
  }

  protected CycleMapView mapView() { return map_; }
  protected Overlay overlayPushBottom(final Overlay overlay) { return map_.overlayPushBottom(overlay); }
  protected Overlay overlayPushTop(final Overlay overlay) { return map_.overlayPushTop(overlay); }

  protected void findPlace() { launchFindDialog(); }

  @Override
  public void onPause() {
    map_.onPause();
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    map_.onResume();
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    if (map_ != null)
      map_.onCreateOptionsMenu(menu);
    createMenuItem(menu, R.string.menu_find_place, Menu.NONE, R.drawable.ic_menu_search);
  }

  @Override
  public void onPrepareOptionsMenu(final Menu menu) {
    if (forceMenuRebuild_) {
      forceMenuRebuild_ = false;
      menu.clear();
      onCreateOptionsMenu(menu, getActivity().getMenuInflater());
      onPrepareOptionsMenu(menu);
    }

    if (map_ != null)
      map_.onPrepareOptionsMenu(menu);
    enableMenuItem(menu, R.string.menu_find_place, true);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (map_.onMenuItemSelected(item.getItemId(), item))
      return true;

    if (item.getItemId() == R.string.menu_find_place) {
      launchFindDialog();
      return true;
    }

    return false;
  }

  @Override
  public boolean onContextItemSelected(final MenuItem item) {
    return map_.onMenuItemSelected(item.getItemId(), item);
  }

  private void launchFindDialog() {
    FindPlace.launch(getActivity(), map_.getBoundingBox(), new FindPlace.Listener() {
      @Override
      public void onPlaceFound(IGeoPoint place) {
        map_.centreOn(place);
      }
    });
  }

  @Override
  public boolean onBackPressed() {
    return map_.onBackPressed();
  }
}
