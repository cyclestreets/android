package net.cyclestreets.api.client.geojson;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class GeoPlacesFactory {

  private GeoPlacesFactory() {}

  public static GeoPlaces toGeoPlaces(FeatureCollection featureCollection) {
    List<GeoPlace> places = new ArrayList<>();
    for (Feature feature : featureCollection.getFeatures()) {
      places.add(toGeoPlace(feature));
    }
    return new GeoPlaces(places);
  }

  private static GeoPlace toGeoPlace(Feature feature) {
    LngLatAlt coordinates = ((Point)feature.getGeometry()).getCoordinates();
    return new GeoPlace(new GeoPoint(coordinates.getLatitude(), coordinates.getLongitude()),
                        (String)feature.getProperty("name"),
                        (String)feature.getProperty("near"));
  }
}
