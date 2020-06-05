package net.cyclestreets.api.client.geojson

import net.cyclestreets.api.Map
import net.cyclestreets.api.Maps
import org.geojson.Feature
import org.geojson.FeatureCollection

class MapsFactory {
    companion object {
        fun toMaps(featureCollection: FeatureCollection): Maps {
            val packs = mutableListOf<Map>()
            for (feature in featureCollection.features) {
                packs.add(toMap(feature))
            }
            return Maps(packs)
        }

        private fun toMap(feature: Feature): Map {
            return Map(
                    feature.getProperty("name"),
                    feature.getProperty("url")
            )
        }
    }
}
