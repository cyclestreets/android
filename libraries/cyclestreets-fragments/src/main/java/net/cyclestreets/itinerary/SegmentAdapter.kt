package net.cyclestreets.itinerary

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import net.cyclestreets.CycleStreetsPreferences
import net.cyclestreets.api.DistanceFormatter
import net.cyclestreets.fragments.R
import net.cyclestreets.routing.ElevationFormatter
import net.cyclestreets.routing.Journey
import net.cyclestreets.routing.Route
import net.cyclestreets.routing.Segment
import net.cyclestreets.util.StringUtils
import net.cyclestreets.util.Theme
import net.cyclestreets.util.TurnIcons
import java.util.*

internal class SegmentAdapter(context: Context) : BaseAdapter() {
    private val iconMappings: TurnIcons.Mapping = TurnIcons.LoadMapping(context)
    private val footprints: Drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.footprints, null)!!
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val themeColor: Drawable = ResourcesCompat.getDrawable(context.resources, R.color.apptheme_color, null)!!
    private val backgroundColor: Int = Theme.backgroundColor(context)
    private val routeString: String = context.getString(R.string.elevation_route)
    private var v: View? = null

    private fun hasSegments(): Boolean {
        return !Route.journey().isEmpty
    }

    override fun getCount(): Int {
        return if (hasSegments()) Route.journey().segments().count() else 1
    }

    override fun getItem(position: Int): Segment? {
        return if (!hasSegments()) null else Route.journey().segments().get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        if (!hasSegments())
            return inflater.inflate(R.layout.itinerary_not_available, parent, false)

        val seg = Route.journey().segments().get(position)
        val layoutId = if (position != 0) R.layout.itinerary_item else R.layout.itinerary_header_item
        val view: View = inflater.inflate(layoutId, parent, false)
        v = view;

        val highlight = position == Route.journey().activeSegmentIndex()

        if (position == 0) {
            fillInOverview(Route.journey())
        }
        setText(R.id.segment_distance, seg.distance(), highlight)
        setText(R.id.segment_cumulative_distance, seg.runningDistance(), highlight)
        setText(R.id.segment_time, seg.runningTime(), highlight)

        setMainText(R.id.segment_street, seg.turn(), seg.street(), highlight)
        setTurnIcon(R.id.segment_type, seg.turn(), seg.walk())

        if (highlight && position != 0)
            view.background = themeColor

        return view
    }

    private fun setText(id: Int, t: String, highlight: Boolean) {
        val n = getTextView(id) ?: return
        n.text = t
        if (highlight)
            n.setTextColor(Color.BLACK)
    }

    private fun setMainText(id: Int, turn: String, street: String, highlight: Boolean) {
        val text = if (turn.isNotEmpty()) "$turn into $street" else street
        setText(id, text, highlight)
    }

    private fun setTurnIcon(id: Int, turn: String, walk: Boolean) {
        val iv = v!!.findViewById<ImageView>(id) ?: return

        val icon = turnIcon(turn)
        iv.setImageDrawable(icon)
        iv.setBackgroundColor(backgroundColor)
        if (walk)
            iv.background = footprints
    }

    private fun turnIcon(turn: String): Drawable {
        return iconMappings.icon(turn)
    }

    private fun getTextView(id: Int): TextView? {
        return v!!.findViewById(id)
    }

    /////////////
    // TODO: The methods below are duplicated in ElevationProfileFragment.java.  Commonise as default
    //       interface methods once our minSdkVersion is >=24.
    private fun distanceFormatter(): DistanceFormatter {
        return DistanceFormatter.formatter(CycleStreetsPreferences.units())
    }

    private fun elevationFormatter(): ElevationFormatter {
        return ElevationFormatter.formatter(CycleStreetsPreferences.units())
    }

    private fun fillInOverview(journey: Journey) {
        val start = journey.segments().first()

        setText(R.id.title, journey.name())
        setText(R.id.journeyid, String.format(Locale.getDefault(), "#%,d", journey.itinerary()))
        setText(R.id.routetype, StringUtils.initCap(journey.plan()) + " " + routeString + ":")
        setText(R.id.distance, distanceFormatter().total_distance(journey.total_distance()))
        setText(R.id.journeytime, start.totalTime())
        setText(R.id.calories, start.calories())
        setText(R.id.carbondioxide, start.co2())
        setText(R.id.elevation_gain, elevationFormatter().height(journey.elevation().totalElevationGain()))
        setText(R.id.elevation_loss, elevationFormatter().height(journey.elevation().totalElevationLoss()))
    }

    private fun setText(id: Int, text: String) {
        getTextView(id)!!.text = text
    }
}
