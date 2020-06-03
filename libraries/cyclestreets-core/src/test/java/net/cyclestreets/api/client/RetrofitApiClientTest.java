package net.cyclestreets.api.client;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Blog;
import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.POICategories;
import net.cyclestreets.api.POICategory;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.PhotomapCategories;
import net.cyclestreets.api.PhotomapCategory;
import net.cyclestreets.api.Photos;
import net.cyclestreets.api.Result;
import net.cyclestreets.api.Signin;
import net.cyclestreets.api.Upload;
import net.cyclestreets.api.UserJourney;
import net.cyclestreets.api.UserJourneys;
import net.cyclestreets.core.R;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static net.cyclestreets.api.Photo.Video;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Config(manifest=Config.NONE, sdk = 28)
@RunWith(RobolectricTestRunner.class)
public class RetrofitApiClientTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8089);

  private RetrofitApiClient apiClient;

  private Context testContext;
  private Resources testResources;

  @Before
  public void setUp() throws Exception {
    // Delete the cache so that we can test cached endpoints are hit once!
    File cacheDirFile = new File("/tmp/RetrofitApiClientCache");
    FileUtils.deleteDirectory(cacheDirFile);

    testContext = mock(Context.class);
    testResources = mock(Resources.class);
    when(testContext.getResources()).thenReturn(testResources);
    when(testContext.getCacheDir()).thenReturn(new File("/tmp"));
    apiClient = new RetrofitApiClient.Builder()
        .withApiKey("myApiKey")
        .withContext(testContext)
        .withV1Host("http://localhost:8089")
        .withV2Host("http://localhost:8089")
        .withBlogHost("http://localhost:8089")
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
    // Initialise API Client messages without doing full initialise
    ApiClient.INSTANCE.initMessages(testContext);
  }

  @Test
  public void testGetPoiCategories() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/v2/pois.types"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withHeader("Cache-Control", "public, max-age=604800")
                    .withBodyFile("pois-types.json")));
    when(testResources.getResourcePackageName(R.drawable.poi_bedsforcyclists)).thenReturn("drawable-xxhdpi");
    when(testResources.getDrawable(anyInt(), eq(null))).thenReturn(mock(Drawable.class));

    // when
    POICategories poiCategories = apiClient.getPOICategories();

    // call the endpoint 5 more times
    for (int ii = 0; ii < 5; ii++) {
      apiClient.getPOICategories();
    }

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/pois.types"))
            .withQueryParam("key", equalTo("myApiKey")));
    assertThat(poiCategories.count()).isEqualTo(52);
    POICategory category = poiCategories.get(37);
    assertThat(category.getName()).isEqualTo("Supermarkets");
    assertThat(category.getIcon()).isNotNull();

    // caching should mean the REST request is only made once
    List<LoggedRequest> requests = findAll(getRequestedFor(urlPathEqualTo("/v2/pois.types")));
    assertThat(requests).hasSize(1);
  }

  @Test
  public void testGetPoisByBbox() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/v2/pois.locations"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("pois.json")));

    // when
    List<POI> pois = apiClient.getPOIs("bikeshops", 0.1, 52.2, 0.2, 52.3);

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/pois.locations"))
        .withQueryParam("type", equalTo("bikeshops"))
        .withQueryParam("bbox", equalTo("0.1,52.2,0.2,52.3"))
        .withQueryParam("fields", equalTo("id,latitude,longitude,name,notes,osmTags,website"))
        .withQueryParam("key", equalTo("myApiKey")));
    validatePois(pois);
  }

  @Test
  public void testGetPoisByRadius() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/v2/pois.locations"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("pois.json")));

    // when
    List<POI> pois = apiClient.getPOIs("bikeshops", 0.15, 52.25, 100);

    // call the endpoint 5 more times
    for (int ii = 0; ii < 5; ii++) {
      apiClient.getPOIs("bikeshops", 0.15, 52.25, 100);
    }

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/pois.locations"))
            .withQueryParam("type", equalTo("bikeshops"))
            .withQueryParam("longitude", equalTo("0.15"))
            .withQueryParam("latitude", equalTo("52.25"))
            .withQueryParam("radius", equalTo("100"))
            .withQueryParam("limit", equalTo("150"))
            .withQueryParam("fields", equalTo("id,latitude,longitude,name,notes,osmTags,website"))
            .withQueryParam("key", equalTo("myApiKey")));
    validatePois(pois);

    // not cached - REST request will be made 6 times
    List<LoggedRequest> requests = findAll(getRequestedFor(urlPathEqualTo("/v2/pois.locations")));
    assertThat(requests).hasSize(6);
  }

  private static void validatePois(List<POI> pois) {
    assertThat(pois.size()).isEqualTo(7);

    // happy path
    POI poi = pois.get(0);
    assertThat(poi.id()).isEqualTo(101399);
    assertThat(poi.name()).isEqualTo("Chris's Bikes");
    assertThat(poi.notes()).isEqualTo("The notes section");
    assertThat(poi.phone()).isEqualTo("01234 567890");
    assertThat(poi.openingHours()).isEqualTo("Mo-Fr 09:00-17:00\nSa 10:00-18:00");
    assertThat(poi.url()).isEqualTo("http://www.madeup.com");
    assertThat(poi.position()).isEqualTo(new GeoPoint(52.225338, 0.091919));

    // website provided within `osmTags.url`, but not in `website`
    poi = pois.get(6);
    assertThat(poi.id()).isEqualTo(113267);
    assertThat(poi.name()).isEqualTo("Bicycle Ambulance");
    assertThat(poi.notes()).isEqualTo("");
    assertThat(poi.phone()).isEqualTo("");
    assertThat(poi.openingHours()).isEqualTo("Tu-Fr 08:30-18:00\nSa 10:00-18:00");
    assertThat(poi.url()).isEqualTo("http://bicycleambulance.com");
    assertThat(poi.position()).isEqualTo(new GeoPoint(52.209179, 0.120061));
  }

  @Test
  public void testGetUserJourneys() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/v2/journeys.user"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("journeys.json")));

    // when
    UserJourneys journeys = apiClient.getUserJourneys("socrates");

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/journeys.user"))
            .withQueryParam("username", equalTo("socrates"))
            .withQueryParam("format", equalTo("flat"))
            .withQueryParam("datetime", equalTo("friendly"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(journeys.size()).isEqualTo(3);
    UserJourney journey = journeys.get(2);

    assertThat(journey.name()).isEqualTo("Hedingham Close to Old Montague Street");
    assertThat(journey.id()).isEqualTo(43089395);
  }

  @Test
  public void testGetPhotos() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/v2/photomap.locations"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("photos.json")));

    // when
    Photos photos = apiClient.getPhotos(0.1, 52.2, 0.2, 52.3);

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/photomap.locations"))
            .withQueryParam("bbox", equalTo("0.1,52.2,0.2,52.3"))
            .withQueryParam("fields", equalTo("id,caption,datetime,categoryId,metacategoryId,hasVideo,videoFormats,thumbnailUrl,shortlink"))
            .withQueryParam("thumbnailsize", equalTo("640"))
            .withQueryParam("limit", equalTo("45"))
            .withQueryParam("key", equalTo("myApiKey")));

    Iterator<Photo> iterator = photos.iterator();
    iterator.next();
    iterator.next();
    iterator.next();
    Photo photo4 = iterator.next();

    assertThat(iterator.hasNext()).isFalse();

    assertThat(photo4.id()).isEqualTo(82169);
    assertThat(photo4.caption()).isEqualTo("Link from Clerk Maxwell Road to the West Cambridge site");
    assertThat(photo4.datetime()).isEqualTo(1466693269L);
    assertThat(photo4.category()).isEqualTo("cycleways");
    assertThat(photo4.metacategory()).isEqualTo("other");
    assertThat(photo4.thumbnailUrl()).isEqualTo("https://www.cyclestreets.net/location/82169/cyclestreets82169-size640.jpg");
    assertThat(photo4.url()).isEqualTo("https://cycle.st/p82169");
    assertThat(photo4.position()).isEqualTo(new GeoPoint(52.209908, 0.094543));
    assertThat(photo4.isPlaceholder()).isFalse();
    assertThat(photo4.hasVideos()).isTrue();
    List<Video> videos = (List<Video>)photo4.videos();
    assertThat(videos.size()).isEqualTo(2);
    Video video = videos.get(1);
    assertThat(video.url()).isEqualTo("https://www.cyclestreets.net/location/20588/cyclestreets20588.flv");
    assertThat(video.format()).isEqualTo("flv");
  }

  @Test
  public void testGeoCoder() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/v2/geocoder"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("geocoder.json")));

    // when
    GeoPlaces geoPlaces = apiClient.geoCoder("High", 0.1, 52.2, 0.2, 52.3);
    // call the endpoint 5 more times
    for (int ii = 0; ii < 5; ii++) {
      apiClient.geoCoder("High", 0.1, 52.2, 0.2, 52.3);
    }

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/geocoder"))
            .withQueryParam("bbox", equalTo("0.1,52.2,0.2,52.3"))
            .withQueryParam("countrycodes", equalTo("gb,ie"))
            .withQueryParam("q", equalTo("High"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(geoPlaces.size()).isEqualTo(5);
    GeoPlace place = geoPlaces.get(1);
    assertThat(place.name()).isEqualTo("The High");
    assertThat(place.near()).isEqualTo("Essex, East of England");
    assertThat(place.coord()).isEqualTo(new GeoPoint(51.769678, 0.0939271));

    // not cached - REST request will be made 6 times
    List<LoggedRequest> requests = findAll(getRequestedFor(urlPathEqualTo("/v2/geocoder")));
    assertThat(requests).hasSize(6);
  }

  @Test
  public void testRegisterReturnsOk() throws Exception {
    // given
    stubFor(post(urlPathEqualTo("/v2/user.create"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("registration-ok.json")));

    // when
    Result result = apiClient.register("arnold", "cyberdyne101", "The Terminator", "101@skynet.com");

    // then
    verify(postRequestedFor(urlPathEqualTo("/v2/user.create"))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withRequestBody(equalTo("username=arnold&password=cyberdyne101&name=The%20Terminator&email=101%40skynet.com"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(result.ok()).isTrue();
    assertThat(result.message()).contains("Your account has been registered");
  }

  @Test
  public void testRegisterReturnsError() throws Exception {
    // given
    stubFor(post(urlPathEqualTo("/v2/user.create"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-error.json")));

    // when
    Result result = apiClient.register("username", "pwd", "name", "email@bob.com");

    // then
    verify(postRequestedFor(urlPathEqualTo("/v2/user.create"))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(result.ok()).isFalse();
    assertThat(result.message()).contains("Your account could not be registered.");
  }

  @Test
  public void testAuthenticateReturnsOk() throws Exception {
    // given
    stubFor(post(urlPathEqualTo("/v2/user.authenticate"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("authenticate-ok.json")));

    // when
    Signin.Result result = apiClient.authenticate("precious", "9nazgul");

    // then
    verify(postRequestedFor(urlPathEqualTo("/v2/user.authenticate"))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withRequestBody(equalTo("identifier=precious&password=9nazgul"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(result.ok()).isTrue();
    assertThat(result.name()).isEqualTo("Bilbo Baggins");
    assertThat(result.email()).isEqualTo("bilbo@bag-end.com");
  }

  @Test
  public void testSendFeedbackReturnsOk() throws Exception {
    // given
    stubFor(post(urlPathEqualTo("/v2/feedback.add"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("feedback-ok.json")));

    // when
    Result result = apiClient.sendFeedback(1234, "Comments I want to make", "My Name", "ballboy@wimbledon.com");

    // then
    verify(postRequestedFor(urlPathEqualTo("/v2/feedback.add"))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withRequestBody(matching("type=routing&itinerary=1234&comments=Comments%20I%20want%20to%20make&name=My%20Name&email=ballboy%40wimbledon.com"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(result.ok()).isTrue();
    assertThat(result.message()).contains("Thank you for submitting this feedback");
  }

  @Test
  public void testGetPhotomapCategories() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/v2/photomap.categories"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withHeader("Cache-Control", "public, max-age=1209600")
                    .withBodyFile("photomap-categories.json")));

    // when
    PhotomapCategories categories = apiClient.getPhotomapCategories();

    // call the endpoint 5 more times
    for (int ii = 0; ii < 5; ii++) {
      apiClient.getPhotomapCategories();
    }

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/photomap.categories"))
            .withQueryParam("key", equalTo("myApiKey")));
    assertThat(categories.categories().size()).isEqualTo(18);
    assertThat(categories.metaCategories().size()).isEqualTo(5);
    PhotomapCategory category = categories.categories().get(12);
    assertThat(category.getTag()).isEqualTo("destinations");
    assertThat(category.getName()).isEqualTo("Destination");
    assertThat(category.getDescription()).isEqualTo("A place where you might want to visit.");
    PhotomapCategory metaCategory = categories.metaCategories().get(3);
    assertThat(metaCategory.getTag()).isEqualTo("any");
    assertThat(metaCategory.getName()).isEqualTo("Misc");
    assertThat(metaCategory.getDescription()).isEqualTo("Non-specific");

    // caching should mean the REST request is only made once
    List<LoggedRequest> requests = findAll(getRequestedFor(urlPathEqualTo("/v2/photomap.categories")));
    assertThat(requests).hasSize(1);
  }

  @Test
  public void testUploadPhotoReturnsOk() throws Exception {
    // given
    stubFor(post(urlPathEqualTo("/v2/photomap.add"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("upload-ok.json")));

    // when
    Upload.Result result = apiClient.uploadPhoto("arnold", "cyberdyne101", -0.5, 53, 12345678,
                                                 "scifi", "evilrobots", "The Cyberdyne Model 101",
                                                 null);

    // then
    verify(postRequestedFor(urlPathEqualTo("/v2/photomap.add"))
            .withHeader("Content-Type", matching("multipart/form-data; boundary=.*"))
            .withRequestBody(matching(".*username.*arnold.*password.*cyberdyne101.*longitude.*-0.5.*latitude.*53.*datetime.*12345678.*category.*scifi.*metacategory.*evilrobots.*caption.*The Cyberdyne Model 101.*"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(result.ok()).isTrue();
    assertThat(result.url()).isEqualTo("https://www.cyclestreets.net/location/64001/");
  }

  @Test
  public void testGetJourneyJson() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/api/journey.json"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/json")
                    .withBodyFile("journey-v1api.json")));

    // when
    String journeyJson = apiClient.getJourneyJson("balanced",
                                                  "mySetOfItineraryPoints",
                                                  "2016-07-03 07:51:12",
                                                  null,
                                                  24);
    // N.B. if you try putting a realistic set of itinerary points, Wiremock barfs at the presence
    //      of the unencoded pipe character (see https://github.com/square/retrofit/issues/1891).

    // then
    verify(getRequestedFor(urlPathEqualTo("/api/journey.json"))
            .withQueryParam("plan", equalTo("balanced"))
            .withQueryParam("itinerarypoints", equalTo("mySetOfItineraryPoints"))
            .withQueryParam("leaving", equalTo("2016-07-03 07:51:12"))
            .withQueryParam("speed", equalTo("24"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(journeyJson).isNotNull();
    assertThat(journeyJson).contains("{");
  }

  @Test
  public void testGetBlogEntries() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/news/feed/"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/rss+xml; charset=UTF-8")
                    .withBodyFile("blogfeed.xml")));

    // when
    Blog blog = apiClient.getBlogEntries();

    // call the endpoint 5 more times
    for (int ii = 0; ii < 5; ii++) {
      apiClient.getBlogEntries();
    }

    // then
    verify(getRequestedFor(urlPathEqualTo("/news/feed/"))
            .withQueryParam("key", equalTo("myApiKey")));

    assertThat(blog).isNotNull();
    assertThat(blog.mostRecentTitle()).isEqualTo("Cyclescape website redesign coming soon");
    assertThat(blog.mostRecent()).isEqualTo("Thu, 02 Jan 2020 20:25:56 +0000");

    // caching should mean the REST request is only made once
    List<LoggedRequest> requests = findAll(getRequestedFor(urlPathEqualTo("/news/feed/"))
            .withQueryParam("key", equalTo("myApiKey")));
    assertThat(requests).hasSize(1);
  }
}
