package net.cyclestreets.api.client.geojson;

import org.geojson.Feature;

class AbstractObjectFactory {
  @SuppressWarnings("unchecked")
  protected static <V> V propertyOrDefault(Feature feature, String propertyName, V defaultValue) {
    return (feature.getProperty(propertyName) == null) ? defaultValue : (V)feature.getProperty(propertyName);
  }
}
