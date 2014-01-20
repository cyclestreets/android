package net.cyclestreets;

import net.cyclestreets.fragments.R;

import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.MenuHelper.enableMenuItem;

public class CycleMapFragment extends Fragment implements Undoable
{
  private CycleMapView map_;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved)
  {
    super.onCreate(saved);

    map_ = new CycleMapView(getActivity(), this.getClass().getName());

    final RelativeLayout rl = new RelativeLayout(getActivity());
    rl.addView(map_, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    return rl;
  } // onCreate

  protected CycleMapView mapView() { return map_; }
  protected Overlay overlayPushBottom(final Overlay overlay) { return map_.overlayPushBottom(overlay); }
  protected Overlay overlayPushTop(final Overlay overlay) { return map_.overlayPushTop(overlay); }

  protected void findPlace() { launchFindDialog(); }

  @Override
  public void onPause()
  {
    map_.onPause();
    super.onPause();
  } // onPause

  @Override
  public void onResume()
  {
    super.onResume();
    map_.onResume();
  } // onResume

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
  {
    map_.onCreateOptionsMenu(menu);
    createMenuItem(menu, R.string.ic_menu_findplace, Menu.NONE, R.drawable.ic_menu_search);
    createMenuItem(menu, R.string.ic_menu_settings, 99, R.drawable.ic_menu_settings);
  } // onCreateOptionsMenu

  @Override
  public void onPrepareOptionsMenu(final Menu menu)
  {
    map_.onPrepareOptionsMenu(menu);
    enableMenuItem(menu, R.string.ic_menu_findplace, true);
    enableMenuItem(menu, R.string.ic_menu_settings, true);
  } // onPrepareOptionsMenu

  @Override
  public boolean onOptionsItemSelected(final MenuItem item)
  {
    if(map_.onMenuItemSelected(item.getItemId(), item))
      return true;

    if(item.getItemId() == R.string.ic_menu_findplace)
    {
      launchFindDialog();
      return true;
    } // if ...

    if(item.getItemId() == R.string.ic_menu_settings)
    {
      startActivity(new Intent(getActivity(), SettingsActivity.class));
      return true;
    } // if ...

    return false;
  } // onMenuItemSelected

  @Override
  public boolean onContextItemSelected(final MenuItem item)
  {
    return map_.onMenuItemSelected(item.getItemId(), item);
  } // onContextItemSelected


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);

    if(resultCode != Activity.RESULT_OK)
      return;

    if(requestCode != ActivityId.FindPlace)
      return;

    final GeoPoint place = GeoIntent.getGeoPoint(data);
    // we're in the wrong thread, so pop this away for later and force a refresh
    map_.centreOn(place);
  } // onActivityResult

  private void launchFindDialog()
  {
    final Intent intent = new Intent(getActivity(), FindPlaceActivity.class);
    GeoIntent.setBoundingBox(intent, map_.getBoundingBox());
    startActivityForResult(intent, ActivityId.FindPlace);
  } // launchFindDialog

  @Override
  public boolean onBackPressed()
  {
    return map_.onBackPressed();
  } // onBackPressed
} // CycleMapFragment
