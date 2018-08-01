package net.cyclestreets.api.client;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import java.util.List;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

public class JourneyStringTransformer {

    public static String fromV1ApiXml(String inputXml) {
        XmlToJson xmlToJson = new XmlToJson.Builder(inputXml).build();
        String intermediateJson = xmlToJson.toString();
        return joltTransform(intermediateJson, V1API_XML_JOLT_SPEC);
    }

    public static String fromV1ApiJson(String inputJson) {
        return joltTransform(inputJson, V1API_JSON_JOLT_SPEC);
    }

    // Uses https://github.com/bazaarvoice/jolt to transform JSON strings, without
    // the need for us to define intermediate object representations.
    private static String joltTransform(String inputJson, String specJson) {
        Object inputJsonObject = JsonUtils.jsonToObject(inputJson);

        List chainrSpecJson = JsonUtils.jsonToList(specJson);
        Chainr chainr = Chainr.fromSpec(chainrSpecJson);

        Object transformedOutput = chainr.transform(inputJsonObject);
        return JsonUtils.toJsonString(transformedOutput);
    }

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

    private static final String V1API_JSON_JOLT_SPEC =
            "[{\n" +
                    "  \"operation\": \"shift\",\n" +
                    "  \"spec\": {\n" +
                    "    \"waypoint\": {\n" +
                    "      \"*\": { \"\\\\@attributes\": \"waypoints\" }\n" +
                    "    },\n" +
                    "    \"marker\": {\n" +
                    "      \"*\": {\n" +
                    "        \"\\\\@attributes\": {\n" +
                    "          \"type\": {\n" +
                    "            \"route\": { \"@(2)\": \"route\" },\n" +
                    "            \"segment\": { \"@(2)\": \"segments\" }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}]";
}
