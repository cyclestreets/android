package net.cyclestreets.api.client.dto;

import net.cyclestreets.api.POI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// POJO representation of the FeatureCollection type returned by the CycleStreets API.
public class FeatureCollection {

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

  // Instantiated by GSON
  private FeatureCollection() {}

  private List<Feature> features;

  private class Feature {
    private Properties properties;
    private Geometry geometry;

    private POI toPoi() {
      return new POI(Integer.parseInt(properties.id),
                     properties.name,
                     properties.notes,
                     properties.website,
                     geometry.coordinates[1],
                     geometry.coordinates[0]);
    }
  }

  private class Properties {
    private String id;
    private String name;
    private Map<String, String> osmTags;
    private String notes;
    private String website;
  }

  private class Geometry {
    private Double[] coordinates = { Double.MIN_VALUE, Double.MIN_VALUE};
  }

  public List<POI> toPoiList() {
    List<POI> pois = new ArrayList<>();
    for (Feature feat : features) {
      pois.add(feat.toPoi());
    }
    return pois;
  }
}
