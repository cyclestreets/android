package org.mapsforge.map.android.graphics;

import android.graphics.Bitmap;

public final class AndroidBitmapExpose {
  public static Bitmap expose(AndroidBitmap mapsforgeAndroidBitmap) {
    return mapsforgeAndroidBitmap.bitmap;
  }
}
