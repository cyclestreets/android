package net.cyclestreets.api;

import android.graphics.drawable.Drawable;

import org.osmdroid.api.IGeoPoint;

import java.util.Collections;
import java.util.List;

public class POICategory {
  private final String key;
  private final String name;
  private final Drawable icon;

  public POICategory(final String key,
                     final String name,
                     final Drawable icon) {
    this.key = key;
    this.name = name;
    this.icon = icon;
  }

  public String name() { return name; }
  public Drawable icon() { return icon; }

  public boolean equals(final Object rhs) {
    if (!(rhs instanceof POICategory))
      return false;

    return name.equals(((POICategory)rhs).name);
  }

  public List<POI> pois(final IGeoPoint centre,
                        final int radius)
    throws Exception {
    try {
      final List<POI> pois = ApiClient.getPOIs(key,
                                               centre.getLongitudeE6() / 1E6,
                                               centre.getLatitudeE6() / 1E6,
                                               radius);
      for(final POI poi : pois)
        poi.setCategory(this);
      return pois;
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}
