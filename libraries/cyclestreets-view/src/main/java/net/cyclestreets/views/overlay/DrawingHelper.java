package net.cyclestreets.views.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class DrawingHelper {
  public static int offset(final Context context) {
    return (int)(8 * context.getResources().getDisplayMetrics().density);
  } // offset
  
  public static float cornerRadius(final Context context) {
    return 0;
  } // cornerRadius    
  
  public static void drawRoundRect(final Canvas canvas,
                                   final Rect rect,
                                   final float cornerRadius,
                                   final Paint brush) {
    canvas.drawRoundRect(new RectF(rect), cornerRadius, cornerRadius, brush);
  } // drawRoundRect
  
  public static Matrix bitmapTransform_ = new Matrix();
  
  public static void drawBitmap(final Canvas canvas,
                                final Bitmap bitmap,
                                final Rect position) {
    bitmapTransform_.setTranslate(0, 0);
    bitmapTransform_.postTranslate(position.left, position.top);
    canvas.drawBitmap(bitmap, bitmapTransform_, null);
  } // drawBitmap
} // class OverlayHelper
