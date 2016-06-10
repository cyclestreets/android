package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osmdroid.api.IGeoPoint;

import android.graphics.drawable.Drawable;

import net.cyclestreets.api.json.JsonArrayHandler;
import net.cyclestreets.api.json.JsonObjectHandler;
import net.cyclestreets.api.json.JsonRootHandler;
import net.cyclestreets.api.json.JsonRootObjectHandler;
import net.cyclestreets.api.json.JsonStringHandler;

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
  
  public static Factory<List<POI>> factory() {
    return new POIFactory();
  } // factory

  private static class POIFactory extends Factory.JsonProcessor<List<POI>> {
    private List<POI> pois_;
    private int id_;
    private String name_;
    private String notes_;
    private String url_;
    private double lat_;
    private double lon_;

/*
{
    "type": "FeatureCollection",
    "features": [
        {
            "type": "Feature",
            "properties": {
                "id": "943984",
                "name": "(Name not known)",
                "osmTags": {
                    "amenity": "bicycle_parking",
                    "capacity": "76",
                    "covered": "no",
                    "source": "survey"
                }
            },
            "geometry": {
                "type": "Point",
                "coordinates": [
                    0.118357,
                    52.205166
                ]
            }
        }
    ]
}
 */

    @Override
    protected JsonRootHandler rootHandler() {
      pois_ = new ArrayList<>();
      
      final JsonRootHandler root = new JsonRootObjectHandler();
      final JsonObjectHandler feature = root.getArray("features").getObject();
      feature.setBeginObjectListener(new JsonObjectHandler.BeginListener() {
        @Override
        public void begin(String name) {
          id_ = 0;
          name_ = null;
          notes_ = null;
          url_ = null;
          lat_ = Double.MIN_VALUE;
          lon_ = Double.MIN_VALUE;
        } // start
      });
      feature.setEndObjectListener(new JsonObjectHandler.EndListener() {
        public void end() {
          pois_.add(new POI(id_, name_, notes_, url_, lat_, lon_));
        }
      });

      final JsonObjectHandler properties = feature.getObject("properties");
      properties.getString("id").setListener(new JsonStringHandler.Listener() {
        public void string(String name, String value) {
          id_ = Integer.parseInt(value);
        }
      });
      properties.getString("name").setListener(new JsonStringHandler.Listener(){
        public void string(String name, String value) {
          name_ = value;
        }
      });
      properties.getString("notes").setListener(new JsonStringHandler.Listener() {
        public void string(String name, String value) {
          notes_ = value;
        }
      });
      properties.getString("website").setListener(new JsonStringHandler.Listener() {
        public void string(String name, String value) {
          url_ = value;
        }
      });

      final JsonArrayHandler coords = feature.getObject("geometry").getArray("coordinates");
      coords.getString().setListener(new JsonStringHandler.Listener() {
        @Override
        public void string(String name, String value) {
          Double v = Double.parseDouble(value);
          if (lon_ == Double.MIN_VALUE)
            lon_ = v;
          else
            lat_ = v;
        }
      });
      return root;
    } // contentHandler

    @Override
    protected List<POI> get() { return pois_; } // get
  } // class POIFactory
} // class POICategory
