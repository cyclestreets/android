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
// For A-B route, input json will have array of waypoints.
// For circular route, input json has a single waypoint.
// For circular routes it may also have a single poi or an array of pois (or none at all).
// Convert these to arrays, do the transformation, then convert back to arrays if single
// Note (if debugging) that the waypoints in the output may appear at the beginning or in the original place,
// depending on whether they are an array of waypoints (A-B route) or a single item (circular route).
private const val V1API_JSON_JOLT_SPEC = """
[// First, convert single waypoint / poi to array
{
    "operation": "cardinality",
    "spec": {
    "waypoint": "MANY",
    "poi": "MANY"
    }
},
// Now do the transformation, but note that 1-element arrays will get converted back to strings...
{
  "operation": "shift",
  "spec": {
    
    "waypoint": {
      "*": { "\\@attributes": "waypoints" }},
    
    "marker": {
      "*": {
        "\\@attributes": {
          "type": {
            "route": { "@(2)": "route" },
            "segment": { "@(2)": "segments[]" }
          }
        }
      }
    },
    "poi": {
      "*": { "\\@attributes": "pois" }
    },
   
    "error": "Error"
  }
},
// ... so we need to convert the strings back to arrays!
{
    "operation": "cardinality",
    "spec": {
    "waypoints": "MANY",
    "pois": "MANY"
    }
}
]"""
