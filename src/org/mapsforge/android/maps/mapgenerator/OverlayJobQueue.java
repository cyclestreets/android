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
package org.mapsforge.android.maps.mapgenerator;

import java.util.PriorityQueue;

import org.mapsforge.android.maps.MapsforgeTilesOverlay;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.core.MercatorProjection;
import org.mapsforge.core.Tile;
import org.mapsforge.core.GeoPoint;

/**
 * A JobQueue keeps the list of pending jobs for a MapView and prioritizes them.
 */
public class OverlayJobQueue 
{
  private static final int INITIAL_CAPACITY = 128;

  private PriorityQueue<MapGeneratorJob> priorityQueue;
  private MapsforgeTilesOverlay overlay;
  private boolean scheduleNeeded;

  public OverlayJobQueue(MapsforgeTilesOverlay overlay) {
    this.overlay = overlay;
    this.priorityQueue = new PriorityQueue<MapGeneratorJob>(INITIAL_CAPACITY);
  }

  /**
   * Adds the given job to this queue. Does nothing if the given job is already in this queue.
   * 
   * @param mapGeneratorJob
   *            the job to be added to this queue.
   */
  public synchronized void addJob(MapGeneratorJob mapGeneratorJob) {
    if (!this.priorityQueue.contains(mapGeneratorJob)) {
      this.priorityQueue.offer(mapGeneratorJob);
    }
  }

  /**
   * Removes all jobs from this queue.
   */
  public synchronized void clear() {
    this.priorityQueue.clear();
  }

  /**
   * @return true if this queue contains no jobs, false otherwise.
   */
  public synchronized boolean isEmpty() {
    return this.priorityQueue.isEmpty();
  }

  /**
   * @return the most important job from this queue or null, if empty.
   */
  public synchronized MapGeneratorJob poll() {
    if (this.scheduleNeeded) {
      this.scheduleNeeded = false;
      schedule();
    }
    return this.priorityQueue.poll();
  }

  /**
   * Request a scheduling of all jobs that are currently in this queue.
   */
  public synchronized void requestSchedule() {
    this.scheduleNeeded = true;
  }

  /**
   * Schedules all jobs in this queue.
   */
  private void schedule() {
    PriorityQueue<MapGeneratorJob> tempJobQueue = new PriorityQueue<MapGeneratorJob>(INITIAL_CAPACITY);
    
    while (!this.priorityQueue.isEmpty()) {
      MapGeneratorJob mapGeneratorJob = this.priorityQueue.poll();
      double priority = getPriority(mapGeneratorJob.tile);
      mapGeneratorJob.setPriority(priority);
      tempJobQueue.offer(mapGeneratorJob);
    }

    this.priorityQueue = tempJobQueue;
  }
	
  private static final int ZOOM_LEVEL_PENALTY = 5;
  
  /**    
   * Calculates the priority for the given tile based on the current position and zoom level of the supplied MapView.
   * The smaller the distance from the tile center to the MapView center, the higher its priority. If the zoom level
   * of a tile differs from the zoom level of the MapView, its priority decreases.
   *
   * @param tile
   *            the tile whose priority should be calculated.
   * @param mapView
   *            the MapView whose current position and zoom level define the priority of the tile.
   * @return the current priority of the tile. A smaller number means a higher priority.
   */
  double getPriority(Tile tile) {
    byte tileZoomLevel = tile.zoomLevel;
    
    // calculate the center coordinates of the tile
    long tileCenterPixelX = tile.getPixelX() + (Tile.TILE_SIZE >> 1);
    long tileCenterPixelY = tile.getPixelY() + (Tile.TILE_SIZE >> 1);
    double tileCenterLongitude = MercatorProjection.pixelXToLongitude(tileCenterPixelX, tileZoomLevel);
    double tileCenterLatitude = MercatorProjection.pixelYToLatitude(tileCenterPixelY, tileZoomLevel);
    
    // calculate the Euclidian distance from the MapView center to the tile center
    GeoPoint geoPoint = overlay.getCentre();
    double longitudeDiff = geoPoint.getLongitude() - tileCenterLongitude;
    double latitudeDiff = geoPoint.getLatitude() - tileCenterLatitude;
    double euclidianDistance = Math.sqrt(longitudeDiff * longitudeDiff + latitudeDiff * latitudeDiff);
    
    if (overlay.zoomLevel() == tileZoomLevel) {
      return euclidianDistance;
    }
    
    int zoomLevelDiff = Math.abs(overlay.zoomLevel() - tileZoomLevel);
    double scaleFactor = Math.pow(2, zoomLevelDiff);
    
    double scaledEuclidianDistance;
    if (overlay.zoomLevel() < tileZoomLevel) {
      scaledEuclidianDistance = euclidianDistance * scaleFactor;
    } else {
      scaledEuclidianDistance = euclidianDistance / scaleFactor;
    }
    
    double zoomLevelPenalty = zoomLevelDiff * ZOOM_LEVEL_PENALTY;
    return scaledEuclidianDistance * zoomLevelPenalty;
  }
}
