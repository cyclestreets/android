package org.mapsforge.android.maps;

import java.io.InputStream;

import org.mapsforge.android.maps.mapgenerator.MapGenerator;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.core.Tile;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.graphics.drawable.Drawable;

public class MapsforgeOSMTileSource implements ITileSource
{
  final String name_;
  final MapGenerator mapGenerator_;

  
  public MapsforgeOSMTileSource(final String name)
  {
    name_ = name;
    mapGenerator_ = new DatabaseRenderer();
  } // MapsforgeOSMTileSource

  @Override
  public String localizedName(ResourceProxy proxy) { return name(); }
  @Override
  public String name() { return name_; }
  @Override
  public int ordinal() { return name_.hashCode(); }
  @Override
  public int getTileSizePixels() { return Tile.TILE_SIZE; }
  @Override
  public int getMaximumZoomLevel() { return mapGenerator_.getZoomLevelMax(); }
  @Override
  public int getMinimumZoomLevel() { return 6; }

  
  @Override
  public Drawable getDrawable(String arg0) throws LowMemoryException
  {
    return null;
  } // getDrawable

  @Override
  public Drawable getDrawable(InputStream arg0) throws LowMemoryException
  {
    return null;
  } // getDrawable

  @Override
  public String getTileRelativeFilenameString(final MapTile tile)
  {
    return null;
  } // getTileRelativeFilenameString

} // MapsforgeOSMTileSource
