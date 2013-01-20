package net.cyclestreets;

import java.util.List;

import net.cyclestreets.content.RouteSummary;
import net.cyclestreets.planned.Route;
import net.cyclestreets.api.Segment;
import net.cyclestreets.util.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class StoredRoutesActivity extends ListActivity 
{
  private static final int MENU_OPEN = 1;
  private static final int MENU_DELETE = 2;
  private static final int MENU_RENAME = 3;
  
  private RouteSummaryAdaptor listAdaptor_;
  
  @Override
  public void onCreate(final Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.storedroutes);
    getWindow().setGravity(Gravity.CENTER);       
    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    getWindow().setBackgroundDrawableResource(R.drawable.empty);
    
    listAdaptor_ = new RouteSummaryAdaptor(this, Route.storedRoutes());
    setListAdapter(listAdaptor_);
    registerForContextMenu(getListView());
  } // onCreate
  
  @Override
  public void onCreateContextMenu(final ContextMenu menu, 
                                  final View v, 
                                  final ContextMenu.ContextMenuInfo menuInfo) 
  {
     menu.add(0, MENU_OPEN, Menu.NONE, "Open");
     menu.add(0, MENU_RENAME, Menu.NONE, "Rename");
     menu.add(0, MENU_DELETE, Menu.NONE, "Delete");
  }  // onCreateContextMenu

  @Override
  public boolean onContextItemSelected(final MenuItem item) 
  {
    try {
      final AdapterView.AdapterContextMenuInfo info 
          = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      int localId = (int)getListAdapter().getItemId(info.position);

      switch(item.getItemId())
      {
        case MENU_OPEN:
          openRoute(localId);
          break;
        case MENU_RENAME:
          renameRoute(localId);
          break;
        case MENU_DELETE:
          deleteRoute(localId);
          break;
      } // switch
        
      return true;
    } // try
    catch (final ClassCastException e) {
      return false;  
    } // catch
  } // onContextItemSelected
   
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id)
  {
    openRoute((int)id);
  } // onListItemClick
  
  private void openRoute(final int localId)
  {
    Intent intent = new Intent();
    intent.putExtra(CycleStreetsConstants.ROUTE_ID, localId);
    setResult(RESULT_OK, intent);
    finish();
  } // routeSelected
  
  private void renameRoute(final int localId)
  {
    final RouteSummary route = listAdaptor_.getRouteSummary(localId);
    Dialog.editTextDialog(this, route.title(), "Rename",
        new Dialog.UpdatedTextListener() {          
          @Override
          public void updatedText(final String updated) {
            Route.RenameRoute(localId, updated);
            listAdaptor_.refresh(Route.storedRoutes());
          } // updatedText
        });
  } // renameRoute
  
  private void deleteRoute(final int localId)
  {
    Route.DeleteRoute(localId);
    listAdaptor_.refresh(Route.storedRoutes());
  } // deleteRoute
   
  //////////////////////////////////
  static class RouteSummaryAdaptor extends BaseAdapter
  {
    private final LayoutInflater inflater_;
    private List<RouteSummary> routes_;
        
    RouteSummaryAdaptor(final Context context, final List<RouteSummary> routes)
    {
      inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      routes_ = routes;
    } // SegmentAdaptor   
    
    public void refresh(final List<RouteSummary> routes)
    {
      routes_ = routes;
      notifyDataSetChanged();
    } // refresh

    public RouteSummary getRouteSummary(int localId) 
    {
      for(final RouteSummary r : routes_)
        if(r.localId() == localId)
          return r;
      return null;
    } // getRouteSummary
    
    @Override
    public int getCount() { return routes_.size(); }
    
    @Override
    public Object getItem(int position) { return routes_.get(position); }

    @Override
    public long getItemId(int position) { return routes_.get(position).localId(); } 
    
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) 
    {
      final RouteSummary summary = routes_.get(position);
      final View v = inflater_.inflate(R.layout.storedroutes_item, parent, false);

      final TextView n = (TextView)v.findViewById(R.id.route_title);
      
      final String p = summary.plan();
      final String plan = p.substring(0,1).toUpperCase() + p.substring(1);
      
      n.setText(summary.title() + "\n" + 
                plan + " route, " + 
                Segment.formatter.total_distance(summary.distance()));
      
      return v;
    } // getView
  } // class RouteSummaryAdaptor
} // class StoredRoutesActivity
