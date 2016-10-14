package net.cyclestreets.api;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import net.cyclestreets.core.R;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PhotoMarkers {
  private final Resources res_;
  private final Drawable defaultMarker_;
  private final Map<String, Drawable> markers_;
  private final BitmapFactory.Options bfo_;

  public PhotoMarkers(final Resources res) {
    res_ = res;
    defaultMarker_ = res.getDrawable(R.drawable.general_neutral);
    markers_ = new HashMap<>();

    bfo_ = new BitmapFactory.Options();
    bfo_.inTargetDensity = 240;
  } // PhotoMarkers

  public Drawable getMarker(final Photo photo) {
    final String key = String.format("photomarkers/%s_%s.png", photo.category(), mapMetaCat(photo.metacategory()));

    if (!markers_.containsKey(key)) {
      try {
        final InputStream asset = res_.getAssets().open(key);
        final Bitmap bmp = BitmapFactory.decodeStream(asset);
        final Drawable marker = new BitmapDrawable(res_, bmp);
        asset.close();
        markers_.put(key, marker);
        return marker;
      } catch (Exception e) {
        markers_.put(key, defaultMarker_);
      }
    }

    return markers_.get(key);
  } // getMarker

  private String mapMetaCat(final String mc) {
    if ("good".equals(mc) || "bad".equals(mc))
      return mc;
    return "neutral";
  } // mapMetaCat
} // class PhotoMarkers
