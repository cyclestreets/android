package net.cyclestreets.api.client;

import android.content.Context;

import net.cyclestreets.api.*;
import net.cyclestreets.core.R;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


// Useful for manual testing that operations do work with the real API, and not just WireMock.
@Ignore("Only meant for manual testing")
@Config(manifest=Config.NONE, sdk = 28)
@RunWith(RobolectricTestRunner.class)
public class RetrofitApiClientIntegrationTest {

  RetrofitApiClient apiClient;

  @Before
  public void setUp() throws Exception {
    Context testContext = mock(Context.class);
    when(testContext.getCacheDir()).thenReturn(new File("/tmp"));
    apiClient = new RetrofitApiClient.Builder()
        .withApiKey(getApiKey())
        .withContext(testContext)
        .withV1Host("https://www.cyclestreets.net")
        .withV2Host("https://api.cyclestreets.net")
        .withBlogHost("https://www.cyclestreets.org")
        .build();

    when(testContext.getString(R.string.feedback_ok)).thenReturn("Thank you for submitting this feedback. We will get back to you when we have checked this out.");
    when(testContext.getString(R.string.feedback_error_prefix)).thenReturn("Your feedback could not be sent.\n\n");
    when(testContext.getString(R.string.registration_ok)).thenReturn("Your account has been registered.\n\nAn email has been sent to the address you gave.\n\nWhen the email arrives, follow the instructions it contains to complete the registration.");
    when(testContext.getString(R.string.registration_error_prefix)).thenReturn("Your account could not be registered.\n\n");
    when(testContext.getString(R.string.signin_ok)).thenReturn("You have successfully signed into CycleStreets.");
    when(testContext.getString(R.string.signin_error_prefix)).thenReturn("Error : ");
    when(testContext.getString(R.string.signin_default_error)).thenReturn("Could not sign into CycleStreets.  Please check your username and password.");
    when(testContext.getString(R.string.upload_ok)).thenReturn("Your photo was uploaded successfully.");
    when(testContext.getString(R.string.upload_error_prefix)).thenReturn("There was a problem uploading your photo: \n");
    ApiClient.INSTANCE.initialiseForTests(testContext, new ApiClientImpl(apiClient));
  }

  private String getApiKey() throws IOException {
    String apiKey = "apiKeyRedacted";
    InputStream in = RetrofitApiClientIntegrationTest.class.getClassLoader().getResourceAsStream("cyclestreets-api.key");
    if (in != null) {
      try {
        apiKey = IOUtils.toString(in, "UTF-8").trim();
        System.out.println("Loaded api Key '" + apiKey + "' from api.key");
      } catch (IOException e) {
        // Give up and use default
        System.out.println("Failed to load API key from api.key - use default");
      } finally {
        in.close();
      }
    } else {
      System.out.println("No api.key found to run integration test - use default");
    }
    return apiKey;
  }

  @Test
  public void hitGeoCoderApi() throws Exception {
    GeoPlaces geoPlaces = apiClient.geoCoder("High", 0.1, 52.2, 0.2, 52.3);
    for (GeoPlace place : geoPlaces) {
      System.out.println(place);
    }
  }

  @Test
  public void hitGetPOICategoriesApi() throws Exception {
    POICategories poiCategories = apiClient.getPOICategories();
    for (POICategory category : poiCategories) {
      System.out.println(category.getName() + ": " + category);
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
  public void hitGetPhotoApi() throws Exception {
    Photos photos = apiClient.getPhoto(93348);
    for (Photo photo : photos) {
      System.out.println(photo);
    }
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
    Result result = apiClient.register("test" + random, "pwd1234", "friendlyname", "test" + random + "@nosuchdomain.com");
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
    System.out.println(result.message());
    assertThat(result.ok(), is(true));
  }

  @Test
  public void hitSendFeedbackApi() throws Exception {
    Result result = apiClient.sendFeedback(1234, "test comment", "test", "test@nosuchdomain.com");
    System.out.println(result.ok());
    System.out.println(result.message());
    assertThat(result.ok(), is(true));
  }

  @Test
  public void hitGetPhotomapCategoriesApi() throws Exception {
    PhotomapCategories categories = apiClient.getPhotomapCategories();
    System.out.println("categories: " + categories.categories());
    System.out.println("meta-categories: " + categories.metaCategories());
  }

  @Test
  public void hitUploadPhotoApiWithoutPhoto() throws Exception {
    Upload.Result result = apiClient.uploadPhoto("test66137", "pwd1234", 0, 52, 1467394411,
            "cycleparking", "good", "Caption: THIS IS TEST DATA and should not be on the map", null);
    System.out.println(result.ok());
    System.out.println(result.url());
    System.out.println(result.message());
    // Important - remove the test data from the map, otherwise we look pretty unprofessional!
    System.out.println("Don't forgot to log on as this user and delete the photo afterwards...");
  }

  @Test
  public void hitUploadPhotoApiWithPhoto() throws Exception {
    Upload.Result result = apiClient.uploadPhoto("test66137", "pwd1234", 0, 52, 1467394411,
            "cycleparking", "good", "Caption: THIS IS TEST DATA and should not be on the map", "/tmp/test-image.png");
    System.out.println(result.ok());
    System.out.println(result.url());
    System.out.println(result.message());
    // Important - remove the test data from the map, otherwise we look pretty unprofessional!
    System.out.println("Don't forgot to log on as this user and delete the photo afterwards...");
  }

  @Test
  public void hitGetJourneyJsonApi() throws Exception {
    String json = apiClient.getJourneyJson("quietest", "0.117950,52.205302,City+Centre|0.131402,52.221046,Mulberry+Close|0.147324,52.199650,Thoday+Street", null, null, 24);
    System.out.println(json);
  }

  @Test
  public void hitRetrievePreviousJourneyJsonApi() throws Exception {
    String json = apiClient.retrievePreviousJourneyJson("fastest", 53135357);
    System.out.println(json);
  }

  @Test
  public void hitGetBlogEntriesApi() throws Exception {
    Blog blog = apiClient.getBlogEntries();
    System.out.println(blog.toHtml());
  }
}
