package net.cyclestreets.track;

import org.osmdroid.util.GeoPoint;

public class CyclePoint extends GeoPoint {
	public float accuracy;
	public double altitude;
	public float speed;
	public long time;

  public CyclePoint(int lat, int lgt, long currentTime) {
    super(lat, lgt);
    time = currentTime;
  }

	public CyclePoint(int lat, int lgt, long currentTime, float accuracy, double altitude, float speed) {
		super(lat, lgt);
		time = currentTime;
		this.accuracy = accuracy;
		this.altitude = altitude;
		this.speed = speed;
	}
} // class CyclePoint
