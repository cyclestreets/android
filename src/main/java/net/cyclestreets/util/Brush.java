package net.cyclestreets.util;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class Brush 
{
	static public Paint Grey = createFillBrush(255, 127, 127, 127);
	static public Paint LightGrey = createFillBrush(255, 192, 192, 192);
	static public Paint White = createFillBrush(255, 255, 255, 255);
	static public Paint BlackOutline = createOutlineBrush(255,0,0,0);

	static private Paint createFillBrush(final int a, final int r, final int g, final int b)
	{
	  return createBrush(a, r, g, b, Style.FILL_AND_STROKE);
	} // createFillBrush
	
  static private Paint createOutlineBrush(final int a, final int r, final int g, final int b)
  {
    final Paint brush = createBrush(a, r, g, b, Style.STROKE);
    brush.setStrokeWidth(0);
    return brush;
  } // createOutlineBrush

  static private Paint createBrush(final int a, final int r, final int g, final int b, final Style style)
	{
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(style);
    paint.setARGB(a, r, g, b);    
    return paint;
	} // createBrush
	
	static public Paint createTextBrush(final int size)
	{
	  return createTextBrush(size, 255, 255, 255, 255);
	} // createTextBrush
	
	static public Paint createTextBrush(final int size, final int a, final int r, final int g, final int b)
	{
    final Paint paint = createFillBrush(a, r, g, b);

    paint.setTextAlign(Align.CENTER);
    paint.setTypeface(Typeface.DEFAULT);
    paint.setTextSize(size * 2);
    
    return paint;
	} // createTextBrush
} // Brushes
