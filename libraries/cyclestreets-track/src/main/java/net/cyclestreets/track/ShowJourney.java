package net.cyclestreets.track;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.cyclestreets.views.CycleMapView;

public class ShowJourney extends Activity {
	private CycleMapView mapView_;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    setContentView(R.layout.completed_journey);

    mapView_ = new CycleMapView(this, getClass().getName());
    mapView_.hideLocationButton();
    final RelativeLayout v = (RelativeLayout)findViewById(R.id.mapholder);
    v.addView(mapView_,
              new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                                              RelativeLayout.LayoutParams.FILL_PARENT));

    final Bundle cmds = getIntent().getExtras();
    final long journeyId = cmds.getLong("showtrip");
    final TripData trip = TripData.fetchTrip(this, journeyId);

    setText(R.id.journey_info, trip.info());
    setText(R.id.journey_purpose, trip.purpose());
    setText(R.id.journey_start, trip.fancyStart());

    // zoomToBoundingBox works better if setZoom first
    mapView_.getController().setZoom(14);
    mapView_.overlayPushTop(JourneyOverlay.CompletedJourneyOverlay(this, mapView_, trip));
  } // onCreate

  private void setText(final int id, final String text) {
    final TextView tv = (TextView)findViewById(id);
    tv.setText(text);
  } // setText
} // ShowJourney
