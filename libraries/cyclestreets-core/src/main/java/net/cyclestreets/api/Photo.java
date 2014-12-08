package net.cyclestreets.api;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class Photo 
{
  private final int id_;
  private final String featureName_;
  private final String caption_;
  private final String url_;
  private final String thumbnailUrl_;
  private GeoPoint position_;
  private List<Video> videos_;
  
  Photo(int id, 
        String feature,
        String caption,
        String url,
        String thumbnailUrl,
        GeoPoint position,
        List<Video> videos)
  {
    id_ = id;
    featureName_ = feature;
    caption_ = caption;
    url_ = url;
    thumbnailUrl_ = thumbnailUrl;
    position_ = position;
    videos_ = videos;
  } // Photo
  
  public int id() { return id_; }
  public String feature() { return featureName_; }
  public String caption() { return caption_; }
  public String url() { return url_; }
  public String thumbnailUrl() { return thumbnailUrl_; }
  public GeoPoint position() { return position_; }
  public boolean hasVideos() { return videos_ != null && videos_.size() != 0; }
  public Iterable<Video> videos() { return videos_; }

  void setPosition(final GeoPoint position) { position_ = position; }

  @Override
  public int hashCode() { return id_; }

  /*
   * Photos are equal if they have the same id
   */
  @Override
  public boolean equals(Object obj) 
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Photo other = (Photo)obj;
    if (id_ != other.id_)
      return false;
    return true;
  } // equals

  @Override
  public String toString() { return id_ + ":" + caption_; }

  public static class Video {
    final private String format_;
    final private String url_;

    public Video(final String format, final String url) {
      format_ = format;
      url_ = url;
    } // Video;

    public String format() { return format_; }
    public String url() { return url_; }
  } // Video
} // class Photo
