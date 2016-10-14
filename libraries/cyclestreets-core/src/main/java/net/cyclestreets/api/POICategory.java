package net.cyclestreets.api;

import android.graphics.drawable.Drawable;

import org.osmdroid.api.IGeoPoint;

import java.util.Collections;
import java.util.List;

public class POICategory {
  private final String key_;
  private final String name_;
  private final Drawable icon_;
  
  public POICategory(final String key,
                     final String name,
                     final Drawable icon) {
    key_ = key;
    name_ = name;
    icon_ = icon;
  } // POICategory
  
  public String name() { return name_; }
  public Drawable icon() { return icon_; }
  
  public boolean equals(final Object rhs) {
    if(!(rhs instanceof POICategory))
      return false;
    
    return name_.equals(((POICategory)rhs).name_);
  } // equals

  public List<POI> pois(final IGeoPoint centre,
                        final int radius)
    throws Exception {
    try {
      final List<POI> pois = ApiClient.getPOIs(key_, 
                                               centre.getLongitudeE6() / 1E6,
                                               centre.getLatitudeE6() / 1E6,
                                               radius);
      for(final POI poi : pois)
        poi.setCategory(this);
      return pois;
    } catch(Exception e) {
      return Collections.emptyList();
    } // catch
  } // pois
} // class POICategory
