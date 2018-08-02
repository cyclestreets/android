package net.cyclestreets.routing.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;

public class GeoPointDeserializer extends JsonDeserializer<IGeoPoint> {
  @Override
  public IGeoPoint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.readValueAsTree();
    return new GeoPoint(node.get("latitude").asDouble(), node.get("longitude").asDouble());
  }
}
