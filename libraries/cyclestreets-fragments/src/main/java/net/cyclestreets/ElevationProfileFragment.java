package net.cyclestreets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import static net.cyclestreets.util.StringUtils.initCap;

public class ElevationProfileFragment extends Fragment
                                      implements Route.Listener {
  private LinearLayout graphHolder_;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle savedInstanceState) {
    final View elevation = inflater.inflate(R.layout.elevation, container, false);
    graphHolder_ = (LinearLayout)elevation.findViewById(R.id.graphview);
    return elevation;
  } // onCreateView

  @Override
  public void onResume() {
    super.onResume();
    Route.onResume();
    Route.registerListener(this);
  } // onResume

  @Override
  public void onPause() {
    Route.unregisterListener(this);
    super.onPause();
  } // onPause

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    drawGraph(journey);
    drawText(journey);
  } // onNewJourney

  @Override
  public void onResetJourney() {
  } // onResetJourney

  private void drawGraph(final Journey journey) {
    final LineGraphView graph = new LineGraphView(getActivity(), "");

    List<GraphView.GraphViewData> data = new ArrayList<>();
    for (Elevation elevation : journey.elevation().profile())
      data.add(new GraphView.GraphViewData(elevation.distance(), elevation.elevation()));

    GraphViewSeries graphSeries = new GraphViewSeries(data.toArray(new GraphView.GraphViewData[]{}));

    graph.addSeries(graphSeries);
    graph.setDrawBackground(true);
    graph.getGraphViewStyle().setGridStyle(GraphViewStyle.GridStyle.HORIZONTAL);
    graph.getGraphViewStyle().setNumHorizontalLabels(5);
    graph.getGraphViewStyle().setNumVerticalLabels(4);

    final ElevationFormatter formatter = ElevationFormatter.formatter(CycleStreetsPreferences.units());
    graph.setCustomLabelFormatter(new CustomLabelFormatter() {
      @Override
      public String formatLabel(double value, boolean isValueX) {
        if (isValueX)
          return (value != 0) ? formatter.distance((int)value) : "";
        return formatter.height((int) value);
      }
    });

    graphHolder_.removeAllViews();
    graphHolder_.addView(graph);
  } // drawGraph

  private void drawText(final Journey journey) {
    Segment.Start start = journey.segments().first();

    setText(R.id.title, journey.name());
    setText(R.id.journeyid, String.format("#%,d", journey.itinerary()));
    setText(R.id.routetype, initCap(journey.plan()) + " route:");
    setText(R.id.distance, distance(journey.total_distance()));
    setText(R.id.journeytime, start.totalTime());
    setText(R.id.calories, start.calories());
    setText(R.id.carbondioxide, start.co2());
  } // drawText

  private void setText(int id, String text) { ((TextView)getView().findViewById(id)).setText(text); }
  private String distance(final int metres) {
    return DistanceFormatter.formatter(CycleStreetsPreferences.units()).total_distance(metres);
  }
} // ElevationProfileFragment