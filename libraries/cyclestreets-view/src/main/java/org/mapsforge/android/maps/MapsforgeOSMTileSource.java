package org.mapsforge.android.maps;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.mapsforge.android.maps.mapgenerator.JobParameters;
import org.mapsforge.android.maps.mapgenerator.JobTheme;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.core.BoundingBox;
import org.mapsforge.core.Tile;
import org.mapsforge.map.reader.MapDatabase;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.io.File;
import java.io.InputStream;

public class MapsforgeOSMTileSource implements ITileSource {
  @SuppressWarnings("serial")
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

  private static final float DEFAULT_TEXT_SCALE = 1;
  private final String name_;
  private final DatabaseRenderer mapGenerator_;
  private final MapDatabase mapDatabase_;
  private final JobParameters jobParameters_;
  private final DebugSettings debugSettings_;
  private String mapFile_;
  private BoundingBox mapBounds_;
  private int zoomBounds_;
  private int westTileBounds_;
  private int eastTileBounds_;
  private int southTileBounds_;
  private int northTileBounds_;
  private int tileSize_;
  
  public MapsforgeOSMTileSource(final String name,
                                final boolean upSize) {
    name_ = name;
    mapGenerator_ = new DatabaseRenderer();
    mapDatabase_ = new MapDatabase();
    mapGenerator_.setMapDatabase(mapDatabase_);
        
    jobParameters_ = new JobParameters(new RenderTheme(), DEFAULT_TEXT_SCALE);
    debugSettings_ = new DebugSettings(false, false, false);

    tileSize_ = upSize ? 512 : 256;
  } // MapsforgeOSMTileSource
  
  public void setMapFile(final String mapFile) {
    if((mapFile == null) || (mapFile.equals(mapFile_)))
        return;
    
    mapFile_ = mapFile;
    mapDatabase_.closeFile();
    mapDatabase_.openFile(new File(mapFile));
    mapBounds_ = mapDatabase_.getMapFileInfo().boundingBox;
    zoomBounds_ = -1;
  } // setMapFile

  @Override
  public String name() { return name_; }
  @Override
  public int ordinal() { return name_.hashCode(); }
  @Override
  public int getTileSizePixels() { return tileSize_; }
  @Override
  public int getMaximumZoomLevel() { return mapGenerator_.getZoomLevelMax(); }
  @Override
  public int getMinimumZoomLevel() { return 6; }

  public synchronized Drawable getDrawable(int tileX, int tileY, int zoom) throws LowMemoryException {
	  if(tileOutOfBounds(tileX, tileY, zoom))
		  return null;
	  
    final Tile tile = new Tile(tileX, tileY, (byte)zoom);
    MapGeneratorJob mapGeneratorJob = new MapGeneratorJob(tile, 
                                                          "ooot",                                                           
                                                          jobParameters_,
                                                          debugSettings_);

    Bitmap tileBitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, Bitmap.Config.RGB_565);
    boolean success = mapGenerator_.executeJob(mapGeneratorJob, tileBitmap);

    if (tileSize_ != Tile.TILE_SIZE)
      tileBitmap = Bitmap.createScaledBitmap(tileBitmap, tileSize_, tileSize_, false);

    return success ? new ExpirableBitmapDrawable(tileBitmap) : null;
  } // getDrawable
  
  private boolean tileOutOfBounds(int tileX, int tileY, int zoom) {
    if(zoom != zoomBounds_)
      recalculateTileBounds(zoom);
			
    final boolean oob = (tileX < westTileBounds_) || (tileX > eastTileBounds_) ||
    	                  (tileY < northTileBounds_) || (tileY > southTileBounds_);
    return oob;
  } // tileOutOfBounds
  
  /* convert lon/lat to tile x,y from http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames */
  private void recalculateTileBounds(final int zoom) {
    zoomBounds_ = zoom;
    westTileBounds_ = lon2XTile(mapBounds_.getMinLongitude(), zoomBounds_);
    eastTileBounds_ = lon2XTile(mapBounds_.getMaxLongitude(), zoomBounds_);
    southTileBounds_ = lat2YTile(mapBounds_.getMinLatitude(), zoomBounds_);
    northTileBounds_ = lat2YTile(mapBounds_.getMaxLatitude(), zoomBounds_);
  } // recalculateTileBounds
  
  @Override
  public Drawable getDrawable(String arg0) throws LowMemoryException { return null; }
  @Override
  public Drawable getDrawable(InputStream arg0) throws LowMemoryException { return null; }
  @Override
  public String getTileRelativeFilenameString(final MapTile tile) { return null; }
  
  private static int lon2XTile(final double lon, final int zoom) {
    return (int)Math.floor((lon + 180) / 360 * (1<<zoom)) ;
  } // lon2XTile
  
  private static int lat2YTile(final double lat, final int zoom) {
    return (int)Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom)) ;
  } // lat2YTile
} // MapsforgeOSMTileSource
