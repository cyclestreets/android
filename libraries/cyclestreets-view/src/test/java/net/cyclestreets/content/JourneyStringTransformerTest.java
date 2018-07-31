package net.cyclestreets.content;

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

    static String expectedJson;

    @BeforeClass
    public static void setup() throws IOException {
        expectedJson = TestUtils.fromResourceFile("journey-domain.json");
    }

    @Test
    public void fromV1ApiXmlTest() throws IOException, JSONException {
        String inputXml = TestUtils.fromResourceFile("journey-v1api.xml");
        String outputJson = JourneyStringTransformer.fromV1ApiXml(inputXml);
        System.out.println(outputJson);
        JSONAssert.assertEquals(expectedJson, outputJson, JSONCompareMode.STRICT);
    }

    @Ignore
    @Test
    public void fromV1ApiJsonTest() throws IOException, JSONException {
        String inputJson = TestUtils.fromResourceFile("journey-v1api.json");
        String outputJson = JourneyStringTransformer.fromV1ApiJson(inputJson);
        System.out.println(outputJson);
        JSONAssert.assertEquals(expectedJson, outputJson, JSONCompareMode.STRICT);
    }
}
