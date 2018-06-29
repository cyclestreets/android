package net.cyclestreets;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

import net.cyclestreets.api.DistanceFormatter;
import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.Elevation;
import net.cyclestreets.routing.ElevationFormatter;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.routing.Waypoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.cyclestreets.util.StringUtils.initCap;

public class ElevationProfileFragment extends Fragment implements Route.Listener {
  private LinearLayout graphHolder;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle savedInstanceState) {
    final View elevation = inflater.inflate(R.layout.elevation, container, false);
    graphHolder = (LinearLayout)elevation.findViewById(R.id.graphview);
    return elevation;
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
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    drawGraph(journey);
    fillInOverview(journey);
  }

  @Override
  public void onResetJourney() {}

  private void drawGraph(final Journey journey) {
    final LineGraphView graph = new LineGraphView(getActivity(), "");
    final ElevationFormatter formatter = elevationFormatter();

    List<GraphView.GraphViewData> data = new ArrayList<>();
    for (Elevation elevation : journey.elevation().profile())
      data.add(new GraphView.GraphViewData(elevation.distance(), elevation.elevation()));

    GraphViewSeries graphSeries = new GraphViewSeries(data.toArray(new GraphView.GraphViewData[]{}));

    graph.addSeries(graphSeries);
    graph.setDrawBackground(true);

    graph.setManualYMinBound(formatter.roundHeightBelow(journey.elevation().minimum()));
    graph.setManualYMaxBound(formatter.roundHeightAbove(journey.elevation().maximum()));

    graph.getGraphViewStyle().setGridStyle(GraphViewStyle.GridStyle.HORIZONTAL);
    graph.getGraphViewStyle().setNumHorizontalLabels(5);
    graph.getGraphViewStyle().setNumVerticalLabels(5);

    graph.setCustomLabelFormatter((value, isValueX) -> {
      if (isValueX)
        return (value != 0) ? formatter.distance((int)value) : "";
      return formatter.height((int) value);
    });

    graphHolder.removeAllViews();
    graphHolder.addView(graph);
  }

  public TextView getTextView(int id) {
    return getView().findViewById(id);
  }

  /////////////
  // TODO: The methods below are duplicated in ItineraryFragment.java.  Commonise as default
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
    setText(R.id.routetype, initCap(journey.plan()) + " " + getContext().getString(R.string.elevation_route) + ":");
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
