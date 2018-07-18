package net.cyclestreets.api;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

import net.cyclestreets.core.R;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PhotoMarkers {
  private final Resources res;
  private final Drawable defaultMarker;
  private final Map<String, Drawable> markers = new HashMap<>();
  private final BitmapFactory.Options bfo;

  public PhotoMarkers(final Resources res) {
    this.res = res;
    defaultMarker = ResourcesCompat.getDrawable(res, R.drawable.general_neutral, null);

    bfo = new BitmapFactory.Options();
    bfo.inTargetDensity = 240;
  }

  public Drawable getMarker(final Photo photo) {
    final String key = String.format("photomarkers/%s_%s.png", photo.category(), mapMetaCat(photo.metacategory()));

    if (!markers.containsKey(key)) {
      try {
        final InputStream asset = res.getAssets().open(key);
        final Bitmap bmp = BitmapFactory.decodeStream(asset);
        final Drawable marker = new BitmapDrawable(res, bmp);
        asset.close();
        markers.put(key, marker);
        return marker;
      } catch (Exception e) {
        markers.put(key, defaultMarker);
      }
    }

    return markers.get(key);
  }

  private String mapMetaCat(final String mc) {
    if ("good".equals(mc) || "bad".equals(mc))
      return mc;
    return "neutral";
  }
}
