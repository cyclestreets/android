package net.cyclestreets.util;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class Brush 
{
	static public Paint Grey = createGreyBrush();
	static public Paint White = createWhiteBrush();

	static private Paint createGreyBrush()
	{
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.DEFAULT);

		paint.setARGB(255, 127, 127, 127);
		
		return paint;
	} // createBgBrush

	static private Paint createWhiteBrush()
	{
		final Paint paint = createGreyBrush();

		paint.setARGB(255, 255, 255, 255);
		
		return paint;
	} // createTextBrush

	static public Paint createTextBrush(final int size)
	{
		final Paint paint = createGreyBrush();

		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.DEFAULT);
		paint.setTextSize(size * 2);
		paint.setARGB(255, 255, 255, 255);
		
		return paint;
	} // createTextBrush
} // Brushes
