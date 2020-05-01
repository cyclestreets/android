package net.cyclestreets.api;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class GeoPlaces implements Iterable<GeoPlace>
{
  private List<GeoPlace> places = new ArrayList<>();

  public GeoPlaces(Collection<GeoPlace> places) {
    this.places.addAll(places);
  }

  private GeoPlaces() {}

  @Override
  public Iterator<GeoPlace> iterator() { return places.iterator(); }

  public boolean isEmpty() { return places.isEmpty(); }

  public int size() { return places.size(); }
  public GeoPlace get(int index) { return places.get(index); }

  public List<GeoPlace> asList() { return places; }

  public static GeoPlaces EMPTY = new GeoPlaces();

  ///////////////////////////////////////////////
  public static GeoPlaces search(final String searchTerm,
                                 final BoundingBox bounds) {
    return ApiClient.INSTANCE.geoCoder(searchTerm,
                                       bounds.getLonWest(),
                                       bounds.getLatSouth(),
                                       bounds.getLonEast(),
                                       bounds.getLatNorth());
  }
}
