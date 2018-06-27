package net.cyclestreets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.cyclestreets.api.DistanceFormatter;
import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.routing.Waypoints;
import net.cyclestreets.util.Theme;
import net.cyclestreets.util.TurnIcons;

import static net.cyclestreets.util.StringUtils.initCap;

public class ItineraryFragment extends ListFragment implements Route.Listener {
  private Journey journey_ = Journey.NULL_JOURNEY;

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
    if (journey_.isEmpty())
      return;

    journey_.setActiveSegmentIndex(position);
    try {
      ((RouteMapActivity)getActivity()).showMap();
    } catch (Exception e) {
    }
  }

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    journey_ = journey;
    setSelection(journey_.activeSegmentIndex());
  }

  @Override
  public void onResetJourney() {
    journey_ = Journey.NULL_JOURNEY;
  }

  //////////////////////////////////
  static class SegmentAdapter extends BaseAdapter {
    private final ItineraryFragment itinerary_;
    private final TurnIcons.Mapping iconMappings_;
    private final Drawable footprints_;
    private final LayoutInflater inflater_;
    private final Drawable themeColor_;
    private final int backgroundColor_;

    SegmentAdapter(final Context context, final ItineraryFragment itinerary) {
      itinerary_ = itinerary;
      iconMappings_ = TurnIcons.LoadMapping(context);
      footprints_ = context.getResources().getDrawable(R.drawable.footprints);

      inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      themeColor_ = context.getResources().getDrawable(R.color.apptheme_color);
      backgroundColor_ = Theme.backgroundColor(context);
    }

    private Journey journey() { return itinerary_.journey_; }

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
        return inflater_.inflate(R.layout.itinerary_not_available, parent, false);

      final Segment seg = Route.journey().segments().get(position);
      final int layout_id = position != 0 ? R.layout.itinerary_item : R.layout.itinerary_header_item;
      final View v = inflater_.inflate(layout_id, parent, false);

      final boolean highlight = (position == Route.journey().activeSegmentIndex());

      if (position == 0) {
        Journey journey = Route.journey();
        Segment.Start start = journey.segments().first();

        setText(v, R.id.title, journey.name(), false);
        setText(v, R.id.journeyid, String.format("#%,d", journey.itinerary()), false);
        setText(v, R.id.routetype, initCap(journey.plan()) + " route:", false);
        setText(v, R.id.distance, distance(journey.total_distance()), false);
        setText(v, R.id.journeytime, start.totalTime(), false);
        setText(v, R.id.calories, start.calories(), false);
        setText(v, R.id.carbondioxide, start.co2(), false);
      }
      setText(v, R.id.segment_distance, seg.distance(), highlight);
      setText(v, R.id.segment_cumulative_distance, seg.runningDistance(), highlight);
      setText(v, R.id.segment_time, seg.runningTime(), highlight);

      setMainText(v, R.id.segment_street, seg.turn(), seg.street(), highlight);
      setTurnIcon(v, R.id.segment_type, seg.turn(), seg.walk());

      if (highlight && position != 0)
        v.setBackgroundDrawable(themeColor_);

      return v;
    }

    private void setText(final View v, final int id, final String t, final boolean highlight) {
      final TextView n = (TextView)v.findViewById(id);
      if (n == null)
        return;
      n.setText(t);
      if (highlight)
        n.setTextColor(Color.BLACK);
    }

    private void setMainText(final View v, final int id, final String turn, final String street, final boolean highlight) {
      String t = street;
      if (turn.length() != 0)
        t = turn + " into " + street;
      setText(v, id, t, highlight);
    }

    private void setTurnIcon(final View v, final int id, final String turn, final boolean walk) {
      final ImageView iv = (ImageView)v.findViewById(id);
      if (iv == null)
        return;

      final Drawable icon = turnIcon(turn);
      iv.setImageDrawable(icon);
      iv.setBackgroundColor(backgroundColor_);
      if (walk)
        iv.setBackgroundDrawable(footprints_);
    }

    private Drawable turnIcon(final String turn) {
      return iconMappings_.icon(turn);
    }

    private String distance(final int metres) { return DistanceFormatter.formatter(CycleStreetsPreferences.units()).total_distance(metres); }
  }
}
