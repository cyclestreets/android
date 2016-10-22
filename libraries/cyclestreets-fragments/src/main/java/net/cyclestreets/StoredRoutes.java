package net.cyclestreets;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.cyclestreets.content.RouteSummary;
import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.util.Dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.StringUtils.initCap;

public class StoredRoutes {
  public static void launch(final Context context) {
    RouteSummaryAdapter rsa = new RouteSummaryAdapter(context);
    AlertDialog ad = Dialog.listViewDialog(context,
        R.string.menu_saved_routes,
        rsa,
        null,
        null);
    ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(context, android.R.style.TextAppearance_Large);
    rsa.setDialog(ad);
  } // launch

  //////////////////////////////////
  private static class RouteSummaryAdapter extends BaseAdapter
      implements View.OnClickListener,
      View.OnLongClickListener,
      View.OnCreateContextMenuListener {
    private final Context context_;
    private final LayoutInflater inflater_;
    private List<RouteSummary> routes_;
    private final Map<View, Integer> viewRoute_;
    private AlertDialog ad_;

    RouteSummaryAdapter(final Context context) {
      context_ = context;
      inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      routes_ = Route.storedRoutes();
      viewRoute_ = new HashMap<>();
    } // SegmentAdaptor

    public void setDialog(final AlertDialog ad) { ad_ = ad; }

    private void refresh() {
      routes_ = Route.storedRoutes();
      notifyDataSetChanged();
      if (routes_.size() == 0)
        closeDialog();
    } // refresh

    private void closeDialog() {
      if (ad_ != null)
        ad_.cancel();
    } // closeDialog

    public RouteSummary getRouteSummary(int localId) {
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
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      final RouteSummary summary = routes_.get(position);
      View view = (convertView == null) ?
          inflater_.inflate(R.layout.storedroutes_item, parent, false) :
          convertView;
      viewRoute_.put(view, summary.localId());

      final TextView titleView = (TextView)view.findViewById(R.id.route_title);
      final TextView detailView = (TextView)view.findViewById(R.id.route_details);

      final String plan = initCap(summary.plan());

      titleView.setText(summary.title());
      detailView.setText(context_.getString(R.string.storedroutes_detail_format, plan,
                                            Segment.formatter.total_distance(summary.distance())));

      view.setOnClickListener(this);
      view.setOnLongClickListener(this);
      view.setOnCreateContextMenuListener(this);

      return view;
    } // getView

    @Override
    public void onClick(final View view) {
      final int localId = viewRoute_.get(view);
      openRoute(localId);
    } // onClick

    @Override
    public boolean onLongClick(final View view) {
      view.showContextMenu();
      return true;
    } // onClick

    @Override
    public void onCreateContextMenu(final ContextMenu menu,
                                    final View view,
                                    final ContextMenu.ContextMenuInfo contextMenuInfo) {
      final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(final MenuItem item) {
          RouteSummaryAdapter.this.onViewMenuClick(view, item);
          return true;
        } // onMenuItemClick
      };
      createMenuItem(menu, R.string.ic_menu_open).setOnMenuItemClickListener(listener);
      createMenuItem(menu, R.string.ic_menu_rename).setOnMenuItemClickListener(listener);
      createMenuItem(menu, R.string.ic_menu_delete).setOnMenuItemClickListener(listener);
    } // onCreateContextMenu

    private void onViewMenuClick(final View view, final MenuItem item) {
      final int localId = viewRoute_.get(view);
      final int menuId = item.getItemId();

      if(R.string.ic_menu_open == menuId)
        openRoute(localId);
      if(R.string.ic_menu_rename == menuId)
        renameRoute(localId);
      if(R.string.ic_menu_delete == menuId)
        deleteRoute(localId);
    } // onMenuItemClick

    //////////////////////////////////////////////
    /////////////////////////////////////////////
    private void openRoute(final int localId) {
      Route.PlotStoredRoute(localId, context_);
      closeDialog();
    } // routeSelected

    private void renameRoute(final int localId)
    {
      final RouteSummary route = getRouteSummary(localId);
      Dialog.editTextDialog(context_, route.title(), "Rename",
          new Dialog.UpdatedTextListener() {
            @Override
            public void updatedText(final String updated) {
              Route.RenameRoute(localId, updated);
              refresh();
            } // updatedText
          });
    } // renameRoute

    private void deleteRoute(final int localId) {
      Route.DeleteRoute(localId);
      refresh();
    } // deleteRoute
  } // class RouteSummaryAdaptor

  private StoredRoutes() { }
} // StoredRoutes
