package net.cyclestreets.content;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import java.util.List;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

public class JourneyStringTransformer {

    private static final String V1API_XML_JOLT_SPEC =
            "[{\n" +
            "  \"operation\": \"shift\",\n" +
            "  \"spec\": {\n" +
            "    \"markers\": {\n" +
            "      \"waypoint\": { \"@\": \"waypoints\" },\n" +
            "      \"marker\": {\n" +
            "        \"*\": {\n" +
            "          \"type\": {\n" +
            "            \"route\": { \"@(2)\": \"route\" },\n" +
            "            \"segment\": { \"@(2)\": \"segments\" }\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}]\n";

    public static String fromV1ApiXml(String inputXml) {
        XmlToJson xmlToJson = new XmlToJson.Builder(inputXml).build();
        String basicJsonString = xmlToJson.toString();
        String transformedJson = joltTransform(basicJsonString, V1API_XML_JOLT_SPEC);
        return transformedJson;
    }

    public static String fromV1ApiJson(String inputJson) {
        return inputJson;
    }

    private static String joltTransform(String inputJson, String specJson) {
        Object inputJsonObject = JsonUtils.jsonToObject(inputJson);

        List chainrSpecJson = JsonUtils.jsonToList(specJson);
        Chainr chainr = Chainr.fromSpec(chainrSpecJson);

        Object transformedOutput = chainr.transform(inputJsonObject);
        String outputJson = JsonUtils.toJsonString(transformedOutput);
        System.out.println(outputJson);
        return outputJson;
    }
}
