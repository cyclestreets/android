package net.cyclestreets.api.client;

import net.cyclestreets.TestUtils;

import org.json.JSONException;
import org.junit.BeforeClass;
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
        String outputJson = JourneyStringTransformer.INSTANCE.fromV1ApiXml(inputXml);
        System.out.println(outputJson);
        JSONAssert.assertEquals(expectedJson, outputJson, JSONCompareMode.STRICT);
    }

    @Test
    public void fromV1ApiJsonTest() throws IOException, JSONException {
        String inputJson = TestUtils.fromResourceFile("__files/journey-v1api.json");
        String outputJson = JourneyStringTransformer.INSTANCE.fromV1ApiJson(inputJson);
        System.out.println(outputJson);
        JSONAssert.assertEquals(expectedJson, outputJson, JSONCompareMode.STRICT);
    }
}
