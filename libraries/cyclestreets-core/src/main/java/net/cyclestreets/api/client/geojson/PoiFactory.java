package net.cyclestreets.api.client.geojson;

import net.cyclestreets.api.POI;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class PoiFactory {

  private PoiFactory() {}

  private static POI toPoi(Feature feature) {
    LngLatAlt coordinates = ((Point)feature.getGeometry()).getCoordinates();
    return new POI(Integer.parseInt((String)feature.getProperty("id")),
                   (String)feature.getProperty("name"),
                   (String)feature.getProperty("notes"),
                   (String)feature.getProperty("website"),
                   coordinates.getLatitude(),
                   coordinates.getLongitude());
  }

  public static List<POI> toPoiList(FeatureCollection featureCollection) {
    List<POI> pois = new ArrayList<>();
    for (Feature feature : featureCollection.getFeatures()) {
      pois.add(toPoi(feature));
    }
    return pois;
  }
}
