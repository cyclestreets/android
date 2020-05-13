package net.cyclestreets.views.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable


const val DEFAULT_DRAWABLE_SIZE = -1

fun offset(context: Context): Int {
    return (8 * context.resources.displayMetrics.density).toInt()
}

fun cornerRadius(): Float {
    return 0F
}

fun drawRoundRect(canvas: Canvas,
                  rect: Rect?,
                  cornerRadius: Float,
                  brush: Paint) {
    canvas.drawRoundRect(RectF(rect), cornerRadius, cornerRadius, brush)
}

/**
 * Extract the Bitmap from a Drawable and resize it to the expectedSize conserving the ratio.
 *
 * @param drawable   Drawable used to extract the Bitmap. Can be null.
 * @param expectSize Expected size for the Bitmap. Use [.DEFAULT_DRAWABLE_SIZE] to
 * keep the original [Drawable] size.
 * @return The Bitmap associated to the Drawable or null if the drawable was null.
 * @see <html>[Stackoverflow answer](https://stackoverflow.com/a/10600736/1827254)</html>
 */
fun getBitmapFromDrawable(drawable: Drawable, expectSize: Int = DEFAULT_DRAWABLE_SIZE): Bitmap {

    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val ratio = if (expectSize == DEFAULT_DRAWABLE_SIZE) 1f else {
        calculateRatio(drawable.intrinsicWidth, drawable.intrinsicHeight, expectSize)
    }
    val width = (drawable.intrinsicWidth * ratio).toInt()
    val height = (drawable.intrinsicHeight * ratio).toInt()

    val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

/**
 * Calculate the ratio to multiply the Bitmap size with, for it to be the maximum size of
 * "expected".
 *
 * @param height   Original Bitmap height
 * @param width    Original Bitmap width
 * @param expected Expected maximum size.
 * @return If height and with equals 0, 1 is return. Otherwise the ratio is returned.
 * The ratio is based on the greatest side so the image will always be the maximum size.
 */
private fun calculateRatio(height: Int, width: Int, expected: Int): Float {
    return if (height > width) {
        expected / width.toFloat()
    } else {
        expected / height.toFloat()
    }
}
