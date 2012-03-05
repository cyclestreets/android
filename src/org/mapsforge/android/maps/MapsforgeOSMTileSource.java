package org.mapsforge.android.maps;

import java.io.InputStream;

import org.mapsforge.android.maps.mapgenerator.JobParameters;
import org.mapsforge.android.maps.mapgenerator.JobTheme;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.core.Tile;
import org.mapsforge.map.reader.MapDatabase;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public class MapsforgeOSMTileSource implements ITileSource
{
  @SuppressWarnings("serial")
  static private class RenderTheme implements JobTheme 
  {
    static private final String path = "/org/mapsforge/android/maps/rendertheme/osmarender/osmarender.xml";
    
    @Override
    public InputStream getRenderThemeAsStream() {
      return getClass().getResourceAsStream(path);
    }
  }          

  private static final float DEFAULT_TEXT_SCALE = 1;

  private final String name_;
  private final DatabaseRenderer mapGenerator_;
  private final MapDatabase mapDatabase_;
  private final JobParameters jobParameters_;
  private final DebugSettings debugSettings_;
  private String mapFile_;
  
  public MapsforgeOSMTileSource(final String name)
  {
    name_ = name;
    mapGenerator_ = new DatabaseRenderer();
    mapDatabase_ = new MapDatabase();
    mapGenerator_.setMapDatabase(mapDatabase_);
    
    jobParameters_ = new JobParameters(new RenderTheme(), DEFAULT_TEXT_SCALE);
    debugSettings_ = new DebugSettings(false, false, false);
  } // MapsforgeOSMTileSource
  
  public void setMapFile(final String mapFile)
  {
    if((mapFile == null) || (mapFile.equals(mapFile_)))
        return;
    
    mapFile_ = mapFile;
    mapDatabase_.closeFile();
    mapDatabase_.openFile(mapFile_);
  } // setMapFile

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

  public synchronized Drawable getDrawable(int tileX, int tileY, int zoom) throws LowMemoryException
  {
    final Tile tile = new Tile(tileX, tileY, (byte)zoom);
    MapGeneratorJob mapGeneratorJob = new MapGeneratorJob(tile, 
                                                          "ooot",                                                           
                                                          jobParameters_,
                                                          debugSettings_);

    final Bitmap tileBitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, Bitmap.Config.RGB_565);
    boolean success = mapGenerator_.executeJob(mapGeneratorJob, tileBitmap);
    return success ?  new ExpirableBitmapDrawable(tileBitmap) : null;
  } // getDrawable
  
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
