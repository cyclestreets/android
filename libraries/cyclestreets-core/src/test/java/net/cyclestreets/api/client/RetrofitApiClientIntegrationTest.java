package net.cyclestreets.api.client;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.Photos;
import net.cyclestreets.api.Registration;
import net.cyclestreets.api.UserJourney;
import net.cyclestreets.api.UserJourneys;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Random;

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
  public void hitGeoCoderApi() throws Exception {
    GeoPlaces geoPlaces = apiClient.geoCoder("High", 52.3, 52.2, 0.2, 0.1);
    for (GeoPlace place : geoPlaces) {
      System.out.println(place);
    }
  }

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

  @Test
  public void hitRegistrationApi() throws Exception {
    String random = String.valueOf(new Random().nextInt(100000));
    System.out.println("Registering user test" + random);
    Registration.Result result = apiClient.register("test" + random, "pwd1234", "friendlyname", "test" + random + "@nosuchdomain.com");
    System.out.println(result.ok());
    System.out.println(result.message());
  }
}