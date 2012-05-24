package org.mapsforge.android.maps;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.graphics.drawable.Drawable;

public class MapsforgeOSMDroidTileProvider extends MapTileModuleProviderBase
{
  private MapsforgeOSMTileSource tileSource_;
  
  public MapsforgeOSMDroidTileProvider()
  {
    super(NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
    tileSource_ = null;
  } // MapsforgeOSMDroidTileProvider
  
  @Override
  protected String getName() { return "Mapsforge"; } 

  @Override
  protected String getThreadGroupName() { return "mapsforge"; }

  @Override
  public boolean getUsesDataConnection() { return false; }

  @Override
  public int getMaximumZoomLevel()
  {
    return (tileSource_ != null ? tileSource_.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL);
  } // getMaximumZoomLevel

  @Override
  public int getMinimumZoomLevel()
  {
    return (tileSource_ != null ? tileSource_.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
  } // getMinimumZoomLevel

  @Override
  protected Runnable getTileLoader()
  {
    return new TileLoader();
  } // getTileLoader

  @Override
  public void setTileSource(final ITileSource tileSource)
  {
    tileSource_ = (tileSource instanceof MapsforgeOSMTileSource) ?
                    (MapsforgeOSMTileSource)tileSource :
                    null;
  } // setTileSource
  
  /////////////////////////////////////////////////
  private class TileLoader extends MapTileModuleProviderBase.TileLoader 
  {
    @Override
    public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException 
    {
      if(tileSource_ == null) 
        return null;

      try {
        final MapTile tile = aState.getMapTile();
        return tileSource_.getDrawable(tile.getX(), tile.getY(), tile.getZoomLevel());
      }
      catch(LowMemoryException e) {
        return null;
      }
    } // loadFile
  } // TileLoader
} // MapsForgeOSMDroidTileProvider
