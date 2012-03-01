package org.mapsforge.android.maps;

import java.io.FileNotFoundException;

import net.cyclestreets.views.CycleMapView;

import org.mapsforge.android.AndroidUtils;
import org.mapsforge.android.maps.DebugSettings;
import org.mapsforge.android.maps.FpsCounter;
import org.mapsforge.android.maps.OverlayFrameBuffer;
import org.mapsforge.android.maps.mapgenerator.FileSystemTileCache;
import org.mapsforge.android.maps.mapgenerator.InMemoryTileCache;
import org.mapsforge.android.maps.mapgenerator.JobParameters;
import org.mapsforge.android.maps.mapgenerator.JobTheme;
import org.mapsforge.android.maps.mapgenerator.MapGenerator;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.OverlayJobQueue;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.android.maps.mapgenerator.tiledownloader.TileDownloader;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.ExternalRenderTheme;
import org.mapsforge.android.maps.rendertheme.InternalRenderTheme;
import org.mapsforge.core.GeoPoint;
import org.mapsforge.core.MercatorProjection;
import org.mapsforge.core.Tile;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;

public class MapsforgeTilesOverlay extends TilesOverlay
{
  public MapsforgeTilesOverlay(final Context context, final CycleMapView mapView)
  {
    super(context);
    
    mapView_ = mapView;
    
    setLoadingBackgroundColor(FrameBuffer.MAP_VIEW_BACKGROUND);

    this.debugSettings = new DebugSettings(false, false, false);
    this.fileSystemTileCache = new FileSystemTileCache(DEFAULT_TILE_CACHE_SIZE_FILE_SYSTEM, 0);
    this.fpsCounter = new FpsCounter();
    this.frameBuffer = new OverlayFrameBuffer();
    this.inMemoryTileCache = new InMemoryTileCache(DEFAULT_TILE_CACHE_SIZE_IN_MEMORY);
    this.jobParameters = new JobParameters(DEFAULT_RENDER_THEME, DEFAULT_TEXT_SCALE);
    this.jobQueue = new OverlayJobQueue();
    this.mapDatabase = new MapDatabase();
    this.mapWorker = new OverlayMapWorker(this);
    this.mapWorker.start();
    this.mapGenerator = new DatabaseRenderer();

    setMapGeneratorInternal(mapGenerator);

    setMapFile(Environment.getExternalStorageDirectory() + "/download/great_britain-0.3.0.map");
  } // MapsforgeTilesOverlay
  
  @Override
  public int getLoadingBackgroundColor()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getMaximumZoomLevel()
  {
    return this.mapGenerator.getZoomLevelMax();
  }

  @Override
  public int getMinimumZoomLevel()
  {
    return 4;
  }

  @Override
  public void setLoadingBackgroundColor(int arg0)
  {
  }

  @Override
  public void setUseDataConnection(boolean arg0)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean useDataConnection()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void draw(final Canvas canvas, IMapView mapView, boolean shadow)
  {
    if(shadow)
      return;

    this.frameBuffer.draw(canvas);
    if(!getCentre().equals(lastCentre_))
    {
      lastCentre_ = getCentre();
      clearAndRedrawMapView();
    }
  }
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////
    public static final InternalRenderTheme DEFAULT_RENDER_THEME = InternalRenderTheme.OSMARENDER;

    private static final float DEFAULT_TEXT_SCALE = 1;
    private static final int DEFAULT_TILE_CACHE_SIZE_FILE_SYSTEM = 100;
    private static final int DEFAULT_TILE_CACHE_SIZE_IN_MEMORY = 20;

    private CycleMapView mapView_;
    private DebugSettings debugSettings;
    private final TileCache fileSystemTileCache;
    private final FpsCounter fpsCounter;
    private final OverlayFrameBuffer frameBuffer;
    private final TileCache inMemoryTileCache;
    private JobParameters jobParameters;
    private final OverlayJobQueue jobQueue;
    private final MapDatabase mapDatabase;
    private String mapFile;
    private MapGenerator mapGenerator;
    private final OverlayMapWorker mapWorker;
    private GeoPoint lastCentre_;

    /**
     * @return the debug settings which are used in this MapView.
     */
    public DebugSettings getDebugSettings() {
            return this.debugSettings;
    }

    /**
     * @return the file system tile cache which is used in this MapView.
     */
    public TileCache getFileSystemTileCache() {
            return this.fileSystemTileCache;
    }

    /**
     * @return the FPS counter which is used in this MapView.
     */
    public FpsCounter getFpsCounter() {
            return this.fpsCounter;
    }

    /**
     * @return the FrameBuffer which is used in this MapView.
     */
    public OverlayFrameBuffer getFrameBuffer() {
            return this.frameBuffer;
    }

    /**
     * @return the in-memory tile cache which is used in this MapView.
     */
    public TileCache getInMemoryTileCache() {
            return this.inMemoryTileCache;
    }

