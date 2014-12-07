package net.cyclestreets.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import net.cyclestreets.api.json.JsonReader;

public class Photos implements Iterable<Photo> {
  private List<Photo> photos_;

  protected Photos()
  {
    photos_ = new ArrayList<>();
  } // Photos

  private void add(final Photo photo)
  {
    photos_.add(photo);
  } // add

  @Override
  public Iterator<Photo> iterator()
  {
    return photos_.iterator();
  } // iterator

  /////////////////////////////////////////////////////////////
  public static Photos load(final IGeoPoint centre,
                            final int zoom,
                            final BoundingBoxE6 boundingBox)
     throws Exception {
    return load(centre.getLongitudeE6() / 1E6,
                centre.getLatitudeE6() / 1E6,
                zoom,
                boundingBox.getLonEastE6() / 1E6,
                boundingBox.getLonWestE6() / 1E6,
                boundingBox.getLatNorthE6() / 1E6,
                boundingBox.getLatSouthE6() / 1E6);
  } // load

  private static Photos load(final double clong,
                             final double clat,
                             final int zoom,
                             final double e,
                             final double w,
                             final double n,
                             final double s)
      throws Exception {
    return ApiClient.getPhotos(clong, clat, zoom, e, w, n, s);
  } // load

  ////////////////////////////////////////////////////
  public static Factory<Photos> factory() {
    return new PhotosFactory();
  } // factory

  private static class PhotosFactory implements Factory<Photos> {
    private Photos photos_;

    public PhotosFactory() {
    } // PhotosFactory

    public Photos read(final byte[] json) {
      try {
        return doRead(json);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    } // read

    public Photos doRead(final byte[] json) throws IOException {
      final JsonReader reader = new JsonReader(byteStreamReader(json));
      try {
        reader.beginObject();
        while (reader.hasNext()) {
          final String name = reader.nextName();
          if ("features".equals(name))
            readPhotos(reader);
          else
            reader.skipValue();
        } // while
        reader.endObject();
      }
      finally {
        reader.close();
      }

      return photos_;
    } // doRead

    private Reader byteStreamReader(final byte[] bytes) throws UnsupportedEncodingException {
      final InputStream in = new ByteArrayInputStream(bytes);
      return new InputStreamReader(in, "UTF-8");
    } // byteReader

    private void readPhotos(final JsonReader reader) throws IOException {
      reader.beginArray();
      while (reader.hasNext())
        readPhoto(reader);
      reader.endArray();
    } // readPhotos

    private void readPhoto(final JsonReader reader) throws IOException {
      reader.beginObject();
      // type, properties, geometry
      Photo newPhoto = null;
      GeoPoint location = null;
      while (reader.hasNext()) {
        final String name = reader.nextName();
        if ("properties".equals(name))
          newPhoto = readProperties(reader);
        else if ("geometry".equals(name))
          location = readLocation(reader);
        else
          reader.skipValue();
      } // while

      newPhoto.setPosition(location);
      photos_.add(newPhoto);

      reader.endObject();
    } // readPhoto

    private Photo readProperties(final JsonReader reader) throws IOException {
      // id, caption, hasPhoto, hasVideo, videoFormats, thumbnailUrl, shortlink
      int id = -1;
      String feature = "Not known";
      String caption = null;
      String thumbnailUrl = null;
      String url = null;

      reader.beginObject();
      while (reader.hasNext()) {
        String name = reader.nextName();
        if("id".equals(name))
          id = reader.nextInt();
        else if ("caption".equals(name))
          caption = reader.nextString();
        else if ("thumbnailUrl".equals(name))
          thumbnailUrl = reader.nextString();
        else if ("shortlink".equals(name))
          url = reader.nextString();
        else
          reader.skipValue();
      } // while
      reader.endObject();

      return new Photo(id, feature, caption, thumbnailUrl, url, null);
    } // readProperties

    private GeoPoint readLocation(final JsonReader reader) throws IOException {
      return null;
    } // readLocation
  } // class PhotosFactory
} // class Photos
