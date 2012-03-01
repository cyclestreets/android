/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.android.maps;

import org.mapsforge.android.maps.PausableThread;
import org.mapsforge.android.maps.mapgenerator.MapGenerator;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.OverlayJobQueue;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.core.Tile;

import android.graphics.Bitmap;

/**
 * A MapWorker uses a {@link MapGenerator} to generate map tiles. 
 * It runs in a separate thread to avoid blocking the UI
 * thread.
 */
public class OverlayMapWorker extends PausableThread 
{
  private static final String THREAD_NAME = "MapWorker";
  
  private final TileCache fileSystemTileCache;
  private final TileCache inMemoryTileCache;
  private final OverlayJobQueue jobQueue;
  private MapGenerator mapGenerator;
  private final MapsforgeTilesOverlay mapView;
  private final Bitmap tileBitmap;
  
  /**
   * @param mapView
   *            the MapView for which this MapWorker generates map tiles.
   */
  public OverlayMapWorker(final MapsforgeTilesOverlay overlay) 
  {
    super();
    this.mapView = overlay;
    this.jobQueue = overlay.getJobQueue();
    this.inMemoryTileCache = overlay.getInMemoryTileCache();
    this.fileSystemTileCache = overlay.getFileSystemTileCache();
    this.tileBitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, Bitmap.Config.RGB_565);
  }

  /**
   * @param mapGenerator
   *            the MapGenerator which this MapWorker should use.
   */
  public void setMapGenerator(MapGenerator mapGenerator) {
    this.mapGenerator = mapGenerator;
  }

  @Override
  protected void afterRun() {
    this.tileBitmap.recycle();
  }

  @Override
  protected void doWork() {
    MapGeneratorJob mapGeneratorJob = this.jobQueue.poll();
    
    if (this.inMemoryTileCache.containsKey(mapGeneratorJob)) {
      return;
    } else if (this.fileSystemTileCache.containsKey(mapGeneratorJob)) {
      return;
    }

    boolean success = this.mapGenerator.executeJob(mapGeneratorJob, this.tileBitmap);

    if (!isInterrupted() && success) {
      if (this.mapView.getFrameBuffer().drawBitmap(mapGeneratorJob.tile, this.tileBitmap, mapView.getCentre(), mapView.zoomLevel())) {
      	this.inMemoryTileCache.put(mapGeneratorJob, this.tileBitmap);
      }
      this.mapView.postInvalidate();
      this.fileSystemTileCache.put(mapGeneratorJob, this.tileBitmap);
    }
  }

  @Override
  protected String getThreadName() {
    return THREAD_NAME;
  }

  @Override
  protected int getThreadPriority() {
    return (Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2;
  }

  @Override
  protected boolean hasWork() {
    return !this.jobQueue.isEmpty();
  }
}
