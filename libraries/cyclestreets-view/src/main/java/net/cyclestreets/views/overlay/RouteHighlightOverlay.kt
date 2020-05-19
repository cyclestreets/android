package net.cyclestreets.views.overlay

import android.content.Context
import android.graphics.Canvas
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import net.cyclestreets.iconics.IconicsHelper.materialIcons
import net.cyclestreets.routing.Route.journey
import net.cyclestreets.routing.Route.routeAvailable
import net.cyclestreets.routing.Segment
import net.cyclestreets.util.Theme.highlightColor
import net.cyclestreets.util.Theme.lowlightColor
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay


class RouteHighlightOverlay(context: Context, private val mapView: CycleMapView) : Overlay() {

    private var current: Segment? = null

    private val routingInfoRect: Button
    private val routeNowIcon: ImageView
    private val prevButton: FloatingActionButton
    private val nextButton: FloatingActionButton

    private val highlightColour: Int

    init {
        val routeView = LayoutInflater.from(mapView.context).inflate(R.layout.route_view, null)

        routingInfoRect = routeView.findViewById(R.id.routing_info_rect)
        routeNowIcon = routeView.findViewById(R.id.route_now_icon)

        val (prevIcon, nextIcon) = materialIcons(context, listOf(GoogleMaterial.Icon.gmd_chevron_left, GoogleMaterial.Icon.gmd_chevron_right), lowlightColor(context))

        prevButton = routeView.findViewById<FloatingActionButton>(R.id.route_highlight_prev).apply {
            setImageDrawable(prevIcon)
            visibility = View.INVISIBLE
            setOnClickListener { _ -> regressActiveSegment(1) }
            setOnLongClickListener { _ -> regressActiveSegment(Int.MAX_VALUE) }
        }

        nextButton = routeView.findViewById<FloatingActionButton>(R.id.route_highlight_next).apply {
            setImageDrawable(nextIcon)
            visibility = View.INVISIBLE
            setOnClickListener { _ -> advanceActiveSegment(1) }
            setOnLongClickListener { _ -> advanceActiveSegment(Int.MAX_VALUE) }
        }

        mapView.addView(routeView)

        highlightColour = highlightColor(context) or -0x1000000
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        drawButtons()

        if (current === journey().activeSegment())
            return

        current = journey().activeSegment()
        if (current == null)
            return

        drawSegmentInfo()
        this.mapView.controller.animateTo(current!!.start())
    }

    private fun drawButtons() {
        if (!routeAvailable()) {
            prevButton.hide()
            nextButton.hide()
            return
        }

        prevButton.isEnabled = !journey().atStart()
        prevButton.show()
        nextButton.isEnabled = !journey().atEnd()
        nextButton.show()
    }

    private fun drawSegmentInfo() {
        // If there's no active segment, populating the routing info is done by the TapToRouteOverlay
        val seg = journey().activeSegment() ?: return

        routeNowIcon.visibility = View.INVISIBLE

        routingInfoRect.setBackgroundColor(highlightColour)
        routingInfoRect.gravity = Gravity.LEFT
        routingInfoRect.text = seg.toString()
        routingInfoRect.isEnabled = false
    }

    private fun regressActiveSegment(stepsToMove: Int): Boolean {
        if (!routeAvailable())
            return false

        for (i in stepsToMove downTo 1) {
            if (journey().atStart())
                break
            journey().regressActiveSegment()
        }

        mapView.invalidate()
        return true
    }

    private fun advanceActiveSegment(stepsToMove: Int): Boolean {
        if (!routeAvailable())
            return false

        for (i in stepsToMove downTo 1) {
            if (journey().atEnd())
                break
            journey().advanceActiveSegment()
        }

        mapView.invalidate()
        return true
    }

}