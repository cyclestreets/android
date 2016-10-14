package net.cyclestreets;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.cyclestreets.fragments.R;

import static net.cyclestreets.util.MenuHelper.showMenuItem;

public class ItineraryAndElevationFragment extends Fragment {
  private Fragment lastFrag_;
  private Fragment itinerary_;
  private Fragment elevation_;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    setRetainInstance(true);
    itinerary_ = new ItineraryFragment();
    elevation_ = new ElevationProfileFragment();

    super.onCreate(savedInstanceState);
  } // onCreate

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.itinerary_and_elevation, container, false);
  } // onCreateView

  @Override
  public void onPause() {
    super.onPause();
  } // onPause

  @Override
  public void onResume() {
    super.onResume();
    showFrag(lastFrag_ != null ? lastFrag_ : itinerary_);
  } // onResume

  private void showFrag(Fragment frag) {
    FragmentManager fm = getChildFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();

    if (lastFrag_ != null)
      ft.detach(lastFrag_);

    if (frag != null) {
      String tag = frag.getTag();
      if (fm.findFragmentByTag(tag) == null)
        ft.add(R.id.container, frag, frag.getClass().getSimpleName());
      else
        ft.attach(frag);
    } // if ...

    ft.commit();

    lastFrag_ = frag;

    ActivityCompat.invalidateOptionsMenu(getActivity());
  } // showFrag

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    inflater.inflate(R.menu.itinerary_and_elevation_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  } // onCreateOptionsMenu

  @Override
  public void onPrepareOptionsMenu(final Menu menu) {
    showMenuItem(menu, R.id.ic_menu_itinerary, itinerary_ != lastFrag_);
    showMenuItem(menu, R.id.ic_menu_elevation, elevation_ != lastFrag_);
    super.onPrepareOptionsMenu(menu);
  } // onPrepareOptionsMenu


  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (super.onOptionsItemSelected(item))
      return true;

    final int menuId = item.getItemId();

    if (R.id.ic_menu_itinerary == menuId)
      showFrag(itinerary_);

    if (R.id.ic_menu_elevation == menuId)
      showFrag(elevation_);

    return true;
  } // onMenuItemSelected
} // ItineraryAndElevationFragment
