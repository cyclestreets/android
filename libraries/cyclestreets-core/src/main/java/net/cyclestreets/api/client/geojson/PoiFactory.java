package net.cyclestreets.api.client.geojson;

import android.text.TextUtils;

import net.cyclestreets.api.POI;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoiFactory {

  private PoiFactory() {}

  private static POI toPoi(Feature feature) {
    LngLatAlt coordinates = ((Point)feature.getGeometry()).getCoordinates();

    Map<String, String> osmTags = feature.getProperty("osmTags");
    if (osmTags == null)
      osmTags = new HashMap<>();
    String openingHours = osmTags.get("opening_hours");
    if (openingHours != null)
      openingHours = openingHours.replaceAll("; *", "\n");

    String website = feature.getProperty("website");
    if (TextUtils.isEmpty(website))
      website = osmTags.get("url");

    return new POI(Integer.parseInt(feature.getProperty("id")),
                   feature.getProperty("name"),
                   feature.getProperty("notes"),
                   website,
                   osmTags.get("phone"),
                   openingHours,
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
