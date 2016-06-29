package net.cyclestreets.api.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.Photos;
import net.cyclestreets.api.Registration;
import net.cyclestreets.api.UserJourney;
import net.cyclestreets.api.UserJourneys;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Iterator;
import java.util.List;

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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class RetrofitApiClientTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8089);

  RetrofitApiClient apiClient = new RetrofitApiClient.Builder()
      .withApiKey("myApiKey")
      .withV1Host("http://localhost:8089")
      .withV2Host("http://localhost:8089")
      .build();

  @Test
  public void testGetPoisByBbox() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/v2/pois.locations"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("pois.json")));

    // when
    List<POI> pois = apiClient.getPOIs("bikeshops", 0.2, 0.1, 52.3, 52.2);

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/pois.locations"))
        .withQueryParam("type", equalTo("bikeshops"))
        .withQueryParam("bbox", equalTo("0.1,52.2,0.2,52.3"))
        .withQueryParam("fields", equalTo("id,name,notes,website,latitude,longitude"))
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

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/pois.locations"))
            .withQueryParam("type", equalTo("bikeshops"))
            .withQueryParam("longitude", equalTo("0.15"))
            .withQueryParam("latitude", equalTo("52.25"))
            .withQueryParam("radius", equalTo("100"))
            .withQueryParam("limit", equalTo("150"))
            .withQueryParam("fields", equalTo("id,name,notes,website,latitude,longitude"))
            .withQueryParam("key", matching("myApiKey")));
    validatePois(pois);
  }

  private static void validatePois(List<POI> pois) {
    assertThat(pois.size(), is(7));
    POI poi = pois.get(0);

    assertThat(poi.name(), is("Chris's Bikes"));
    assertThat(poi.id(), is(101399));
    assertThat(poi.notes(), is("The notes section"));
    assertThat(poi.url(), is("http://www.madeup.com"));
    assertThat(poi.position(), is(new GeoPoint(52.225338, 0.091919)));
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
            .withQueryParam("key", matching("myApiKey")));

    assertThat(journeys.size(), is(3));
    UserJourney journey = journeys.get(2);

    assertThat(journey.name(), is("Hedingham Close to Old Montague Street"));
    assertThat(journey.id(), is(43089395));
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
    Photos photos = apiClient.getPhotos(0.2, 0.1, 52.3, 52.2);

    // then
    verify(getRequestedFor(urlPathEqualTo("/v2/photomap.locations"))
            .withQueryParam("bbox", equalTo("0.1,52.2,0.2,52.3"))
            .withQueryParam("fields", equalTo("id,caption,categoryId,metacategoryId,hasVideo,videoFormats,thumbnailUrl,shortlink"))
            .withQueryParam("thumbnailsize", equalTo("640"))
            .withQueryParam("limit", equalTo("45"))
            .withQueryParam("key", matching("myApiKey")));

    Iterator<Photo> iterator = photos.iterator();
    iterator.next();
    iterator.next();
    iterator.next();
    Photo photo4 = iterator.next();

    assertThat(iterator.hasNext(), is(false));

    assertThat(photo4.id(), is(82169));
    assertThat(photo4.caption(), is("Link from Clerk Maxwell Road to the West Cambridge site"));
    assertThat(photo4.category(), is("cycleways"));
    assertThat(photo4.metacategory(), is("other"));
    assertThat(photo4.thumbnailUrl(), is("https://www.cyclestreets.net/location/82169/cyclestreets82169-size640.jpg"));
    assertThat(photo4.url(), is("http://cycle.st/p82169"));
    assertThat(photo4.position(), is(new GeoPoint(52.209908, 0.094543)));
    assertThat(photo4.isPlaceholder(), is(false));
    assertThat(photo4.hasVideos(), is(true));
    List<Video> videos = (List<Video>)photo4.videos();
    assertThat(videos.size(), is(2));
    Video video = videos.get(1);
    assertThat(video.url(), is("http://www.cyclestreets.net/location/20588/cyclestreets20588.flv"));
    assertThat(video.format(), is("flv"));
  }

  @Test
  public void testGeoCoder() throws Exception {
    // given
    stubFor(get(urlPathEqualTo("/api/geocoder.xml"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("geocoder.xml")));

    // when
    GeoPlaces geoPlaces = apiClient.geoCoder("High", 52.3, 52.2, 0.2, 0.1);

    // then
    verify(getRequestedFor(urlPathEqualTo("/api/geocoder.xml"))
            .withQueryParam("w", equalTo("0.1"))
            .withQueryParam("e", equalTo("0.2"))
            .withQueryParam("s", equalTo("52.2"))
            .withQueryParam("n", equalTo("52.3"))
            .withQueryParam("countrycodes", equalTo("gb,ie"))
            .withQueryParam("street", equalTo("High"))
            .withQueryParam("key", matching("myApiKey")));

    assertThat(geoPlaces.size(), is(5));
    GeoPlace place = geoPlaces.get(1);
    assertThat(place.name(), is("The High"));
    assertThat(place.near(), is("Essex, East of England"));
    assertThat(place.coord(), is((IGeoPoint)new GeoPoint(51.769678, 0.0939271)));
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
    Registration.Result result = apiClient.register("arnold", "cyberdyne101", "The Terminator", "101@skynet.com");

    // then
    verify(postRequestedFor(urlPathEqualTo("/v2/user.create"))
            .withHeader("Content-Type", matching("multipart/form-data; boundary=.*"))
            .withRequestBody(matching(".*name=\"username\".*arnold.*name=\"password\".*cyberdyne101.*name=\"name\".*The Terminator.*name=\"email\".*101@skynet.com.*"))
            .withQueryParam("key", matching("myApiKey")));

    assertThat(result.ok(), is(true));
    assertThat(result.message(), containsString("Your account has been registered"));
  }

  @Test
  public void testRegisterReturnsError() throws Exception {
    // given
    stubFor(post(urlPathEqualTo("/v2/user.create"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("registration-error.json")));

    // when
    Registration.Result result = apiClient.register("username", "pwd", "name", "email@bob.com");

    // then
    verify(postRequestedFor(urlPathEqualTo("/v2/user.create"))
            .withHeader("Content-Type", matching("multipart/form-data; boundary=.*"))
            .withQueryParam("key", matching("myApiKey")));

    assertThat(result.ok(), is(false));
    assertThat(result.message(), containsString("Your account could not be registered."));
  }
}