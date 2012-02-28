package net.cyclestreets.views;

import java.util.List;

import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public interface CycleMapView extends IMapView
{
  View view();
  
  List<Overlay> getOverlays();
  Overlay overlayPushTop(Overlay overlay);
  Overlay overlayPushBottom(Overlay overlay);
  
  void onPause();
  void onResume();
  
  void invalidate();
  void postInvalidate();
  
  Location getLastFix();
  BoundingBoxE6 getBoundingBox();
  void centreOn(GeoPoint place);
  void enableAndFollowLocation();
  void disableFollowLocation();
  boolean isMyLocationEnabled();
  boolean onTrackballEvent(MotionEvent event);
  boolean onBackPressed();
  
  boolean onCreateOptionsMenu(Menu menu);
  boolean onMenuItemSelected(int featureId, MenuItem item);
  boolean onPrepareOptionsMenu(Menu menu);
  boolean showContextMenu();
  
  void setMapListener(final MapListener ml);

  Context getContext();

  String mapAttribution();

} // CycleMapView