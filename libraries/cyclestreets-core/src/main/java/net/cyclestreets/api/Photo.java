package net.cyclestreets.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Photo implements Parcelable {
  private final int id;
  private final String category;
  private final String metaCategory;
  private final String caption;
  private final long datetime;
  private final String url;
  private final String thumbnailUrl;
  private final GeoPoint position;
  private final List<Video> videos;

  public Photo(int id,
               String feature,
               String metaCategory,
               String caption,
               long datetime,
               String url,
               String thumbnailUrl,
               GeoPoint position,
               List<Video> videos) {
    this.id = id;
    category = feature;
    this.metaCategory = metaCategory;
    this.caption = caption;
    this.datetime = datetime;
    this.url = url;
    this.thumbnailUrl = thumbnailUrl;
    this.position = position;
    this.videos = videos;
  }

  public int id() { return id; }
  public boolean isPlaceholder() { return thumbnailUrl == null && !hasVideos(); }
  public String category() { return category; }
  public String metacategory() { return metaCategory; }
  public String caption() { return caption; }
  public long datetime() { return datetime; }
  public String url() { return url; }
  public String thumbnailUrl() { return thumbnailUrl; }
  public GeoPoint position() { return position; }
  public boolean hasVideos() { return videos.size() != 0; }
  public Iterable<Video> videos() { return videos; }

  public Video video(final String format) {
    for (Video v : videos)
      if (v.format().equals(format))
        return v;
    return null;
  }

  @Override
  public int hashCode() { return id; }

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
    return (id == other.id);
  }

  @Override
  public String toString() { return id + ":" + caption; }

  public static class Video {
    private final String format;
    private final String url;

    public Video(final String format, final String url) {
      this.format = format;
      this.url = url;
    }

    public String format() { return format; }
    public String url() { return url; }
  }

  ////////////////////////////////////////////////
  // parcelable
  @Override
  public int describeContents() { return 0; }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeInt(id);
    dest.writeString(category);
    dest.writeString(metaCategory);
    dest.writeString(caption);
    dest.writeString(url);
    dest.writeString(thumbnailUrl);
    dest.writeDouble(position.getLatitude());
    dest.writeDouble(position.getLongitude());
    dest.writeInt(videos.size());
    for (Video v : videos) {
      dest.writeString(v.format());
      dest.writeString(v.url());
    }
  }

  public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
    @Override
    public Photo createFromParcel(final Parcel source) {
      final int id = source.readInt();
      final String feature = source.readString();
      final String metaCategory = source.readString();
      final String caption = source.readString();
      final long datetime = source.readLong();
      final String url = source.readString();
      final String thumbnailUrl = source.readString();

      final double latitude = source.readDouble();
      final double longitude = source.readDouble();

      final List<Video> videos = new ArrayList<>();
      final int videoCount = source.readInt();
      for (int i = 0; i != videoCount; ++i) {
        final String format = source.readString();
        final String vUrl = source.readString();
        videos.add(new Video(format, vUrl));
      }

      return new Photo(
          id,
          feature,
          metaCategory,
          caption,
          datetime,
          url,
          thumbnailUrl,
          new GeoPoint(latitude, longitude),
          videos);
    }

    @Override
    public Photo[] newArray(int size) {
      return new Photo[size];
    }
  };
}
