package net.cyclestreets.api;

import org.osmdroid.util.BoundingBoxE6;

import java.io.IOException;
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

  static public GeoPlaces EMPTY = new GeoPlaces();

  ///////////////////////////////////////////////
  static public GeoPlaces search(final String searchTerm,
                                 final BoundingBoxE6 bounds)
          throws IOException {
    return ApiClient.geoCoder(searchTerm,
            bounds.getLatNorthE6() / 1E6,
            bounds.getLatSouthE6() / 1E6,
            bounds.getLonEastE6() / 1E6,
            bounds.getLonWestE6() / 1E6);
  }
}
