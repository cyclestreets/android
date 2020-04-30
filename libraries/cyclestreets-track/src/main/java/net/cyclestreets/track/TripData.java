package net.cyclestreets.track;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class TripData {
  private long tripid;
  private long startTime_ = 0;
  private long endTime_ = 0;
  private int status;
  private float distance;
  private String purp_;
  private String info_;
  private String fancystart_;
  private List<CyclePoint> gpspoints;
  private String note_;
  private String age_;
  private String gender_;
  private String experience_;

  private DbAdapter mDb;

  public static int STATUS_RECORDING = 0;
  public static int STATUS_RECORDING_COMPLETE = 5;
  public static int STATUS_COMPLETE_UNSENT = 1;
  public static int STATUS_COMPLETE = 2;
  public static int STATUS_COMPLETE_FAILED = 3;

  public static TripData createTrip(Context c) {
    TripData t = new TripData(c.getApplicationContext(), 0);
    t.createTripInDatabase(c);
    t.initializeData();
    return t;
  }

  public static TripData fetchTrip(Context c, long tripid) {
    TripData t = new TripData(c.getApplicationContext(), tripid);
    t.populateDetails();
    return t;
  }

  public TripData(Context ctx, long tripid) {
    Context context = ctx.getApplicationContext();
    this.tripid = tripid;
    mDb = new DbAdapter(context);
  }

  private void initializeData() {
    startTime_ = now();
    endTime_ = now();
    distance = 0;

    purp_ = fancystart_ = info_ = "";

    gpspoints = new ArrayList<>();

    mDb.open();
    mDb.setStartTime(tripid, startTime_);
    mDb.close();
  }

  // Get lat/long extremes, etc, from trip record
  private void populateDetails() {
    mDb.openReadOnly();

    Cursor tripdetails = mDb.fetchTrip(tripid);
    startTime_ = tripdetails.getInt(tripdetails.getColumnIndex("start"));
    status =  tripdetails.getInt(tripdetails.getColumnIndex("status"));
    endTime_ = tripdetails.getInt(tripdetails.getColumnIndex("endtime"));
    distance = tripdetails.getFloat(tripdetails.getColumnIndex("distance"));

    purp_ = tripdetails.getString(tripdetails.getColumnIndex("purp"));
    fancystart_ = tripdetails.getString(tripdetails.getColumnIndex("fancystart"));
    info_ = tripdetails.getString(tripdetails.getColumnIndex("fancyinfo"));
    note_ = tripdetails.getString(tripdetails.getColumnIndex("note"));
    age_ = tripdetails.getString(tripdetails.getColumnIndex("age"));
    gender_ = tripdetails.getString(tripdetails.getColumnIndex("gender"));
    experience_ = tripdetails.getString(tripdetails.getColumnIndex("experience"));

    tripdetails.close();
    mDb.close();

    loadJourney();
  }

  private void loadJourney() {
    // Otherwise, we need to query DB and build points from scratch.
    gpspoints = new ArrayList<>();

    mDb.openReadOnly();

    Cursor points = mDb.fetchAllCoordsForTrip(tripid);
    int COL_LAT = points.getColumnIndex("lat");
    int COL_LGT = points.getColumnIndex("lgt");
    int COL_TIME = points.getColumnIndex("time");
    int COL_ACC  = points.getColumnIndex(DbAdapter.K_POINT_ACC);
    int COL_SPEED = points.getColumnIndex(DbAdapter.K_POINT_SPEED);
    int COL_ALT = points.getColumnIndex(DbAdapter.K_POINT_ALT);

    while (!points.isAfterLast()) {
      double lat = points.getInt(COL_LAT) / 1e6;
      double lgt = points.getInt(COL_LGT) / 1e6;
      long time = points.getInt(COL_TIME);
      double altitude = points.getDouble(COL_ALT);
      float speed = (float)points.getDouble(COL_SPEED);
      float acc = (float)points.getDouble(COL_ACC);

      gpspoints.add(new CyclePoint(lat, lgt, time, acc, altitude, speed));

      points.moveToNext();
    }
    points.close();
    mDb.close();
  }

  private void createTripInDatabase(Context c) {
    mDb.open();
    tripid = mDb.createTrip();
    mDb.close();
  }

  void dropTrip() {
    mDb.open();
    mDb.deleteAllCoordsForTrip(tripid);
    mDb.deleteTrip(tripid);
    mDb.close();
  }

  public long id() { return tripid; }
  public boolean dataAvailable() { return gpspoints.size() != 0; }
  public GeoPoint startLocation() { return gpspoints.get(0); }
  public GeoPoint endLocation() { return gpspoints.get(gpspoints.size()-1); }
  public BoundingBox boundingBox() {
    double lathigh = Double.MIN_VALUE;
    double lgthigh = Double.MIN_VALUE;
    double latlow = Double.MAX_VALUE;
    double lgtlow = Double.MAX_VALUE;

    for (GeoPoint gp : gpspoints) {
      lathigh = Math.max(gp.getLatitude(), lathigh);
      latlow = Math.min(gp.getLatitude(), latlow);
      lgthigh = Math.max(gp.getLongitude(), lgthigh);
      lgtlow = Math.min(gp.getLongitude(), lgtlow);
    }

    return new BoundingBox(lathigh, lgtlow, latlow, lgthigh);
  }
  public List<CyclePoint> journey() { return gpspoints;  }
  public long startTime() { return startTime_; }
  public long endTime() { return endTime_; }
  public long secondsElapsed() {
    if (status == STATUS_RECORDING)
      return now() - startTime_;
    return endTime_ - startTime_;
  }
  public long lastPointElapsed() {
    if (!dataAvailable())
      return secondsElapsed();
    return now() - endTime_;
  }
  public float distanceTravelled() {
    return (0.0006212f * distance);
  }
  public String notes() { return note_; }
  public String purpose() { return purp_; }
  public String info() { return info_; }
  public String fancyStart() { return fancystart_; }
  public String age() { return age_; }
  public String gender() { return gender_; }
  public String experience() { return experience_; }

  private long now() { return System.currentTimeMillis()/1000; }

  public void addPointNow(Location loc) {
    double lat = loc.getLatitude();
    double lgt = loc.getLongitude();

    float accuracy = loc.getAccuracy();
    double altitude = loc.getAltitude();
    float speed = loc.getSpeed();

    endTime_ = (loc.getTime()/1000);
    CyclePoint pt = new CyclePoint(lat, lgt, endTime_, accuracy, altitude, speed);

    if (gpspoints.size() > 1) {
      CyclePoint gp = gpspoints.get(gpspoints.size()-1);

      double segmentDistance = gp.distanceToAsDouble(pt);
      if (segmentDistance == 0)
        return; // we haven't gone anywhere

      distance += (float)segmentDistance;
    }

    gpspoints.add(pt);

    mDb.open();
    mDb.addCoordToTrip(tripid, pt);
    mDb.setDistance(tripid, distance);
    mDb.close();
  }

  public void recordingStopped() {
    endTime_ = now();
    mDb.open();
    mDb.updateTripStatus(tripid, STATUS_RECORDING_COMPLETE);
    mDb.setEndTime(tripid, endTime_);
    mDb.close();
  }
  public void metaDataComplete() { updateTripStatus(STATUS_COMPLETE_UNSENT);}
  public void successfullyUploaded() { updateTripStatus(STATUS_COMPLETE); }
  public void uploadFailed() { updateTripStatus(STATUS_COMPLETE_FAILED); }

  private void updateTripStatus(int tripStatus) {
    mDb.open();
    mDb.updateTripStatus(tripid, tripStatus);
    mDb.close();
  }

  public void updateTrip(String purpose,
                         String fancyStart,
                         String fancyInfo,
                         String notes,
                         String age,
                         String gender,
                         String experience) {
    // Save the trip details to the phone database. W00t!
    mDb.open();
    mDb.updateNotes(tripid, purpose, fancyStart, fancyInfo, notes, age, gender, experience);
    mDb.close();

    purp_ = purpose;
    note_ = notes;
    age_ = age;
    gender_ = gender;
    experience_ = experience;
  }

}
