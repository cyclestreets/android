package net.cyclestreets.api.client.dto;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "sayt")
public class GeoPlacesDto {
  @ElementList
  List<GeoPlaceDto> results;

  public GeoPlaces toGeoPlaces() {
    List<GeoPlace> places = new ArrayList<>();
    for (GeoPlaceDto geoPlaceDto : results) {
      places.add(geoPlaceDto.toGeoPlace());
    }
    return new GeoPlaces(places);
  }
}
