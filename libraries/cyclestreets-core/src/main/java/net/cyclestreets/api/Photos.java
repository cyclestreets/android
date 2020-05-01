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
  }

  @Override
  public Iterator<Photo> iterator() {
    return photos.iterator();
  }

  /////////////////////////////////////////////////////////////
  public static Photos load(final BoundingBox boundingBox) {
    return ApiClient.INSTANCE.getPhotos(boundingBox.getLonWest(),
                                        boundingBox.getLatSouth(),
                                        boundingBox.getLonEast(),
                                        boundingBox.getLatNorth());
  }
}
