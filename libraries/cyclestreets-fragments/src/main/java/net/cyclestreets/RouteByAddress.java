package net.cyclestreets;

import net.cyclestreets.fragments.R;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.views.PlaceViewWithCancel;
import net.cyclestreets.api.GeoPlace;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout.LayoutParams;

public class RouteByAddress {
  public static void launch(final Context context,
                            final BoundingBoxE6 boundingBox,
                            final Location lastFix,
                            final Waypoints waypoints) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.ic_menu_directions);

    final RouteByAddressCallbacks rbac = new RouteByAddressCallbacks(context, builder, boundingBox, lastFix, waypoints);

    final AlertDialog ad = builder.create();
    ad.show();

    rbac.setDialog(ad);
  } // launch

  private static class RouteByAddressCallbacks implements View.OnClickListener {
    private final Context context_;
    private final LinearLayout placeHolder_;
    private final RadioGroup routeType_;
    private final Button addWaypoint_;

    private final BoundingBoxE6 bounds_;
    private final IGeoPoint currentLoc_;
    private final Waypoints waypoints_;

    private final String START_MARKER_LABEL;
    private final String FINISH_MARKER_LABEL;
    private final String WAYPOINT_LABEL;

    private AlertDialog ad_;

    public RouteByAddressCallbacks(final Context context,
                                   final AlertDialog.Builder builder,
                                   final BoundingBoxE6 boundingBox,
                                   final Location lastFix,
                                   final Waypoints waypoints) {
      context_ = context;

      START_MARKER_LABEL = context_.getResources().getString(R.string.rba_start);
      FINISH_MARKER_LABEL = context_.getResources().getString(R.string.rba_finish);
      WAYPOINT_LABEL = context_.getResources().getString(R.string.rba_waypoint);

      final View layout = View.inflate(context, R.layout.routebyaddress, null);
      builder.setView(layout);

      builder.setPositiveButton(R.string.go, MessageBox.NoAction);

      bounds_ = boundingBox;
      currentLoc_ = new GeoPoint(lastFix.getLatitude(), lastFix.getLongitude());

      placeHolder_ = (LinearLayout) layout.findViewById(R.id.places);
      waypoints_ = waypoints;

      addWaypoint_ = (Button) layout.findViewById(R.id.addVia);
      addWaypoint_.setOnClickListener(this);

      routeType_ = (RadioGroup) layout.findViewById(R.id.routeTypeGroup);
      routeType_.check(RouteTypeMapper.idFromName(CycleStreetsPreferences.routeType()));

      addWaypointBox();
      addWaypointBox();
    } // RouteActivity

    public void setDialog(final AlertDialog ad) {
      ad_ = ad;
      ad_.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    } // setDialog

    private void findRoute(final List<GeoPlace> places) {
      for (final GeoPlace wp : places)
        for (int i = 0; i != placeHolder_.getChildCount(); ++i) {
          final PlaceViewWithCancel p = (PlaceViewWithCancel) placeHolder_.getChildAt(i);
          p.addHistory(wp);
        } // for ...

      final String routeType = RouteTypeMapper.nameFromId(routeType_.getCheckedRadioButtonId());
      final int speed = CycleStreetsPreferences.speed();
      Route.PlotRoute(routeType, speed, context_, asWaypoints(places));

      ad_.dismiss();
    } // findRoute

    private void addWaypointBox() {
      final PlaceViewWithCancel pv = new PlaceViewWithCancel(context_);
      pv.setBounds(bounds_);

      if (currentLoc_ != null)
        pv.allowCurrentLocation(currentLoc_, placeHolder_.getChildCount() == 0);

      for (int w = 0; w != waypoints_.count(); ++w) {
        String label = String.format(WAYPOINT_LABEL, w);

        if (w == 0)
          label = START_MARKER_LABEL;
        else if (w + 1 == waypoints_.count())
          label = FINISH_MARKER_LABEL;

        pv.allowLocation(waypoints_.get(w), label);
      } // for ...

      pv.setCancelOnClick(new OnRemove(pv));

      placeHolder_.addView(pv);
      pv.requestFocus();

      enableRemoveButtons();
    } // addWaypointBox

    private void removeWaypointBox(final PlaceViewWithCancel pv) {
      placeHolder_.removeView(pv);
      enableRemoveButtons();
    } // removeWaypointBox

    private void enableRemoveButtons() {
      final boolean enable = placeHolder_.getChildCount() > 2;

      for (int i = 0; i != placeHolder_.getChildCount(); ++i) {
        final PlaceViewWithCancel p = (PlaceViewWithCancel) placeHolder_.getChildAt(i);
        p.enableCancel(enable);
      } // for ...

      addWaypoint_.setEnabled(placeHolder_.getChildCount() < 12);
    } // enableRemoveButtons

    private Waypoints asWaypoints(final List<GeoPlace> places) {
      final Waypoints points = new Waypoints();
      for (GeoPlace place : places)
        points.add(place.coord());
      return points;
    } // asWaypoints

    @Override
    public void onClick(final View view) {
      final int viewId = view.getId();

      if (AlertDialog.BUTTON_POSITIVE == viewId)
        resolvePlaces();
      if (R.id.addVia == viewId)
        addWaypointBox();
    } // onClick

    private void resolvePlaces() {
      resolveNextPlace(new ArrayList<GeoPlace>(), 0);
    } // resolvePlaces

    private void resolveNextPlace(final List<GeoPlace> resolvedPlaces, final int index) {
      if (index != placeHolder_.getChildCount()) {
        final PlaceViewWithCancel pv = (PlaceViewWithCancel) placeHolder_.getChildAt(index);
        pv.geoPlace(new PlaceViewWithCancel.OnResolveListener() {
          @Override
          public void onResolve(GeoPlace place) {
            resolvedPlaces.add(place);
            resolveNextPlace(resolvedPlaces, index + 1);
          }
        });
      } else
        findRoute(resolvedPlaces);
    } // resolveNextPlace

    private class OnRemove implements OnClickListener {
      private final PlaceViewWithCancel pv_;

      public OnRemove(final PlaceViewWithCancel pv) {
        pv_ = pv;
      } // OnRemove

      @Override
      public void onClick(final View view) {
        removeWaypointBox(pv_);
      } // onClick
    } // class OnRemove
  } // RouteByAddressCallbacks
} // RouteByAddress
