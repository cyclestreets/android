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

import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.DistanceFormatter;
import net.cyclestreets.routing.Elevation;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.routing.Waypoints;

import java.util.ArrayList;
import java.util.List;

public class ElevationProfileFragment extends Fragment
                                      implements Route.Listener {
  private LinearLayout graphHolder_;
  private TextView details_;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle savedInstanceState) {
    final View elevation = inflater.inflate(R.layout.elevation, container, false);
    graphHolder_ = (LinearLayout)elevation.findViewById(R.id.graphview);
    details_ = (TextView)elevation.findViewById(R.id.elevationdetails);
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

    final DistanceFormatter formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());
    graph.setCustomLabelFormatter(new CustomLabelFormatter() {
      @Override
      public String formatLabel(double value, boolean isValueX) {
        if (isValueX)
          return (value != 0) ? formatter.distance((int)value) : "";
        return formatter.distance((int)value);
      }
    });

    graphHolder_.removeAllViews();
    graphHolder_.addView(graph);
  } // drawGraph

  private void drawText(final Journey journey) {
    Segment start = journey.segments().first();
    details_.setText(start.street() + "\n" + start.extraInfo());
  }
} // ElevationProfileFragment