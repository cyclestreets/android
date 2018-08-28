package net.cyclestreets.api.client;

import net.cyclestreets.TestUtils;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

@Config(manifest= Config.NONE, sdk = 23)
@RunWith(RobolectricTestRunner.class)
public class JourneyStringTransformerTest {

    private static String expectedJson;

    @BeforeClass
    public static void setup() throws IOException {
        // We want to validate that transforming from either V1 API (XML or JSON) results in the
        // same domain JSON.
        expectedJson = TestUtils.fromResourceFile("journey-domain.json");
    }

    @Test
    public void fromV1ApiXmlTest() throws IOException, JSONException {
        String inputXml = TestUtils.fromResourceFile("__files/journey-v1api.xml");
        String outputJson = JourneyStringTransformerKt.fromV1ApiXml(inputXml);
        JSONAssert.assertEquals(expectedJson, outputJson, JSONCompareMode.STRICT);
    }

    @Test
    public void fromV1ApiJsonTest() throws IOException, JSONException {
        String inputJson = TestUtils.fromResourceFile("__files/journey-v1api.json");
        String outputJson = JourneyStringTransformerKt.fromV1ApiJson(inputJson);
        JSONAssert.assertEquals(expectedJson, outputJson, JSONCompareMode.STRICT);
    }

    /**
     * To get the domain JSON for a particular journey:
     *
     * 1.  Hit https://www.cyclestreets.net/api/journey.json?plan=balanced&itinerary=itineraryId&key=redacted
     * 2.  Copy and paste the JSON output into the inputJson variable below
     * 3.  Run the test, then copy the output into e.g. https://jsonformatter.org/json-pretty-print
     */
    @Ignore
    @Test
    public void getDomainJson() throws IOException, JSONException {
        String inputJson = "";
        String outputJson = JourneyStringTransformerKt.fromV1ApiJson(inputJson);
        System.out.println(outputJson);
    }
}
