package net.cyclestreets.views.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class OverlayHelper 
{	
	static int offset(final Context context)
	{
		return (int)(8 * context.getResources().getDisplayMetrics().density);
 	} // offset
	
	static float cornerRadius(final Context context)
	{
		return 4.0f * context.getResources().getDisplayMetrics().density;
	} // cornerRadius		
	
	static private final Matrix canvasTransform_ = new Matrix();
	static private final float[] transformValues_ = new float[9];
	static private final Matrix bitmapTransform_ = new Matrix();
	static private final Paint bitmapPaint_ = new Paint();

	static void drawRoundRect(final Canvas canvas, 
							  final Rect rect, 
							  final float cornerRadius, 
							  final Paint brush)
	{
		canvas.getMatrix(canvasTransform_);
		canvasTransform_.getValues(transformValues_);

		float newWidth = rect.width() / transformValues_[Matrix.MSCALE_X];
		float newHeight = rect.height() / transformValues_[Matrix.MSCALE_Y];
		
		final RectF rf = new RectF(rect);
		rf.right = rf.left + newWidth;
		rf.bottom = rf.top + newHeight;
		
		canvas.drawRoundRect(rf, cornerRadius, cornerRadius, brush);
	} // drawRoundRect
	
	static void drawBitmap(final Canvas canvas,
						   final Bitmap bitmap,
						   final Rect position)
	{
		canvas.getMatrix(canvasTransform_);
		canvasTransform_.getValues(transformValues_);
		
		bitmapTransform_.setTranslate(0, 0);
		bitmapTransform_.postScale(1/transformValues_[Matrix.MSCALE_X], 1/transformValues_[Matrix.MSCALE_Y]);
		bitmapTransform_.postTranslate(position.left, position.top);
		canvas.drawBitmap(bitmap, bitmapTransform_, bitmapPaint_);
	} // drawBitmap
} // class OverlayHelper
