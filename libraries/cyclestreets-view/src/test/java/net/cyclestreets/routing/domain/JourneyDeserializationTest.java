package net.cyclestreets.routing.domain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import net.cyclestreets.TestUtils;

import org.junit.Before;
import org.junit.Test;
import org.osmdroid.api.IGeoPoint;

import java.io.IOException;

public class JourneyDeserializationTest {

  private final ObjectMapper om = new ObjectMapper();

  @Before
  public void setup() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(IGeoPoint.class, new GeoPointDeserializer());
    om.registerModule(module);
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Test
  public void basicTest() throws IOException {
    String rawJson = TestUtils.fromResourceFile("journey-domain.json");
    JourneyDomainObject jdo = om.readValue(rawJson, JourneyDomainObject.class);
    System.out.println(jdo);
  }
}
