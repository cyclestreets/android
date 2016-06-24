package net.cyclestreets.api.client;

import net.cyclestreets.api.POI;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.Photos;
import net.cyclestreets.api.UserJourney;
import net.cyclestreets.api.UserJourneys;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

// Useful for manual testing that operations do work with the real API, and not just WireMock.
// If we assigned an appropriate api key, these tests could be expanded and un-ignored.
@Ignore
public class RetrofitApiClientIntegrationTest {

  RetrofitApiClient apiClient = new RetrofitApiClient.Builder()
          .withApiKey("apiKeyRedacted")
          .withV1Host("https://www.cyclestreets.net")
          .withV2Host("https://api.cyclestreets.net")
          .build();

  @Test
  public void hitGetPOIsByBboxApi() throws Exception {
    List<POI> pois = apiClient.getPOIs("bikeshops", 0.2, 0.1, 52.3, 52.2);
    System.out.println(pois);
  }

  @Test
  public void hitGetPOIsByRadiusApi() throws Exception {
    List<POI> pois = apiClient.getPOIs("bikeshops", -1, 54, 100);
    System.out.println(pois);
  }

  @Test
  public void hitGetPhotosApi() throws Exception {
    Photos photos = apiClient.getPhotos(0.2, 0.1, 52.3, 52.2);
    for (Photo photo : photos) {
      System.out.println(photo);
    }
  }

  @Test
  public void hitGetUserJourneysApi() throws Exception {
    UserJourneys userJourneys = apiClient.getUserJourneys("socrates");
    for (UserJourney journey : userJourneys) {
      System.out.println(journey);
    }
  }
}