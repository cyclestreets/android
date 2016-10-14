package net.cyclestreets.api;

import org.osmdroid.util.BoundingBoxE6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Photos implements Iterable<Photo> {
  private final List<Photo> photos = new ArrayList<>();

  public Photos(List<Photo> photos) {
    this.photos.addAll(photos);
  } // Photos

  @Override
  public Iterator<Photo> iterator() {
    return photos.iterator();
  } // iterator

  /////////////////////////////////////////////////////////////
  public static Photos load(final BoundingBoxE6 boundingBox)
          throws IOException {
    return ApiClient.getPhotos(boundingBox.getLonEastE6() / 1E6,
                               boundingBox.getLonWestE6() / 1E6,
                               boundingBox.getLatNorthE6() / 1E6,
                               boundingBox.getLatSouthE6() / 1E6);
  } // load
}
