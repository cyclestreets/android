package net.cyclestreets.api.client.geojson

import net.cyclestreets.api.GeoPlace
import net.cyclestreets.api.GeoPlaces
import org.geojson.Feature
import org.geojson.FeatureCollection
import org.geojson.Point
import org.osmdroid.util.GeoPoint
import java.util.*

object MapPacksFactory {
    fun toGeoPlaces(featureCollection: FeatureCollection): GeoPlaces {
        val places: MutableList<GeoPlace> = ArrayList()
        for (feature in featureCollection.features) {
            places.add(toGeoPlace(feature))
        }
        return GeoPlaces(places)
    }

    private fun toGeoPlace(feature: Feature): GeoPlace {
        val coordinates = (feature.geometry as Point).coordinates
        return GeoPlace(GeoPoint(coordinates.latitude, coordinates.longitude),
                feature.getProperty("name"),
                feature.getProperty("near"))
    }
}