package net.cyclestreets.util;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class Brush 
{
	static public Paint Grey = createColourBrush(192, 127, 127, 127);
	static public Paint LightGrey = createColourBrush(255, 192, 192, 192);
	static public Paint White = createColourBrush(255, 255, 255, 255);

	static private Paint createColourBrush(final int a, final int r, final int g, final int b)
	{
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.DEFAULT);

		paint.setARGB(a, r, g, b);
		
		return paint;
	} // createBgBrush

	static public Paint createTextBrush(final int size)
	{
		final Paint paint = createColourBrush(255, 255, 255, 255);

		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.DEFAULT);
		paint.setTextSize(size * 2);
		
		return paint;
	} // createTextBrush
} // Brushes
