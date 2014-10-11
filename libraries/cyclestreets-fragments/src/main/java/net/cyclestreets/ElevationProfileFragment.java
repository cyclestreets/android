package net.cyclestreets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.DistanceFormatter;
import net.cyclestreets.routing.Elevation;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;

import java.util.ArrayList;
import java.util.List;

public class ElevationProfileFragment extends Fragment
                                      implements Route.Listener {
  private LinearLayout graphHolder_;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle savedInstanceState) {
    final View elevation = inflater.inflate(R.layout.elevation, null);
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
  } // onNewJourney

  @Override
  public void onResetJourney() {
  } // onResetJourney

  private void drawGraph(final Journey journey) {
    final LineGraphView graph = new LineGraphView(getActivity(), "");

    DistanceFormatter formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());
    List<GraphView.GraphViewData> data = new ArrayList<>();
    for (Elevation elevation : journey.elevations())
      data.add(new GraphView.GraphViewData(elevation.distance(), elevation.elevation()));

    GraphViewSeries graphSeries = new GraphViewSeries(data.toArray(new GraphView.GraphViewData[]{}));

    graph.addSeries(graphSeries);
    graph.setDrawBackground(true);

    graphHolder_.removeAllViews();
    graphHolder_.addView(graph);
  } // drawGraph
} // ElevationProfileFragment