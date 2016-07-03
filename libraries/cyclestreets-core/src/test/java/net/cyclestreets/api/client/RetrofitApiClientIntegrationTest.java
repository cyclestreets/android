package net.cyclestreets.api.client;

import net.cyclestreets.api.Feedback;
import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.Photos;
import net.cyclestreets.api.Registration;
import net.cyclestreets.api.Signin;
import net.cyclestreets.api.Upload;
import net.cyclestreets.api.UserJourney;
import net.cyclestreets.api.UserJourneys;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    GeoPlaces geoPlaces = apiClient.geoCoder("High", 0.1, 52.2, 0.2, 52.3);
    for (GeoPlace place : geoPlaces) {
      System.out.println(place);
    }
  }

  @Test
  public void hitGetPOIsByBboxApi() throws Exception {
    List<POI> pois = apiClient.getPOIs("bikeshops", 0.1, 52.2, 0.2, 52.3);
    System.out.println(pois);
  }

  @Test
  public void hitGetPOIsByRadiusApi() throws Exception {
    List<POI> pois = apiClient.getPOIs("bikeshops", -1, 54, 100);
    System.out.println(pois);
  }

  @Test
  public void hitGetPhotosApi() throws Exception {
    Photos photos = apiClient.getPhotos(0.1, 52.2, 0.2, 52.3);
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
    // Apologies for the test users that this method generates - we should probably delete them...
    String random = String.valueOf(new Random().nextInt(100000));
    System.out.println("Registering user test" + random);
    Registration.Result result = apiClient.register("test" + random, "pwd1234", "friendlyname", "test" + random + "@nosuchdomain.com");
    System.out.println(result.ok());
    System.out.println(result.message());
    assertThat(result.ok(), is(true));
  }

  @Test
  public void hitAuthenticateApi() throws Exception {
    Signin.Result result = apiClient.authenticate("test66137", "pwd1234");
    System.out.println(result.ok());
    System.out.println(result.name());
    System.out.println(result.email());
    System.out.println(result.error());
    assertThat(result.ok(), is(true));
  }

  @Test
  public void hitSendFeedbackApi() throws Exception {
    Feedback.Result result = apiClient.sendFeedback(1234, "test comment", "test", "test@nosuchdomain.com");
    System.out.println(result.ok());
    System.out.println(result.message());
    assertThat(result.ok(), is(true));
  }

  @Test
  public void hitUploadPhotoApiWithoutPhoto() throws Exception {
    Upload.Result result = apiClient.uploadPhoto("test66137", "pwd1234", 0, 52, 1467394411,
            "cycleparking", "good", "Caption: THIS IS TEST DATA and should not be on the map", null);
    System.out.println(result.ok());
    System.out.println(result.url());
    System.out.println(result.error());
    // Important - remove the test data from the map, otherwise we look pretty unprofessional!
    System.out.println("Don't forgot to log on as this user and delete the photo afterwards...");
  }

  @Test
  public void hitUploadPhotoApiWithPhoto() throws Exception {
    Upload.Result result = apiClient.uploadPhoto("test66137", "pwd1234", 0, 52, 1467394411,
            "cycleparking", "good", "Caption: THIS IS TEST DATA and should not be on the map", "/tmp/test-image.png");
    System.out.println(result.ok());
    System.out.println(result.url());
    System.out.println(result.error());
    // Important - remove the test data from the map, otherwise we look pretty unprofessional!
    System.out.println("Don't forgot to log on as this user and delete the photo afterwards...");
  }
}
