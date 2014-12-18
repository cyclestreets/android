package net.cyclestreets.views.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class DrawingHelper 
{	
	static int offset(final Context context)
	{
		return (int)(8 * context.getResources().getDisplayMetrics().density);
 	} // offset
	
	static float cornerRadius(final Context context)
	{
		return 0;
	} // cornerRadius		
	
	static private final Matrix canvasTransform_ = new Matrix();
	static private final float[] transformValues_ = new float[9];
	
	static boolean isDragging(final Canvas canvas)
	{
		canvas.getMatrix(canvasTransform_);
		canvasTransform_.getValues(transformValues_);
		//return (transformValues_[Matrix.MSCALE_X] != 1.0);
    return false;
	} // isDragging
	
	static boolean drawRoundRect(final Canvas canvas, 
							  final Rect rect, 
							  final float cornerRadius, 
							  final Paint brush)
	{
		if(isDragging(canvas))
			return false;

		canvas.drawRoundRect(new RectF(rect), cornerRadius, cornerRadius, brush);
		return true;
	} // drawRoundRect
	
	static private Matrix bitmapTransform_ = new Matrix();
	
	static void drawBitmap(final Canvas canvas,
			                   final Bitmap bitmap,
                         final Rect position)
	{
		if(isDragging(canvas))
			return;

		bitmapTransform_.setTranslate(0, 0);
		bitmapTransform_.postTranslate(position.left, position.top);
		canvas.drawBitmap(bitmap, bitmapTransform_, null);
	} // drawBitmap
} // class OverlayHelper
