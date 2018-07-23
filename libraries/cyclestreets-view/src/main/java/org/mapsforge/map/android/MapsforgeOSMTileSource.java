package org.mapsforge.map.android;

import android.graphics.drawable.Drawable;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.graphics.AndroidTileBitmap;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.layer.renderer.DatabaseRenderer;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapFile;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.io.File;
import java.io.InputStream;

import static org.mapsforge.map.android.graphics.AndroidBitmapExpose.expose;

public class MapsforgeOSMTileSource implements ITileSource {
  @SuppressWarnings("serial")
  /*
  private static class RenderTheme implements JobTheme {
    //private static final String path = "/org/mapsforge/android/maps/rendertheme/osmarender/";
    private static final String path="/assets/rendertheme/";
    private static final String file = "osmarender.xml";

    //@Override
    //public String getRelativePathPrefix() {
    //  return path;
    //}

    @Override
    public InputStream getRenderThemeAsStream() {
      final InputStream is = getClass().getResourceAsStream(path+file);
      return is;
    }
  }
  */

  private static final float DEFAULT_TEXT_SCALE = 1;
  private final String name_;
  private final String attribution_;
  private DatabaseRenderer mapGenerator_;
  private MapDataStore mapDatabase_;
  private BoundingBox mapBounds_;
  private String mapFile_;
  private int zoomBounds_;
  private int westTileBounds_;
  private int eastTileBounds_;
  private int southTileBounds_;
  private int northTileBounds_;
  private int tileSize_;

  public MapsforgeOSMTileSource(final String name,
                                final String attribution,
                                final boolean upSize) {
    name_ = name;
    attribution_ = attribution;
    tileSize_ = upSize ? 512 : 256;
  }

  public void setMapFile(final String mapFile) {
    if ((mapFile == null) || (mapFile.equals(mapFile_)))
        return;

    mapFile_ = mapFile;
    if (mapDatabase_ != null)
      mapDatabase_.close();

    mapDatabase_ = new MapFile(new File(mapFile));
    mapGenerator_ = new DatabaseRenderer(
            mapDatabase_,
            AndroidGraphicFactory.INSTANCE,
            null,
            null,
            true,
            false,
            null
    );
    mapBounds_ = mapDatabase_.boundingBox();
    zoomBounds_ = -1;
  }

  @Override
  public String name() { return name_; }
  @Override
  @Deprecated
  public int ordinal() { return name_.hashCode(); }
  @Override
  public int getTileSizePixels() { return tileSize_; }
  @Override
  public int getMaximumZoomLevel() { return mapGenerator_.getZoomLevelMax(); }
  @Override
  public int getMinimumZoomLevel() { return 6; }

  @Override
  public String getCopyrightNotice() { return attribution_; }

  public synchronized Drawable getDrawable(int tileX, int tileY, int zoom) throws LowMemoryException {
    if (tileOutOfBounds(tileX, tileY, zoom))
      return null;

    final Tile tile = new Tile(tileX, tileY, (byte)zoom, tileSize_);
    final RendererJob mapGeneratorJob = createJob(tile);
    final AndroidTileBitmap tileBitmap = (AndroidTileBitmap)mapGenerator_.executeJob(mapGeneratorJob);
    if (tileBitmap == null)
      return null;

    tileBitmap.scaleTo(tileSize_, tileSize_);
    return new ExpirableBitmapDrawable(expose(tileBitmap));
  } // getDrawable

  private RendererJob createJob(Tile tile) {
    return new RendererJob(
            tile,
            mapDatabase_,
            null,
            new DisplayModel(),
            1,
            false,
            false
    );
  }

  private boolean tileOutOfBounds(int tileX, int tileY, int zoom) {
    if (zoom != zoomBounds_)
      recalculateTileBounds(zoom);

    final boolean oob = (tileX < westTileBounds_) || (tileX > eastTileBounds_) ||
                        (tileY < northTileBounds_) || (tileY > southTileBounds_);
    return oob;
  }

  /* convert lon/lat to tile x,y from http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames */
  private void recalculateTileBounds(final int zoom) {
    zoomBounds_ = zoom;
    westTileBounds_ = lon2XTile(mapBounds_.minLongitude, zoomBounds_);
    eastTileBounds_ = lon2XTile(mapBounds_.maxLongitude, zoomBounds_);
    southTileBounds_ = lat2YTile(mapBounds_.minLatitude, zoomBounds_);
    northTileBounds_ = lat2YTile(mapBounds_.maxLatitude, zoomBounds_);
  }

  @Override
  public Drawable getDrawable(String arg0) throws LowMemoryException { return null; }
  @Override
  public Drawable getDrawable(InputStream arg0) throws LowMemoryException { return null; }
  @Override
  public String getTileRelativeFilenameString(final MapTile tile) { return null; }

  private static int lon2XTile(final double lon, final int zoom) {
    return (int)Math.floor((lon + 180) / 360 * (1<<zoom)) ;
  }

  private static int lat2YTile(final double lat, final int zoom) {
    return (int)Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom)) ;
  }
}
