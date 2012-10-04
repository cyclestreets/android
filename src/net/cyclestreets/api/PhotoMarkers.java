package net.cyclestreets.api;

import net.cyclestreets.R;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class PhotoMarkers 
{
  final private Drawable[] markers_;
  final private Drawable defaultMarker_;
  final private Point defaultMarkerHotspot_;

  public PhotoMarkers(final Resources res) 
  {
    defaultMarker_ = res.getDrawable(R.drawable.icon);
    defaultMarkerHotspot_ = new Point(13,47);

    markers_ = new Drawable[] {
      res.getDrawable(R.drawable.icon),            // 0
      res.getDrawable(R.drawable.mm_20_white_wisp),      // 1
      res.getDrawable(R.drawable.mm_20_bike),          // 2
      res.getDrawable(R.drawable.mm_20_sheffield_stands),    // 3
      res.getDrawable(R.drawable.mm_20_cycleway),        // 4
      res.getDrawable(R.drawable.mm_20_directional_signage),  // 5
      res.getDrawable(R.drawable.mm_20_general_sign),      // 6
      res.getDrawable(R.drawable.icon),            // 7
      res.getDrawable(R.drawable.mm_20_obstruction),      // 8
      res.getDrawable(R.drawable.mm_20_destination),      // 9
      res.getDrawable(R.drawable.mm_20_black),        // 10
      res.getDrawable(R.drawable.mm_20_spanner),        // 11
      res.getDrawable(R.drawable.mm_20_car_parking),      // 12
      res.getDrawable(R.drawable.mm_20_enforcement),      // 13
      res.getDrawable(R.drawable.mm_20_roadworks),      // 14
      res.getDrawable(R.drawable.mm_20_cone),          // 15
      res.getDrawable(R.drawable.mm_20_congestion),      // 16
      res.getDrawable(R.drawable.mm_20_road),          // 17
    };
  } // PhotoMarkers
  
  public Drawable getMarker(int feature) 
  {
    if((feature < 0) || (feature >= markers_.length))
      return defaultMarker_;

    return markers_[feature];
  } // getMarker

  public Point getMarkerHotspot(int feature) 
  {
    return defaultMarkerHotspot_;
  } // getMarkerHotspot
} // class PhotoMarkers
