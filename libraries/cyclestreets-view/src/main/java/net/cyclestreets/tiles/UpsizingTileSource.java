package net.cyclestreets.tiles;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.io.InputStream;

class UpsizingTileSource implements ITileSource {
  private ITileSource base_;
  private final int upsize_ = 512;

  public UpsizingTileSource(final ITileSource base) {
    base_ = base;
  } // base

  @Override
  public int ordinal() { return base_.ordinal(); }
  @Override
  public String name() { return base_.name(); }
  @Override
  public String localizedName(ResourceProxy proxy) { return base_.localizedName(proxy); }
  @Override
  public int getMaximumZoomLevel() { return base_.getMaximumZoomLevel(); }
  @Override
  public int getMinimumZoomLevel() { return base_.getMinimumZoomLevel(); }
  @Override
  public String getTileRelativeFilenameString(MapTile aTile) { return base_.getTileRelativeFilenameString(aTile); }

  @Override
  public int getTileSizePixels() { return upsize_; }

  @Override
  public Drawable getDrawable(String aFilePath) throws BitmapTileSourceBase.LowMemoryException {
    return scaleUp(base_.getDrawable(aFilePath));
  } // getDrawable
  @Override
  public Drawable getDrawable(InputStream aTileInputStream) throws BitmapTileSourceBase.LowMemoryException {
    return scaleUp(base_.getDrawable(aTileInputStream));
  } // getDrawable

  private Drawable scaleUp(final Drawable mapTile) {
    if (mapTile == null)
      return null;

    Bitmap b = ((BitmapDrawable)mapTile).getBitmap();
    Bitmap largerB = Bitmap.createScaledBitmap(b, upsize_, upsize_, true);

    return new ExpirableBitmapDrawable(largerB);
  } // scaleUp
} // UpsizingTileSource
