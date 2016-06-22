package net.cyclestreets.api.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import net.cyclestreets.api.POI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
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
  public void poisByBboxAPI() throws Exception {
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
  public void poisByRadius() throws Exception {
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
}
