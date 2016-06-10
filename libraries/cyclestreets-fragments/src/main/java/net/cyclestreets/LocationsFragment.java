package net.cyclestreets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.cyclestreets.content.LocationDatabase;
import net.cyclestreets.content.SavedLocation;
import net.cyclestreets.fragments.R;

import java.util.List;

import static net.cyclestreets.util.MenuHelper.createMenuItem;

public class LocationsFragment extends ListFragment {
  private LocationDatabase locDb_;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    locDb_ = new LocationDatabase(getActivity());
    setListAdapter(new LocationsAdapter(getActivity(), locDb_));
  } // onCreate

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.locations_list, container, false);
  } // onCreateView

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
    registerForContextMenu(getListView());
  } // onActivityCreated

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.locations, menu);
  } // onCreateOptionsMenu

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.ic_menu_addlocation) {
      editLocation(-1);
      return true;
    } // if ...
    return super.onOptionsItemSelected(item);
  } // onOptionsItemSelected

  @Override
  public void onCreateContextMenu(final ContextMenu menu,
                                  final View v,
                                  final ContextMenu.ContextMenuInfo menuInfo) {
    createMenuItem(menu, R.string.ic_menu_edit);
    createMenuItem(menu, R.string.ic_menu_delete);
  }  // onCreateContextMenu

  @Override
  public boolean onContextItemSelected(final MenuItem item)
  {
    try {
      final AdapterView.AdapterContextMenuInfo info
          = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      final int localId = (int)getListAdapter().getItemId(info.position);
      final int menuId = item.getItemId();

      if(R.string.ic_menu_edit == menuId)
        editLocation(localId);
      if(R.string.ic_menu_delete == menuId)
        deleteLocation(localId);

      return true;
    } // try
    catch (final ClassCastException e) {
      return false;
    } // catch
  } // onContextItemSelected

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    editLocation((int) id);
  } // onListItemClick

  private void editLocation(int localId) {
    Intent edit = new Intent(getActivity(), LocationEditorActivity.class);
    edit.putExtra("localId", localId);
    startActivityForResult(edit, 0);
  } // addNewLocation

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    refresh();
  } // onActivity

  private void deleteLocation(int localId) {
    locDb_.deleteLocation(localId);
    refresh();
  } // deleteLocation

  private void refresh() {
    getListAdapter().refresh();
  } // refresh

  @Override
  public LocationsAdapter getListAdapter() {
    return (LocationsAdapter)super.getListAdapter();
  } // getListAdapter

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

    public void refresh() {
      locs_ = locDb_.savedLocations();
      notifyDataSetChanged();
    } // refresh

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
