package net.cyclestreets.liveride;

import android.Manifest;
import android.util.Log;
import net.cyclestreets.util.Logging;
import net.cyclestreets.util.PermissionsKt;
import org.osmdroid.util.GeoPoint;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LiveRideService extends Service implements LocationListener
{
  private IBinder binder_;
  private LocationManager locationManager_;
  private Location lastLocation_;
  private LiveRideState stage_;
  private static final int UPDATE_DISTANCE = 5;  // metres
  private static final int UPDATE_TIME = 500;    // milliseconds
  private static final String TAG = Logging.getTag(LiveRideService.class);

  @Override
  public void onCreate() {
    binder_ = new Binding();
    locationManager_ = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    stage_ = LiveRideStateKt.stoppedState(this);
  }

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    return Service.START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    stopRiding();
    super.onDestroy();
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return binder_;
  }

  public void startRiding() {
    if (!stage_.isStopped())
      return;

    if (!PermissionsKt.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
      // Should be unreachable, but we're being defensive
      Log.w(TAG, "Location permission is not granted.  Bail out.");
      return;
    }

    stage_ = LiveRideStateKt.initialState(this);
    locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_TIME, UPDATE_DISTANCE, this);
  }

  public void stopRiding() {
    if (stage_.isStopped())
      return;
    stage_.getTts().stop();
    stage_.getTts().shutdown();
    stage_ = LiveRideStateKt.stoppedState(this);
    locationManager_.removeUpdates(this);
  }

  public boolean areRiding() {
    return stage_.arePedalling();
  }

  public Location lastLocation() {
    return lastLocation_;
  }

  public class Binding extends Binder  {
    private LiveRideService service() { return LiveRideService.this; }
    public void startRiding() { service().startRiding(); }
    public void stopRiding() { service().stopRiding(); }
    public boolean areRiding() { return service().areRiding(); }
    public String stage() { return stage_.getClass().getSimpleName(); }
    public Location lastLocation() { return service().lastLocation(); }
  }

  // ///////////////////////////////////////////////
  // location listener
  @Override
  public void onLocationChanged(final Location location) {
    if (!Route.available()) {
      stopRiding();
      return;
    }

    lastLocation_ = location;

    final GeoPoint whereIam = new GeoPoint(location);
    final float accuracy = location.hasAccuracy() ? location.getAccuracy() : 2;

    final Journey journey = Route.journey();

    stage_ = stage_.update(journey, whereIam, (int)accuracy);
  }

  @Override
  public void onProviderDisabled(String arg0) { }
  @Override
  public void onProviderEnabled(String arg0) { }
  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2) { }
}
