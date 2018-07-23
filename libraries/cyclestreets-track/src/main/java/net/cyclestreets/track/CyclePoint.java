package net.cyclestreets.track;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

public class CyclePoint extends GeoPoint {
  public float accuracy;
  public double altitude;
  public float speed;
  public long time;

  public CyclePoint(double lat, double lgt, long currentTime, float accuracy, double altitude, float speed) {
    super(lat, lgt);
    time = currentTime;
    this.accuracy = accuracy;
    this.altitude = altitude;
    this.speed = speed;
  }

  public static final Parcelable.Creator<CyclePoint> CREATOR = new Parcelable.Creator<CyclePoint>() {
    @Override
    public CyclePoint createFromParcel(final Parcel in) {
            return new CyclePoint(in);
        }

    @Override
    public CyclePoint[] newArray(final int size) {
            return new CyclePoint[size];
        }
  };

  private CyclePoint(final Parcel in) {
    super(in.readDouble(), in.readDouble());
    this.time = in.readLong();
    this.accuracy = in.readFloat();
    this.altitude = in.readDouble();
    this.speed = in.readFloat();
  }

  @Override
  public void writeToParcel(final Parcel out, final int flags) {
    out.writeDouble(getLatitude());
    out.writeDouble(getLongitude());
    out.writeLong(this.time);
    out.writeFloat(this.accuracy);
    out.writeDouble(this.altitude);
    out.writeFloat(this.speed);
  }

}
