package net.cyclestreets.api.client.geojson;

import net.cyclestreets.api.Photo;
import net.cyclestreets.api.Photos;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.cyclestreets.api.Photo.Video;

public class PhotosFactory extends AbstractObjectFactory {

  private PhotosFactory() {}

  public static Photos toPhotos(FeatureCollection featureCollection) {
    List<Photo> photos = new ArrayList<>();
    for (Feature feature : featureCollection.getFeatures()) {
      Photo photo = toPhoto(feature);
      if (!photo.isPlaceholder()) {
        photos.add(photo);
      }
    }
    return new Photos(photos);
  }

  private static Photo toPhoto(Feature feature) {
    LngLatAlt coordinates = ((Point)feature.getGeometry()).getCoordinates();
    GeoPoint geoPoint = new GeoPoint(coordinates.getLatitude(), coordinates.getLongitude());
    return new Photo(propertyOrDefault(feature, "id", -1),
                     propertyOrDefault(feature, "categoryId", "Not known"),
                     propertyOrDefault(feature, "metacategoryId", "Not known"),
                     (String)feature.getProperty("caption"),
                     (String)feature.getProperty("shortlink"),
                     (String)feature.getProperty("thumbnailUrl"),
                     geoPoint,
                     toVideos(propertyOrDefault(feature, "videoFormats", Collections.<String, Object>emptyMap())));
  }

  private static List<Video> toVideos(Map<String, Object> propertyMap) {
    List<Video> videos = new ArrayList<>();
    for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
      videos.add(toVideo(entry));
    }
    return videos;
  }

  @SuppressWarnings("unchecked")
  private static Video toVideo(Map.Entry<String, Object> videoEntry) {
    Map<String, String> videoProperties = (Map<String, String>)videoEntry.getValue();
    return new Video(videoEntry.getKey(), videoProperties.get("url"));
  }
}
