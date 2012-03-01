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

import net.cyclestreets.util.Brush;

import org.mapsforge.core.GeoPoint;
import org.mapsforge.core.MercatorProjection;
import org.mapsforge.core.Tile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;

/**
 * A FrameBuffer uses two separate memory buffers to display the current and build up the next frame.
 */
public class OverlayFrameBuffer {
	static final int MAP_VIEW_BACKGROUND = Color.rgb(238, 238, 238);

	private int height;
	private Bitmap mapViewBitmap1;
	private Bitmap mapViewBitmap2;
	private final Canvas mapViewCanvas;
	private final Matrix matrix;
	private int width;

	OverlayFrameBuffer() {
		this.mapViewCanvas = new Canvas();
		this.matrix = new Matrix();
	}

	/**
	 * Draws a tile bitmap at the right position on the MapView bitmap.
	 * 
	 * @param tile
	 *            the corresponding tile for the bitmap.
	 * @param bitmap
	 *            the bitmap to be drawn.
	 * @return true if the tile is visible and the bitmap was drawn, false otherwise.
	 */
	public synchronized boolean drawBitmap(Tile tile, Bitmap bitmap, final GeoPoint topLeft, byte zoomLevel) 
	{
		if (tile.zoomLevel != zoomLevel) {
			// the tile doesn't fit to the current zoom level
			return false;
		} 

		double pixelLeft = MercatorProjection.longitudeToPixelX(topLeft.getLongitude(), zoomLevel);
		double pixelTop = MercatorProjection.latitudeToPixelY(topLeft.getLatitude(), zoomLevel);
		pixelLeft -= this.width >> 1;
		pixelTop -= this.height >> 1;

		if (pixelLeft - tile.getPixelX() > Tile.TILE_SIZE || pixelLeft + this.width < tile.getPixelX()) {
			// no horizontal intersection
			return false;
		} else if (pixelTop - tile.getPixelY() > Tile.TILE_SIZE || pixelTop + this.height < tile.getPixelY()) {
			// no vertical intersection
			return false;
		}

		if (!this.matrix.isIdentity()) {
			// change the current MapView bitmap
			this.mapViewBitmap2.eraseColor(MAP_VIEW_BACKGROUND);
			this.mapViewCanvas.setBitmap(this.mapViewBitmap2);

			// draw the previous MapView bitmap on the current MapView bitmap
			this.mapViewCanvas.drawBitmap(this.mapViewBitmap1, this.matrix, null);
			this.matrix.reset();

			// swap the two MapView bitmaps
			Bitmap mapViewBitmapSwap = this.mapViewBitmap1;
			this.mapViewBitmap1 = this.mapViewBitmap2;
			this.mapViewBitmap2 = mapViewBitmapSwap;
		}

		// draw the tile bitmap at the correct position
		float left = (float) (tile.getPixelX() - pixelLeft);
		float top = (float) (tile.getPixelY() - pixelTop);
		this.mapViewCanvas.drawBitmap(bitmap, left, top, null);
		return true;
	}

	/**
	 * Scales the matrix of the MapView and all its overlays.
	 * 
	 * @param scaleX
	 *            the horizontal scale.
	 * @param scaleY
	 *            the vertical scale.
	 * @param pivotX
	 *            the horizontal pivot point.
	 * @param pivotY
	 *            the vertical pivot point.
	 */
	public void matrixPostScale(float scaleX, float scaleY, float pivotX, float pivotY) {
	  this.matrix.postScale(scaleX, scaleY, pivotX, pivotY);
	}

	/**
	 * Translates the matrix of the MapView and all its overlays.
	 * 
	 * @param translateX
	 *            the horizontal translation.
	 * @param translateY
	 *            the vertical translation.
	 */
	public void matrixPostTranslate(float translateX, float translateY) {
	  this.matrix.postTranslate(translateX, translateY);
	}

	synchronized void clear() {
		if (this.mapViewBitmap1 != null) {
			this.mapViewBitmap1.eraseColor(MAP_VIEW_BACKGROUND);
		}

		if (this.mapViewBitmap2 != null) {
			this.mapViewBitmap2.eraseColor(MAP_VIEW_BACKGROUND);
		}
	}

	synchronized void destroy() {
		if (this.mapViewBitmap1 != null) {
			this.mapViewBitmap1.recycle();
		}

		if (this.mapViewBitmap2 != null) {
			this.mapViewBitmap2.recycle();
		}
	}

	synchronized void draw(Canvas canvas) {
		if (this.mapViewBitmap1 != null) {
		  final Rect screen = canvas.getClipBounds();
		  canvas.drawRect(screen.left, screen.top, screen.right, screen.bottom, Brush.LightGrey);
		  canvas.drawBitmap(mapViewBitmap1, screen.left, screen.top, Brush.LightGrey);
		}
	}

	synchronized void onSizeChanged(final int width, final int height) {
		this.width = width;
		this.height = height;
		this.mapViewBitmap1 = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
		this.mapViewBitmap2 = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
		clear();
		this.mapViewCanvas.setBitmap(this.mapViewBitmap1);
	}
}
