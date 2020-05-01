package net.cyclestreets.track;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.cyclestreets.views.CycleMapView;

import static net.cyclestreets.views.CycleMapView.DEFAULT_ZOOM_LEVEL;

public class ShowJourney extends Activity {
  private CycleMapView mapView_;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.completed_journey);

    mapView_ = new CycleMapView(this, getClass().getName());
    mapView_.hideLocationButton();
    final RelativeLayout v = findViewById(R.id.mapholder);
    v.addView(mapView_,
              new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                              RelativeLayout.LayoutParams.MATCH_PARENT));

    final Bundle cmds = getIntent().getExtras();
    final long journeyId = cmds.getLong("showtrip");
    final TripData trip = TripData.fetchTrip(this, journeyId);

    setText(R.id.journey_info, trip.info());
    setText(R.id.journey_purpose, trip.purpose());
    setText(R.id.journey_start, trip.fancyStart());

    // zoomToBoundingBox works better if setZoom first
    mapView_.getController().setZoom((double)DEFAULT_ZOOM_LEVEL);
    mapView_.overlayPushTop(JourneyOverlay.CompletedJourneyOverlay(this, mapView_, trip));
  }

  private void setText(final int id, final String text) {
    final TextView tv = findViewById(id);
    tv.setText(text);
  }
}
