package net.cyclestreets.itinerary

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.LabelFormatter
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.api.DistanceFormatter
import net.cyclestreets.fragments.R
import net.cyclestreets.routing.Elevation
import net.cyclestreets.routing.ElevationFormatter
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Segment
import net.cyclestreets.routing.Waypoints

import java.util.ArrayList
import java.util.Locale

import net.cyclestreets.util.StringUtils.initCap

class ElevationProfileFragment : Fragment(), Route.Listener {
    private lateinit var graphHolder: LinearLayout

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val elevation = inflater.inflate(R.layout.elevation, container, false)
        graphHolder = elevation.findViewById(R.id.graphview)
        return elevation
    }

    override fun onResume() {
        super.onResume()
        Route.onResume()
        Route.registerListener(this)
    }

    override fun onPause() {
        Route.unregisterListener(this)
        super.onPause()
    }

    override fun onNewJourney(journey: Journey, waypoints: Waypoints) {
        drawGraph(journey)
        fillInOverview(journey, view!!, context!!.getString(R.string.elevation_route))
    }

    override fun onResetJourney() {}

    private fun drawGraph(journey: Journey) {
        val graphView = GraphView(context)
        val formatter = elevationFormatter()

        val data = ArrayList<DataPoint>()
        for (elevation in journey.elevation().profile())
            data.add(DataPoint(elevation.distance().toDouble(), elevation.elevation().toDouble()))

        val graphSeries = LineGraphSeries(data.toTypedArray())
        graphSeries.isDrawBackground = true

        graphView.addSeries(graphSeries)

        // Allow zooming & scrolling on the x-axis (y-axis remains fixed)
        val viewport = graphView.viewport
        viewport.isScalable = true
        viewport.isYAxisBoundsManual = true
        viewport.setMinY(formatter.roundHeightBelow(journey.elevation().minimum()))
        viewport.setMaxY(formatter.roundHeightAbove(journey.elevation().maximum()))

        val gridLabelRenderer = graphView.gridLabelRenderer
        gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.BOTH
        gridLabelRenderer.numHorizontalLabels = 5
        gridLabelRenderer.numVerticalLabels = 5
        gridLabelRenderer.labelFormatter = ElevationLabelFormatter(formatter)
        // we handle y-rounding ourselves, and x-rounding makes labels overlap - see https://github.com/jjoe64/GraphView/issues/413
        gridLabelRenderer.setHumanRounding(false, false)

        graphHolder.removeAllViews()
        graphHolder.addView(graphView)
    }

    private class ElevationLabelFormatter constructor(private val formatter: ElevationFormatter) : LabelFormatter {
        override fun setViewport(viewport: Viewport) {}

        override fun formatLabel(value: Double, isValueX: Boolean): String {
            return if (isValueX)
                if (value != 0.0) formatter.distance(value.toInt()) else ""
            else formatter.roundedHeight(value.toInt())
        }
    }
}
