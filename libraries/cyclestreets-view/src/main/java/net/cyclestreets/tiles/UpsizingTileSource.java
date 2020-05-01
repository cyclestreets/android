package net.cyclestreets.tiles;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import java.io.InputStream;

public class UpsizingTileSource implements ITileSource {
  private ITileSource base_;
  private OnlineTileSourceBase online_;
  private final int upsize_ = 512;

  public UpsizingTileSource(final ITileSource base) {
    base_ = base;
    online_ = (base_ instanceof  OnlineTileSourceBase) ? (OnlineTileSourceBase)base_ : null;
  }

  @Override
  @Deprecated
  public int ordinal() { return base_.ordinal(); }
  @Override
  public String name() { return base_.name(); }
  @Override
  public int getMaximumZoomLevel() { return base_.getMaximumZoomLevel(); }
  @Override
  public int getMinimumZoomLevel() { return base_.getMinimumZoomLevel(); }
  @Override
  public String getTileRelativeFilenameString(long pMapTileIndex) {
    return base_.getTileRelativeFilenameString(pMapTileIndex);
  }

  public String getTileURLString(long pMapTileIndex) {
    return online_ != null ? online_.getTileURLString(pMapTileIndex) : null;
  }

  @Override
  public int getTileSizePixels() { return upsize_; }

  @Override
  public String getCopyrightNotice() { return base_.getCopyrightNotice(); }

  @Override
  public Drawable getDrawable(String aFilePath) throws BitmapTileSourceBase.LowMemoryException {
    return scaleUp(base_.getDrawable(aFilePath));
  }
  @Override
  public Drawable getDrawable(InputStream aTileInputStream) throws BitmapTileSourceBase.LowMemoryException {
    return scaleUp(base_.getDrawable(aTileInputStream));
  }

  private Drawable scaleUp(final Drawable mapTile) {
    if (mapTile == null)
      return null;

    Bitmap b = ((BitmapDrawable)mapTile).getBitmap();
    Bitmap largerB = Bitmap.createScaledBitmap(b, upsize_, upsize_, true);

    return new ExpirableBitmapDrawable(largerB);
  }
}
