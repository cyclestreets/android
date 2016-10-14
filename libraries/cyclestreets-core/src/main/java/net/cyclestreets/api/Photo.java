package net.cyclestreets.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Photo implements Parcelable {
  private final int id_;
  private final String category_;
  private final String metaCategory_;
  private final String caption_;
  private final String url_;
  private final String thumbnailUrl_;
  private final GeoPoint position_;
  private final List<Video> videos_;
  
  public Photo(int id,
        String feature,
        String metaCategory,
        String caption,
        String url,
        String thumbnailUrl,
        GeoPoint position,
        List<Video> videos) {
    id_ = id;
    category_ = feature;
    metaCategory_ = metaCategory;
    caption_ = caption;
    url_ = url;
    thumbnailUrl_ = thumbnailUrl;
    position_ = position;
    videos_ = videos;
  } // Photo
  
  public int id() { return id_; }
  public boolean isPlaceholder() { return thumbnailUrl_ == null && !hasVideos(); }
  public String category() { return category_; }
  public String metacategory() { return metaCategory_; }
  public String caption() { return caption_; }
  public String url() { return url_; }
  public String thumbnailUrl() { return thumbnailUrl_; }
  public GeoPoint position() { return position_; }
  public boolean hasVideos() { return videos_.size() != 0; }
  public Iterable<Video> videos() { return videos_; }
  public Video video(final String format) {
    for (Video v : videos_)
      if (v.format().equals(format))
        return v;
    return null;
  } // video

  @Override
  public int hashCode() { return id_; }

  /*
   * Photos are equal if they have the same id
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Photo other = (Photo)obj;
    return (id_ == other.id_);
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

  ////////////////////////////////////////////////
  // parcelable
  @Override
  public int describeContents() { return 0; }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeInt(id_);
    dest.writeString(category_);
    dest.writeString(metaCategory_);
    dest.writeString(caption_);
    dest.writeString(url_);
    dest.writeString(thumbnailUrl_);
    dest.writeInt(position_.getLatitudeE6());
    dest.writeInt(position_.getLongitudeE6());
    dest.writeInt(videos_.size());
    for (Video v : videos_) {
      dest.writeString(v.format());
      dest.writeString(v.url());
    } // for ...
  } // writeToParcel

  public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
    @Override
    public Photo createFromParcel(final Parcel source) {
      final int id = source.readInt();
      final String feature = source.readString();
      final String metaCategory = source.readString();
      final String caption = source.readString();
      final String url = source.readString();
      final String thumbnailUrl = source.readString();

      final int latE6 = source.readInt();
      final int lonE6 = source.readInt();

      final List<Video> videos = new ArrayList<>();
      final int videoCount = source.readInt();
      for (int i = 0; i != videoCount; ++i) {
        final String format = source.readString();
        final String vurl = source.readString();
        videos.add(new Video(format, vurl));
      } // for ...

      return new Photo(
          id,
          feature,
          metaCategory,
          caption,
          url,
          thumbnailUrl,
          new GeoPoint(latE6, lonE6),
          videos);
    } // createFromParcel

    @Override
    public Photo[] newArray(int size) {
      return new Photo[size];
    } // newArray
  }; // CREATOR
} // class Photo
