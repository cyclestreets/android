package net.cyclestreets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.ListFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.cyclestreets.api.DistanceFormatter;
import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.ElevationFormatter;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.util.Theme;
import net.cyclestreets.util.TurnIcons;

import java.util.Locale;

import static net.cyclestreets.util.StringUtils.initCap;

public class ItineraryFragment extends ListFragment implements Route.Listener {
  private Journey journey = Journey.NULL_JOURNEY;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setListAdapter(new SegmentAdapter(getActivity(), this));
  }

  @Override
  public void onResume() {
    super.onResume();
    Route.onResume();
    Route.registerListener(this);
  }

  @Override
  public void onPause() {
    Route.unregisterListener(this);
    super.onPause();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    if (journey.isEmpty())
      return;

    journey.setActiveSegmentIndex(position);
    try {
      ((RouteMapActivity)getActivity()).showMap();
    } catch (Exception e) {
    }
  }

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    this.journey = journey;
    setSelection(this.journey.activeSegmentIndex());
  }

  @Override
  public void onResetJourney() {
    journey = Journey.NULL_JOURNEY;
  }

  //////////////////////////////////
  static class SegmentAdapter extends BaseAdapter {
    private final ItineraryFragment itinerary;
    private final TurnIcons.Mapping iconMappings;
    private final Drawable footprints;
    private final LayoutInflater inflater;
    private final Drawable themeColor;
    private final int backgroundColor;
    private final String routeString;
    private View v;

    SegmentAdapter(final Context context, final ItineraryFragment itinerary) {
      this.itinerary = itinerary;
      iconMappings = TurnIcons.LoadMapping(context);
      footprints = ResourcesCompat.getDrawable(context.getResources(), R.drawable.footprints, null);

      inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      themeColor = ResourcesCompat.getDrawable(context.getResources(), R.color.apptheme_color, null);
      backgroundColor = Theme.backgroundColor(context);
      routeString = context.getString(R.string.elevation_route);
    }

    private Journey journey() {
      return itinerary.journey;
    }

    private boolean hasSegments() {
      return !journey().isEmpty();
    }

    @Override
    public int getCount() {
      return hasSegments() ? journey().segments().count() : 1;
    }

    @Override
    public Object getItem(int position) {
      if (!hasSegments())
        return null;
      return journey().segments().get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      if (!hasSegments())
        return inflater.inflate(R.layout.itinerary_not_available, parent, false);

      final Segment seg = Route.journey().segments().get(position);
      final int layout_id = position != 0 ? R.layout.itinerary_item : R.layout.itinerary_header_item;
      v = inflater.inflate(layout_id, parent, false);

      final boolean highlight = (position == Route.journey().activeSegmentIndex());

      if (position == 0) {
        fillInOverview(Route.journey());
      }
      setText(R.id.segment_distance, seg.distance(), highlight);
      setText(R.id.segment_cumulative_distance, seg.runningDistance(), highlight);
      setText(R.id.segment_time, seg.runningTime(), highlight);

      setMainText(R.id.segment_street, seg.turn(), seg.street(), highlight);
      setTurnIcon(R.id.segment_type, seg.turn(), seg.walk());

      if (highlight && position != 0)
        v.setBackground(themeColor);

      return v;
    }

    private void setText(final int id, final String t, final boolean highlight) {
      final TextView n = getTextView(id);
      if (n == null)
        return;
      n.setText(t);
      if (highlight)
        n.setTextColor(Color.BLACK);
    }

    private void setMainText(final int id, final String turn, final String street, final boolean highlight) {
      String t = street;
      if (turn.length() != 0)
        t = turn + " into " + street;
      setText(id, t, highlight);
    }

    private void setTurnIcon(final int id, final String turn, final boolean walk) {
      final ImageView iv = v.findViewById(id);
      if (iv == null)
        return;

      final Drawable icon = turnIcon(turn);
      iv.setImageDrawable(icon);
      iv.setBackgroundColor(backgroundColor);
      if (walk)
        iv.setBackground(footprints);
    }

    private Drawable turnIcon(final String turn) {
      return iconMappings.icon(turn);
    }

    public TextView getTextView(int id) {
      return v.findViewById(id);
    }

    /////////////
    // TODO: The methods below are duplicated in ElevationProfileFragment.java.  Commonise as default
    //       interface methods once our minSdkVersion is >=24.
    private DistanceFormatter distanceFormatter() {
      return DistanceFormatter.formatter(CycleStreetsPreferences.units());
    }

    private ElevationFormatter elevationFormatter() {
      return ElevationFormatter.formatter(CycleStreetsPreferences.units());
    }

    private void fillInOverview(final Journey journey) {
      Segment.Start start = journey.segments().first();

      setText(R.id.title, journey.name());
      setText(R.id.journeyid, String.format(Locale.getDefault(), "#%,d", journey.itinerary()));
      setText(R.id.routetype, initCap(journey.plan()) + " " + routeString + ":");
      setText(R.id.distance, distanceFormatter().total_distance(journey.total_distance()));
      setText(R.id.journeytime, start.totalTime());
      setText(R.id.calories, start.calories());
      setText(R.id.carbondioxide, start.co2());
      setText(R.id.elevation_gain, elevationFormatter().height(journey.elevation().totalElevationGain()));
      setText(R.id.elevation_loss, elevationFormatter().height(journey.elevation().totalElevationLoss()));
    }

    private void setText(int id, String text) {
      getTextView(id).setText(text);
    }
  }
}
