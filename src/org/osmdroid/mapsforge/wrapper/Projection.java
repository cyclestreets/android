package org.osmdroid.mapsforge.wrapper;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;
import org.osmdroid.util.GeoPoint;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * A wrapper for the Google {@link org.mapsforge.android.maps.Projection} implementation.
 * This implements {@link IProjection}, which is also implemented by the osmdroid
 * {@link org.osmdroid.views.MapView.Projection}.
 *
 * @author Neil Boyd
 *
 */
public class Projection implements IProjection 
{
  private final org.mapsforge.android.maps.MapView mapView_;
  private final org.mapsforge.android.maps.Projection mProjection;

  public Projection(final org.mapsforge.android.maps.MapView mapView) 
  {
    mapView_ = mapView;
    mProjection = mapView.getProjection();
  }

  private org.mapsforge.core.GeoPoint gp(final IGeoPoint in)
  {
    return new org.mapsforge.core.GeoPoint(in.getLatitudeE6(), in.getLongitudeE6());
  } // gp
  
  @Override
  public Point toPixels(final IGeoPoint in, final Point out) 
  {
    return mProjection.toPixels(gp(in), out);
  }

  @Override
  public IGeoPoint fromPixels(final int x, final int y) 
  {
    final org.mapsforge.core.GeoPoint googleGeoPoint = mProjection.fromPixels(x, y);
    return new GeoPoint(googleGeoPoint.getLatitudeE6(), googleGeoPoint.getLongitudeE6());
  }
	
  @Override
  public float metersToEquatorPixels(final float meters) 
  {
    throw new RuntimeException("Projection.metersToEquatorPixels");
  }

  public Point fromMapPixels(final int x, final int y, final Point reuse) 
  {
    throw new RuntimeException("Projection.fromMapPixels not implemented");
  }

  @Override
  public Rect fromPixelsToProjected(Rect arg0)
  {
    throw new RuntimeException("Projection.fromPixelsToProjected not implemented");
  }

  @Override
  public Rect getScreenRect()
  {
    throw new RuntimeException("Projection.getScreenRect not implemented");
  }

  @Override
  public int getZoomLevel()
  {
    return mapView_.getMapPosition().getZoomLevel();
  }

  @Override
  public Point toMapPixelsProjected(int arg0, int arg1, Point arg2)
  {
     throw new RuntimeException("Projection.toMapPixelsProjected not implemented");
  }

  @Override
  public Point toMapPixelsTranslated(Point arg0, Point arg1)
  {
    throw new RuntimeException("Projection.toMapPixelsTranslated not implemented");
  } 
}