    /**
     * @return the job queue which is used in this MapView.
     */
    public OverlayJobQueue getJobQueue() {
            return this.jobQueue;
    }

    /**
     * @return the map database which is used for reading map files.
     * @throws UnsupportedOperationException
     *             if the current MapGenerator works with an Internet connection.
     */
    public MapDatabase getMapDatabase() {
            if (this.mapGenerator.requiresInternetConnection()) {
                    throw new UnsupportedOperationException();
            }
            return this.mapDatabase;
    }

    /**
     * @return the currently used map file.
     * @throws UnsupportedOperationException
     *             if the current MapGenerator mode works with an Internet connection.
     */
    public String getMapFile() {
            if (this.mapGenerator.requiresInternetConnection()) {
                    throw new UnsupportedOperationException();
            }
            return this.mapFile;
    }

    /**
     * @return the currently used MapGenerator (may be null).
     */
    public MapGenerator getMapGenerator() {
            return this.mapGenerator;
    }

    /**
     * Calls either {@link #invalidate()} or {@link #postInvalidate()}, depending on the current thread.
     */
    public void invalidateOnUiThread() {
      if (AndroidUtils.currentThreadIsUiThread()) {
        invalidate();
      } else {
        postInvalidate();
      }
    }

    protected GeoPoint getCentre()
    {
      final IGeoPoint centre = mapView_.getMapCenter();
      final GeoPoint topLeft = new GeoPoint(centre.getLatitude(), centre.getLongitude());
      return topLeft;
    } // getTopLeft
    
    protected byte zoomLevel()
    {
      return (byte)mapView_.getZoomLevel();
    } // zoomLevel
    
    /**
     * Calculates all necessary tiles and adds jobs accordingly.
     */
    public void redrawTiles() 
    {
      final GeoPoint centre = getCentre();
      final int width = mapView_.getWidth();
      final int height = mapView_.getHeight();
      
      if (width <= 0 || height <= 0) 
      {
        return;
      }

      double pixelLeft = MercatorProjection.longitudeToPixelX(centre.getLongitude(), zoomLevel());
      double pixelTop = MercatorProjection.latitudeToPixelY(centre.getLatitude(), zoomLevel());
      pixelLeft -= width >> 1;
      pixelTop -= height >> 1;

      long tileLeft = MercatorProjection.pixelXToTileX(pixelLeft, zoomLevel());
      long tileTop = MercatorProjection.pixelYToTileY(pixelTop, zoomLevel());
      long tileRight = MercatorProjection.pixelXToTileX(pixelLeft + width, zoomLevel());
      long tileBottom = MercatorProjection.pixelYToTileY(pixelTop + height, zoomLevel());
      
      String cacheId;
      if (this.mapGenerator.requiresInternetConnection()) {
        cacheId = ((TileDownloader) this.mapGenerator).getHostName();
      } else {
        cacheId = this.mapFile;
      }
      
      for (long tileY = tileTop; tileY <= tileBottom; ++tileY) {
        for (long tileX = tileLeft; tileX <= tileRight; ++tileX) {
          Tile tile = new Tile(tileX, tileY, zoomLevel());
          MapGeneratorJob mapGeneratorJob = new MapGeneratorJob(tile, 
                                                                cacheId, 
                                                                this.jobParameters,
                                                                this.debugSettings);
          
          if (this.inMemoryTileCache.containsKey(mapGeneratorJob)) {
            Bitmap bitmap = this.inMemoryTileCache.get(mapGeneratorJob);
            this.frameBuffer.drawBitmap(mapGeneratorJob.tile, bitmap, centre, zoomLevel());
          } else if (this.fileSystemTileCache.containsKey(mapGeneratorJob)) {
            Bitmap bitmap = this.fileSystemTileCache.get(mapGeneratorJob);
            if (bitmap != null) {
              this.frameBuffer.drawBitmap(mapGeneratorJob.tile, bitmap, centre, zoomLevel());
              this.inMemoryTileCache.put(mapGeneratorJob, bitmap);
            } else {
              // the image data could not be read from the cache
              this.jobQueue.addJob(mapGeneratorJob);
            }
          } else {
            // cache miss
            this.jobQueue.addJob(mapGeneratorJob);
          }
        }
      }
      
      invalidateOnUiThread();
      
      this.jobQueue.requestSchedule();
      synchronized (this.mapWorker) {
        this.mapWorker.notify();
      }
    }   

    /**
     * @param debugSettings
     *            the new DebugSettings for this MapView.
     */
    public void setDebugSettings(DebugSettings debugSettings) {
            this.debugSettings = debugSettings;
            clearAndRedrawMapView();
    }

