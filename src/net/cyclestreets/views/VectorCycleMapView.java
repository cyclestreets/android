package net.cyclestreets.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.MapFactory;
import net.cyclestreets.views.overlay.LocationOverlay;
import net.cyclestreets.views.overlay.ControllerOverlay;
import net.cyclestreets.views.overlay.ZoomButtonsOverlay;

import org.mapsforge.android.maps.Projection;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IProjection;
import org.osmdroid.events.MapListener;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;

public class VectorCycleMapView extends ViewGroup
                                implements CycleMapView
{
  private static final String PREFS_APP_SCROLL_X = "scrollX";
  private static final String PREFS_APP_SCROLL_Y = "scrollY";
  private static final String PREFS_APP_ZOOM_LEVEL = "zoomLevel";
  private static final String PREFS_APP_MY_LOCATION = "myLocation";
  private static final String PREFS_APP_FOLLOW_LOCATION = "followLocation";

  private ITileSource renderer_;
  private final SharedPreferences prefs_;
  private final ControllerOverlay controllerOverlay_;
  private final LocationOverlay location_;
  private final int overlayBottomIndex_;
  
  private GeoPoint centreOn_ = null;

  private final org.mapsforge.android.maps.MapView mapsforge_;
  private final OverlayAdaptor overlayAdaptor_;
  
  public VectorCycleMapView(final Context context, final String name)
  {
    super(context);
    
    mapsforge_ = new org.mapsforge.android.maps.MapView(context);
    final String mapFile = Environment.getExternalStorageDirectory().getAbsolutePath() +
                           "/download/great_britain-0.3.0.map";
    mapsforge_.setMapFile(mapFile);
    mapsforge_.setClickable(true);
    mapsforge_.getMapScaleBar().setShowMapScaleBar(true);
    mapsforge_.getMapZoomControls().setShowMapZoomControls(false);
    //mapsforge_.getFpsCounter().setFpsCounter(true);

    overlayAdaptor_ = new OverlayAdaptor(this, mapsforge_);
    
    prefs_ = context.getSharedPreferences("net.cyclestreets.mapview."+name, Context.MODE_PRIVATE);
    
    //setMultiTouchControls(true);
    
    overlayBottomIndex_ = getOverlays().size();
        
    getOverlays().add(new ZoomButtonsOverlay(context, this));
    
    location_ = new LocationOverlay(context, this);
    getOverlays().add(location_);
    
    controllerOverlay_ = new ControllerOverlay(context, this);
    getOverlays().add(controllerOverlay_);
        
    onResume();
  } // CycleMapView
  
  public View view() { return mapsforge_; }
  
  public Overlay overlayPushBottom(final Overlay overlay)
  {
    getOverlays().add(overlayBottomIndex_, overlay);
    return overlay;
  } // overlayPushBottom
  
  public Overlay overlayPushTop(final Overlay overlay)
  {
    // keep TapOverlay on top
    int front = getOverlays().size()-1;
    getOverlays().add(front, overlay);
    return overlay;
  } // overlayPushFront
  
  /////////////////////////////////////////
  // save/restore
  public void onPause()
  {
    final SharedPreferences.Editor edit = prefs_.edit();
    //edit.putInt(PREFS_APP_SCROLL_X, getScrollX());
    //edit.putInt(PREFS_APP_SCROLL_Y, getScrollY());
    edit.putInt(PREFS_APP_ZOOM_LEVEL, getZoomLevel());
    //edit.putBoolean(PREFS_APP_MY_LOCATION, location_.isMyLocationEnabled());
    //edit.putBoolean(PREFS_APP_FOLLOW_LOCATION, location_.isFollowLocationEnabled());

    disableMyLocation();
    
    controllerOverlay_.onPause(edit);
    mapsforge_.onPause();
    
    edit.commit();
  } // onPause

  public void onResume()
  {
    final ITileSource tileSource = mapRenderer();
    if(!tileSource.equals(renderer_))
    {
      renderer_ = tileSource;
      //setTileSource(renderer_);
    } // if ...
    
    location_.enableLocation(pref(PREFS_APP_MY_LOCATION, true));
    if(pref(PREFS_APP_FOLLOW_LOCATION, true))
      location_.enableFollowLocation();
    else
      location_.disableFollowLocation();
        
    //getScroller().abortAnimation();
    
    //GeoPoint gp = new GeoPoint(pref(PREFS_APP_SCROLL_X, 0), 
    //                           pref(PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */
    GeoPoint gp = new GeoPoint(51499266, -124787);
    getController().animateTo(gp);
    getController().setZoom(pref(PREFS_APP_ZOOM_LEVEL, 12));
             
    controllerOverlay_.onResume(prefs_);
    
    mapsforge_.onResume();
  } // onResume 
  
  public boolean onCreateOptionsMenu(final Menu menu)
  {
    return controllerOverlay_.onCreateOptionsMenu(menu);
  } // onCreateOptionsMenu
  
  public boolean onPrepareOptionsMenu(final Menu menu)
  {
    return controllerOverlay_.onPrepareOptionsMenu(menu);
  } // onPrepareOptionsMenu

  public boolean onMenuItemSelected(final int featureId, final MenuItem item)
  {
    return controllerOverlay_.onMenuItemSelected(featureId, item);
  } // onMenuItemSelected
  
  @Override
  public void onCreateContextMenu(final ContextMenu menu) 
  {
    controllerOverlay_.onCreateContextMenu(menu);
  } //  onCreateContextMenu
  
  public boolean onBackPressed()
  {
    return controllerOverlay_.onBackPressed();
  } // onBackPressed
  
  /////////////////////////////////////////
  // location
  public Location getLastFix() { return location_.getLastFix(); }
  public boolean isMyLocationEnabled() { return location_.isMyLocationEnabled(); }
  public void toggleMyLocation() { location_.enableLocation(!location_.isMyLocationEnabled()); }
  public void disableMyLocation() { location_.disableMyLocation(); }
  public void disableFollowLocation() { location_.disableFollowLocation(); }
  public void enableAndFollowLocation() { location_.enableAndFollowLocation(true); }
  
  ///////////////////////////////////////////////////////
  public void centreOn(final GeoPoint place)
  {
    centreOn_ = place;
    invalidate();
  } // centreOn
    
  /*@Override
  protected void dispatchDraw(final Canvas canvas)
  {
    if(centreOn_  != null)
    {
      getController().animateTo(new GeoPoint(centreOn_));
      centreOn_ = null;
      return;
    } // if ..
    
    super.dispatchDraw(canvas);
  } // dispatchDraw
  */
  ///////////////////////////////////////////////////////
  private int pref(final String key, int defValue)
  {
    return prefs_.getInt(key, defValue);
  } // pref
  private boolean pref(final String key, boolean defValue)
  {
    return prefs_.getBoolean(key, defValue);
  } // pref
  
  public String mapAttribution()
  {
    try {
      return attribution_.get(CycleStreetsPreferences.mapstyle());
    }
    catch(Exception e) { 
      // sigh
    } // catch
    return attribution_.get(DEFAULT_RENDERER);
  } // mapAttribution
  
  private ITileSource mapRenderer()
  {    
    try { 
      return TileSourceFactory.getTileSource(CycleStreetsPreferences.mapstyle());
     } // try
    catch(Exception e) {
      // oh dear 
    } // catch
    return TileSourceFactory.getTileSource(DEFAULT_RENDERER);
  } // mapRenderer
  
  static private String DEFAULT_RENDERER = "CycleStreets-OSM";
  static private Map<String, String> attribution_ = 
      MapFactory.map("CycleStreets", "\u00a9 OpenStreetMap and contributors, CC-BY-SA. Map images \u00a9 OpenCycleMap")
                .map("CycleStreets-OSM", "\u00a9 OpenStreetMap and contributors, CC-BY-SA")
                .map("CycleStreets-OS", "Contains Ordnance Survey Data \u00a9 Crown copyright and database right 2010");
  
  static 
  { 
    final OnlineTileSourceBase OPENCYCLEMAP = new XYTileSource("CycleStreets",
                    ResourceProxy.string.cyclemap, 0, 17, 256, ".png",
                    "http://a.tile.opencyclemap.org/cycle/",
                    "http://b.tile.opencyclemap.org/cycle/",
                    "http://c.tile.opencyclemap.org/cycle/");
    final OnlineTileSourceBase OPENSTREETMAP = new XYTileSource("CycleStreets-OSM",
                    ResourceProxy.string.osmarender, 0, 17, 256, ".png",
                    "http://a.tile.openstreetmap.org/",
                    "http://b.tile.openstreetmap.org/",
                    "http://c.tile.openstreetmap.org/");
    final OnlineTileSourceBase OSMAP = new XYTileSource("CycleStreets-OS",
                    ResourceProxy.string.unknown, 0, 17, 256, ".png",
                    "http://a.os.openstreetmap.org/sv/",
                    "http://b.os.openstreetmap.org/sv/",
                    "http://c.os.openstreetmap.org/sv/");
    TileSourceFactory.addTileSource(OPENCYCLEMAP);
    TileSourceFactory.addTileSource(OPENSTREETMAP);
    TileSourceFactory.addTileSource(OSMAP);
  } // static

  @Override
  public BoundingBoxE6 getBoundingBox()
  {
    try {
      IGeoPoint c = getMapCenter();
      double halfWidth = getLongitudeSpan()/2.0;
      double halfHeight = getLatitudeSpan()/2.0;
    
      return new BoundingBoxE6(c.getLatitude()-halfHeight,
                               c.getLongitude()-halfWidth,
                               c.getLatitude()+halfHeight,
                               c.getLongitude()+halfWidth);
    }
    catch(Exception e) {
      return new BoundingBoxE6(1,-1,0,1);
    }
  }

  @Override
  public void setMapListener(MapListener ml)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public IMapController getController()
  {
    return new org.osmdroid.mapsforge.wrapper.MapController(mapsforge_.getController());
  }

  @Override
  public IProjection getProjection()
  {
    return new org.osmdroid.mapsforge.wrapper.Projection(mapsforge_);
  }

  @Override
  public int getZoomLevel()
  {
    return mapsforge_.getMapPosition().getZoomLevel();
  }

  @Override
  public int getMaxZoomLevel()
  {
    return mapsforge_.getMapZoomControls().getZoomLevelMax();
  }

  @Override
  public int getLatitudeSpan()
  {
    return mapsforge_.getProjection().getLatitudeSpan();
  }

  @Override
  public int getLongitudeSpan()
  {
    return mapsforge_.getProjection().getLongitudeSpan();
  }

  @Override
  public IGeoPoint getMapCenter() 
  {
    return new org.osmdroid.mapsforge.wrapper.GeoPoint(mapsforge_.getMapPosition().getMapCenter());
  }

  @Override
  public List<Overlay> getOverlays()
  {
    return overlayAdaptor_.overlays();
  } // getOverlays
  
  @Override
  public boolean isAnimating() 
  {
    return mapsforge_.isZoomAnimatorRunning();
  } // isAnimating
  
  @Override
  public boolean canZoomIn()
  {
    return true;
  } // canZoomIn
  
  @Override
  public boolean canZoomOut()
  {
    return true;
  } // canZoomOut

  // ViewGroup
  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b)
  {
    mapsforge_.getMapZoomControls().onLayout(changed, l, t, r, b);
  } // onLayout
  
  // OverlayAdaptor
  static private class OverlayAdaptor extends org.mapsforge.android.maps.overlay.Overlay
  {
    private final List<Overlay> overlays_ = new ArrayList<Overlay>();
    private final VectorCycleMapView owner_;

    public OverlayAdaptor(VectorCycleMapView owner,
                          org.mapsforge.android.maps.MapView mapView)
    {
      mapView.getOverlays().add(this);
      owner_ = owner;
    } // OverlayAdaptor
    
    public List<Overlay> overlays() { return overlays_; }
    
    @Override
    protected void drawOverlayBitmap(final Canvas canvas, 
                                     final Point position, 
                                     final Projection projection,
                                     final byte zoomLevel)
    {
      for(final Overlay o : overlays_)
        o.draw(canvas, owner_, false);
    } // drawOverlayBitmap    
  } // class OverlayAdaptor
} // CycleMapView
