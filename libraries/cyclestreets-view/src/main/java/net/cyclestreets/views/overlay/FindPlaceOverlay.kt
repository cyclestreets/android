package net.cyclestreets.views.overlay

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import net.cyclestreets.Undoable
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.api.IProjection
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class FindPlaceOverlay(context: Context, cycleMapView: CycleMapView) : Overlay(), Undoable {
    // (Passing CMV as parm so centreOn can be accessed, which we need to calc screen coords)
    private val placeMarker_: Drawable?
    private var cMapView_: CycleMapView
    private var halfWidth: Int = 0
    private var halfHeight: Int = 0

    private val controller = OverlayHelper(cycleMapView).controller()

    init {
        val res: Resources = context.getResources()
        placeMarker_ = ResourcesCompat.getDrawable(res, R.drawable.x_marks_spot, null)
        if (placeMarker_ != null) {
            halfWidth = placeMarker_.intrinsicWidth / 2
            halfHeight = placeMarker_.intrinsicHeight / 2
        }
        cMapView_ = cycleMapView
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        val foundPlace: IGeoPoint? = cMapView_.getFoundPlace()
        if ((foundPlace == null) || (placeMarker_ == null)) return

        if (!controller.checkUndo(this))
            controller.pushUndo(this)

        val screenPos = Point()
        val projection: IProjection = mapView.projection
        projection.toPixels(foundPlace, screenPos)

        placeMarker_.bounds = Rect(screenPos.x - halfWidth,
                screenPos.y - halfHeight,
                screenPos.x + halfWidth,
                screenPos.y + halfHeight)
        placeMarker_.draw(canvas)
    }

    protected fun redraw() {
        cMapView_.postInvalidate()
    }

    override fun onBackPressed(): Boolean {
        controller.flushUndo(this)
        cMapView_.setFoundPlace(null)
        redraw()
        return true
    }

}
