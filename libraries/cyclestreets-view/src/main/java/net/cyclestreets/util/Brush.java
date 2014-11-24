package net.cyclestreets.util;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class Brush {
  public static Paint Grey = createFillBrush(255, 127, 127, 127);
  public static Paint LightGrey = createFillBrush(255, 192, 192, 192);
  public static Paint White = createFillBrush(255, 255, 255, 255);
  public static Paint BlackOutline = createOutlineBrush(255,0,0,0);

  private static Paint createFillBrush(final int a, final int r, final int g, final int b) {
    return createBrush(a, r, g, b, Style.FILL_AND_STROKE);
  } // createFillBrush
  
  private static Paint createOutlineBrush(final int a, final int r, final int g, final int b) {
    final Paint brush = createBrush(a, r, g, b, Style.STROKE);
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
    return createTextBrush(size, 255, 255, 255, 255);
  } // createTextBrush
  
  public static Paint createTextBrush(final int size, final int a, final int r, final int g, final int b)  {
    final Paint paint = createFillBrush(a, r, g, b);

    paint.setTextAlign(Align.CENTER);
    paint.setTypeface(Typeface.DEFAULT);
    paint.setTextSize(size * 2);
    
    return paint;
  } // createTextBrush
  
  private Brush() { }
} // Brushes
