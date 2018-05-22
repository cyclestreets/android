package net.cyclestreets.api;

import org.osmdroid.util.BoundingBox;

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
  public static Photos load(final BoundingBox boundingBox)
          throws IOException {
    return ApiClient.getPhotos(boundingBox.getLonEast(),
                               boundingBox.getLonWest(),
                               boundingBox.getLatNorth(),
                               boundingBox.getLatSouth());
  } // load
}
