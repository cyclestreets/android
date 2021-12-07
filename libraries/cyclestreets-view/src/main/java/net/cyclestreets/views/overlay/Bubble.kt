package net.cyclestreets.views.overlay

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.view.MotionEvent
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import net.cyclestreets.util.Draw
import org.osmdroid.api.IProjection
import org.osmdroid.views.MapView
import net.cyclestreets.Undoable
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.views.Projection

object Bubble {
    var activeItem: POIOverlay.POIOverlayItem? = null
    private var bubble: Rect? = null
    private val curScreenCoords = Point()
    private lateinit var tapHereText: String
    private lateinit var controller: ControllerOverlay

    fun initialise(context: Context, mapView: CycleMapView) {
        controller = mapView.controllerOverlay_
        tapHereText = context.getString(R.string.tap_here)
    }

    fun onSingleTap(event: MotionEvent, pj: Projection, context: Context, overlay: Undoable): Boolean {
        if (activeItem != null && tappedInBubble(event, pj, context, overlay))
            return true
        return false
    }

    private fun tappedInBubble(event: MotionEvent, pj: Projection, context: Context, overlay: Undoable): Boolean {
        val screenRect = pj.intrinsicScreenRect
        val eventX = screenRect.left + event.x.toInt()
        val eventY = screenRect.top + event.y.toInt()

        if (!bubble!!.contains(eventX, eventY))
            return false
        // Check if tapped on link
        if (eventY < Draw.titleSectionY) {
            showWebpage(activeItem, context)
        }

        hideBubble(overlay)
        return true
    }

    private fun showWebpage(item: POIOverlay.POIOverlayItem?, context: Context) {
        val url = item!!.poi.url()

        if (url != "") {
            val webpage = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                ContextCompat.startActivity(context, intent, null)
            }
        }
    }

    fun hideOrShowBubble(item: POIOverlay.POIOverlayItem?, overlay: Undoable) {
        if (activeItem === item)
            hideBubble(overlay)
        else
            showBubble(item!!, overlay)
    }

    fun showBubble(item: POIOverlay.POIOverlayItem, overlay: Undoable) {
        hideBubble(overlay)
        activeItem = item
        controller.pushUndo(overlay)
    }

    fun hideBubble(overlay: Undoable) {
        activeItem = null
        controller.flushUndo(overlay)
    }

    fun drawBubble(canvas: Canvas, mapView: MapView, textBrush: Paint, urlBrush: Paint, offset: Int) {
        var url = activeItem!!.poi.url()
        url = if (URLUtil.isValidUrl(url)) url else ""
        val title = if (activeItem!!.title.isNullOrEmpty() && (activeItem!!.poi.url().isNotBlank())) tapHereText else activeItem!!.title

        val bubbleText = listOf(
                title,
                activeItem!!.snippet,
                activeItem!!.poi.phone(),
                activeItem!!.poi.openingHours()
        ).filterNot { it.isNullOrBlank() }.joinToString("\n")

        // find the right place
        val pj: IProjection = mapView.projection
        pj.toPixels(activeItem!!.point, curScreenCoords)

        val x = curScreenCoords.x
        val y = curScreenCoords.y

        val matrix = mapView.matrix
        val matrixValues = FloatArray(9)
        matrix.getValues(matrixValues)

        val scaleX = Math.sqrt(matrixValues[Matrix.MSCALE_X]
                * matrixValues[Matrix.MSCALE_X] + matrixValues[Matrix.MSKEW_Y]
                * matrixValues[Matrix.MSKEW_Y].toDouble()).toFloat()
        val scaleY = Math.sqrt(matrixValues[Matrix.MSCALE_Y]
                * matrixValues[Matrix.MSCALE_Y] + matrixValues[Matrix.MSKEW_X]
                * matrixValues[Matrix.MSKEW_X].toDouble()).toFloat()

        canvas.save()
        canvas.rotate(-mapView.mapOrientation, x.toFloat(), y.toFloat())
        canvas.scale(1 / scaleX, 1 / scaleY, x.toFloat(), y.toFloat())

        bubble = Draw.drawBubble(canvas, textBrush, urlBrush, offset, cornerRadius(), curScreenCoords, bubbleText, url, title)

        canvas.restore()
    }

}