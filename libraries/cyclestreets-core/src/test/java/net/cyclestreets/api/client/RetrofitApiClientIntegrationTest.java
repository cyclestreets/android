package net.cyclestreets.api.client;

import net.cyclestreets.api.POI;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

// Useful for manual testing that operations do work with the real API, and not just WireMock.
// If we assigned an appropriate api key, these tests could be expanded and un-ignored.
@Ignore
public class RetrofitApiClientIntegrationTest {

  RetrofitApiClient apiClient = new RetrofitApiClient("http://www.cyclestreets.net/", "https://api.cyclestreets.net", "apiKeyRedacted");

  @Test
  public void hitAnApi() throws Exception {
    List<POI> pois = apiClient.getPOIs("bikeshops", 0.2, 0.1, 52.3, 52.2);
    System.out.println(pois);
  }
}
