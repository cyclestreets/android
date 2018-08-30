package net.cyclestreets.api.client

import com.bazaarvoice.jolt.Chainr
import com.bazaarvoice.jolt.JsonUtils

import fr.arnaudguyon.xmltojsonlib.XmlToJson

fun fromV1ApiXml(inputXml: String): String {
    val xmlToJson = XmlToJson.Builder(inputXml).build()
    val intermediateJson = xmlToJson.toString()
    return joltTransform(intermediateJson, V1API_XML_JOLT_SPEC)
}

fun fromV1ApiJson(inputJson: String): String {
    return joltTransform(inputJson, V1API_JSON_JOLT_SPEC)
}

// Uses https://github.com/bazaarvoice/jolt to transform JSON strings, without
// the need for us to define intermediate object representations.
private fun joltTransform(inputJson: String, specJson: String): String {
    val inputJsonObject = JsonUtils.jsonToObject(inputJson)

    val chainrSpecJson = JsonUtils.jsonToList(specJson)
    val chainr = Chainr.fromSpec(chainrSpecJson)

    val transformedOutput = chainr.transform(inputJsonObject)
    return JsonUtils.toJsonString(transformedOutput)
}

private const val V1API_XML_JOLT_SPEC = """[{
  "operation": "shift",
  "spec": {
    "markers": {
      "waypoint": { "@": "waypoints" },
      "marker": {
        "*": {
          "type": {
            "route": { "@(2)": "route" },
            "segment": { "@(2)": "segments[]" }
          }
        }
      }
    }
  }
}]
"""

private const val V1API_JSON_JOLT_SPEC = """[{
  "operation": "shift",
  "spec": {
    "waypoint": {
      "*": { "\\@attributes": "waypoints" }
    },
    "marker": {
      "*": {
        "\\@attributes": {
          "type": {
            "route": { "@(2)": "route" },
            "segment": { "@(2)": "segments[]" }
          }
        }
      }
    }
  }
}]"""
