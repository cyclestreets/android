package net.cyclestreets;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.cyclestreets.content.LocationDatabase;
import net.cyclestreets.content.SavedLocation;
import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.Segment;

import java.util.List;

public class LocationsFragment extends ListFragment {
  private LocationDatabase locDb_;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    locDb_ = new LocationDatabase(getActivity());
    setListAdapter(new LocationsAdapter(getActivity(), locDb_));
  } // onCreate

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  } // onActivityCreated

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.locations, menu);
  } // onCreateOptionsMenu

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.ic_menu_addlocation) {
      addNewLocation();
      return true;
    } // if ...
    return super.onOptionsItemSelected(item);
  } // onOptionsItemSelected

  private void addNewLocation() {

  } // addNewLocation
  //////////////////////////////////
  static class LocationsAdapter extends BaseAdapter {
    private final LayoutInflater inflater_;
    private LocationDatabase locDb_;
    private List<SavedLocation> locs_;

    LocationsAdapter(final Context context, final LocationDatabase locDb) {
      inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      locDb_ = locDb;
      locs_ = locDb_.savedLocations();
    } // SegmentAdaptor

    @Override
    public int getCount() { return locs_.size(); }

    @Override
    public Object getItem(int position) { return locs_.get(position); }

    @Override
    public long getItemId(int position) { return locs_.get(position).localId(); }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      final SavedLocation location = locs_.get(position);
      final View v = inflater_.inflate(R.layout.storedroutes_item, parent, false);

      final TextView n = (TextView)v.findViewById(R.id.route_title);
      n.setText(location.name());

      return v;
    } // getView
  } // class LocationsAdaptor

} // LocationsFragment
