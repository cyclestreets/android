package net.cyclestreets.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;

public class Brush {
  public static Paint Grey = createFillBrush(127, 127, 127);
  public static Paint LightGrey = createFillBrush(192, 192, 192);
  public static Paint White = createFillBrush(255, 255, 255);
  public static Paint BlackOutline = createOutlineBrush(0, 0, 0);
  public static Paint LowlightBrush(final Context context) {
    return createFillBrush(Theme.lowlightColor(context));
  } // LowlightBrush
  public static Paint HighlightBrush(final Context context) {
    return createFillBrush(Theme.highlightColor(context));
  } // HighlightBrush

  private static Paint createFillBrush(final int color) {
    Paint brush = createFillBrush(255, 255, 255);
    int opaqueColor = color | 0xff000000;
    brush.setColor(opaqueColor);
    return brush;
  } // createFillBrush

  private static Paint createFillBrush(final int r, final int g, final int b) {
    return createBrush(255, r, g, b, Style.FILL_AND_STROKE);
  } // createFillBrush
  
  private static Paint createOutlineBrush(final int r, final int g, final int b) {
    final Paint brush = createBrush(255, r, g, b, Style.STROKE);
    brush.setStrokeWidth(0);
    return brush;
  } // createOutlineBrush

  private static Paint createBrush(final int a, final int r, final int g, final int b, final Style style) {
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(style);
    paint.setARGB(a, r, g, b);
    return paint;
  } // createBrush
  
  public static Paint createTextBrush(final int size) {
    return createTextBrush(size, 255, 255, 255);
  } // createTextBrush
  
  public static Paint createTextBrush(final int size, final int r, final int g, final int b)  {
    final Paint paint = createFillBrush(r, g, b);

    paint.setTextAlign(Paint.Align.CENTER);
    paint.setTypeface(Typeface.DEFAULT);
    paint.setTextSize(size * 2);
    
    return paint;
  } // createTextBrush
  
  private Brush() { }
} // Brushes