    /**
     * Sets the map file for this MapView.
     * 
     * @param mapFile
     *            the path to the map file.
     * @return a FileOpenResult to describe whether the operation returned successfully.
     * @throws UnsupportedOperationException
     *             if the current MapGenerator mode works with an Internet connection.
     * @throws IllegalArgumentException
     *             if the supplied mapFile is null.
     */
    public FileOpenResult setMapFile(String mapFile) {
            if (this.mapGenerator.requiresInternetConnection()) {
                    throw new UnsupportedOperationException();
            }
            if (mapFile == null) {
                    throw new IllegalArgumentException("mapFile must not be null");
            } else if (mapFile.equals(this.mapFile)) {
                    // same map file as before
                    return FileOpenResult.SUCCESS;
            }

            this.mapWorker.pause();
            this.mapWorker.awaitPausing();

            this.jobQueue.clear();

            this.mapWorker.proceed();

            this.mapDatabase.closeFile();
            FileOpenResult fileOpenResult = this.mapDatabase.openFile(mapFile);
            if (fileOpenResult.isSuccess()) {
                    this.mapFile = mapFile;
                    clearAndRedrawMapView();
                    return FileOpenResult.SUCCESS;
            }
            this.mapFile = null;
            clearAndRedrawMapView();
            return fileOpenResult;
    }

    /**
     * Sets the MapGenerator for this MapView.
     * 
     * @param mapGenerator
     *            the new MapGenerator.
     */
    public void setMapGenerator(MapGenerator mapGenerator) {
            if (this.mapGenerator != mapGenerator) {
                    setMapGeneratorInternal(mapGenerator);
                    clearAndRedrawMapView();
            }
    }

    /**
     * Sets the internal theme which is used for rendering the map.
     * 
     * @param internalRenderTheme
     *            the internal rendering theme.
     * @throws IllegalArgumentException
     *             if the supplied internalRenderTheme is null.
     * @throws UnsupportedOperationException
     *             if the current MapGenerator does not support render themes.
     */
    public void setRenderTheme(InternalRenderTheme internalRenderTheme) {
            if (internalRenderTheme == null) {
                    throw new IllegalArgumentException("render theme must not be null");
            } else if (this.mapGenerator.requiresInternetConnection()) {
                    throw new UnsupportedOperationException();
            }

            this.jobParameters = new JobParameters(internalRenderTheme, this.jobParameters.textScale);
            clearAndRedrawMapView();
    }

    /**
     * Sets the theme file which is used for rendering the map.
     * 
     * @param renderThemePath
     *            the path to the XML file which defines the rendering theme.
     * @throws IllegalArgumentException
     *             if the supplied internalRenderTheme is null.
     * @throws UnsupportedOperationException
     *             if the current MapGenerator does not support render themes.
     * @throws FileNotFoundException
     *             if the supplied file does not exist, is a directory or cannot be read.
     */
    public void setRenderTheme(String renderThemePath) throws FileNotFoundException {
            if (renderThemePath == null) {
                    throw new IllegalArgumentException("render theme path must not be null");
            } else if (this.mapGenerator.requiresInternetConnection()) {
                    throw new UnsupportedOperationException();
            }

            JobTheme jobTheme = new ExternalRenderTheme(renderThemePath);
            this.jobParameters = new JobParameters(jobTheme, this.jobParameters.textScale);
            clearAndRedrawMapView();
    }

    /**
     * Sets the text scale for the map rendering. Has no effect in downloading mode.
     * 
     * @param textScale
     *            the new text scale for the map rendering.
     */
    public void setTextScale(float textScale) {
            this.jobParameters = new JobParameters(this.jobParameters.jobTheme, textScale);
            clearAndRedrawMapView();
    }

    private void setMapGeneratorInternal(MapGenerator mapGenerator) {
            if (mapGenerator == null) {
                    throw new IllegalArgumentException("mapGenerator must not be null");
            }

            if (mapGenerator instanceof DatabaseRenderer) {
                    ((DatabaseRenderer) mapGenerator).setMapDatabase(this.mapDatabase);
            }
            this.mapGenerator = mapGenerator;
            this.mapWorker.setMapGenerator(this.mapGenerator);
    }

    public synchronized void onSizeChanged(int width, int height) {
            this.frameBuffer.destroy();

            if (width > 0 && height > 0) {
                    this.frameBuffer.onSizeChanged(width, height);
                    clearAndRedrawMapView();
            }
    }

    public void clearAndRedrawMapView() {
            this.jobQueue.clear();
            this.frameBuffer.clear();
            redrawTiles();
    }

    void destroy() {
            this.mapWorker.interrupt();

            try {
                    this.mapWorker.join();
            } catch (InterruptedException e) {
                    // restore the interrupted status
                    Thread.currentThread().interrupt();
            }

            this.frameBuffer.destroy();
            this.inMemoryTileCache.destroy();
            this.fileSystemTileCache.destroy();

            this.mapDatabase.closeFile();
    }

    public void onPause() {
            this.mapWorker.pause();
    }

    public void onResume() {
            this.mapWorker.proceed();
    }

    protected void invalidate()
    {
      mapView_.invalidate();
    } // invalidate
    
    protected void postInvalidate()
    {
      mapView_.postInvalidate();
    } // postInvalidate
} // class MapsforgeTilesOverlay
