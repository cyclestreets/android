package net.cyclestreets.api;

import net.cyclestreets.core.R;
import net.cyclestreets.util.MapFactory;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import java.util.Map;

public class PhotoMarkers
{
  final private Map<String, Drawable> markers_;
  final private Drawable defaultMarker_;

  public PhotoMarkers(final Resources res)
  {
    defaultMarker_ = res.getDrawable(R.drawable.mm_20_white_wisp);

    markers_ = MapFactory.map("1", res.getDrawable(R.drawable.mm_20_white_wisp)).
                          map("2", res.getDrawable(R.drawable.mm_20_bike)).
                          map("3", res.getDrawable(R.drawable.mm_20_sheffield_stands)).
                          map("4", res.getDrawable(R.drawable.mm_20_cycleway)).
                          map("5", res.getDrawable(R.drawable.mm_20_directional_signage)).
                          map("6", res.getDrawable(R.drawable.mm_20_general_sign)).
                          map("7", defaultMarker_).
                          map("8", res.getDrawable(R.drawable.mm_20_obstruction)).
                          map("9", res.getDrawable(R.drawable.mm_20_destination)).
                          map("10", res.getDrawable(R.drawable.mm_20_black)).
                          map("11", res.getDrawable(R.drawable.mm_20_spanner)).
                          map("12", res.getDrawable(R.drawable.mm_20_car_parking)).
                          map("13", res.getDrawable(R.drawable.mm_20_enforcement)).
                          map("14", res.getDrawable(R.drawable.mm_20_roadworks)).
                          map("15", res.getDrawable(R.drawable.mm_20_cone)).
                          map("16", res.getDrawable(R.drawable.mm_20_congestion)).
                          map("17", res.getDrawable(R.drawable.mm_20_road)).
                          map("cycleparking", res.getDrawable(R.drawable.mm_20_sheffield_stands)).
                          map("obstructions", res.getDrawable(R.drawable.mm_20_obstruction)).
                          map("cycleways", res.getDrawable(R.drawable.mm_20_cycleway)).
                          map("road", res.getDrawable(R.drawable.mm_20_road)).
                          map("dutchcycleways", res.getDrawable(R.drawable.mm_20_cycleway)).
                          map("carparking", res.getDrawable(R.drawable.mm_20_car_parking)).
                          map("enforcement", res.getDrawable(R.drawable.mm_20_enforcement)).
                          map("routesigns", res.getDrawable(R.drawable.mm_20_directional_signage)).
                          map("signs", res.getDrawable(R.drawable.mm_20_general_sign)).
                          map("destinations", res.getDrawable(R.drawable.mm_20_destination)).
                          //map("potholes", ).
                          map("bikeshops", res.getDrawable(R.drawable.mm_20_spanner)).
                          map("roadworks", res.getDrawable(R.drawable.mm_20_roadworks)).
                          //map("closure", ).
                          map("bicycles", res.getDrawable(R.drawable.mm_20_bike)).
                          map("congestion", res.getDrawable(R.drawable.mm_20_congestion)).
                          map("general", defaultMarker_);
  } // PhotoMarkers

  public Drawable getMarker(final String featureName)
  {
    if(markers_.containsKey(featureName))
      return markers_.get(featureName);

    return defaultMarker_;
  } // getMarker
} // class PhotoMarkers
