package net.cyclestreets.track;

import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import net.cyclestreets.CycleStreetsNotifications;

import static net.cyclestreets.CycleStreetsNotifications.CHANNEL_TRACK_ID;

public class RecordingService extends Service implements LocationListener {
  private static final int updateDistance = 5;  // metres
  private static final int updateTime = 5000;    // milliseconds
  private static final int NOTIFICATION_ID = 1;

  private TrackListener trackListener_;
  private Class<Activity> activityClass_;
  private LocationManager locationManager_ = null;

  // Bike bell variables
  private static final int BELL_FIRST_INTERVAL = 20;
  private static final int BELL_NEXT_INTERVAL = 5;
  private static final long BAIL_TIME = 300;
  private Timer tickTimer_;
  private Timer bellTimer_;
  private SoundPool soundpool_;
  private int bikebell_;
  private final Handler handler_ = new Handler();
  private final Runnable ringBell_ = this::remindUser;
  private final Runnable tick_ = this::notifyUpdate;

  private float curSpeedMph_;
  private TripData trip_;

  public final static int STATE_IDLE = 0;
  public final static int STATE_RECORDING = 1;
  public final static int STATE_FULL = 3;

  private int state_ = STATE_IDLE;

  @Override
  public void onCreate() {
    super.onCreate();
    AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
    soundpool_ = new SoundPool.Builder().setAudioAttributes(attributes).build();
    bikebell_ = soundpool_.load(this.getBaseContext(), R.raw.bikebell,1);
    locationManager_ = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    stopTimers();
  }

  @Override
  public IBinder onBind(
      final Intent intent) {
    return new ServiceBinder(this);
  }
  @Override
  public int onStartCommand(
      final Intent intent,
      final int flags,
      final int startId) {
    return Service.START_STICKY;
  }

  private static class ServiceBinder extends Binder implements IRecordService {
    private final RecordingService rs_;

    public ServiceBinder(final RecordingService rs) {
      rs_ = rs;
    }

    public int getState() {
      return rs_.state_;
    }
    public TripData startRecording() {
      return rs_.startRecording();
    }
    public TripData stopRecording() {
      return rs_.stopRecording();
    }

    public void setListener(
        final TrackListener ra) {
      rs_.trackListener_ = ra;
    }
    public void setNotificationActivity(
        final Class<Activity> activityClass) {
      rs_.activityClass_ = activityClass;
    }
  }

  // ---end SERVICE methods -------------------------

  private TripData startRecording() {
    if (state_ == STATE_RECORDING)
      return trip_;

    startForeground(NOTIFICATION_ID, createNotification(
        "Recording ...",
        Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT));

    state_ = STATE_RECORDING;
    trip_ = TripData.createTrip(this);

    curSpeedMph_ = 0.0f;

    // Start listening for GPS updates!
    locationManager_.requestLocationUpdates(
        LocationManager.GPS_PROVIDER,
        updateTime,
        updateDistance,
        this);

    startTimers();

    if (trackListener_ != null)
      trackListener_.started(trip_);

    return trip_;
  }

  private TripData stopRecording() {
    if (trip_.dataAvailable())
      finishRecording();
    else
      cancelRecording();
    return trip_;
  }

  private void finishRecording() {
    state_ = STATE_FULL;

    clearUp();

    trip_.recordingStopped();
  }

  private void cancelRecording() {
    if (trip_ != null)
      trip_.dropTrip();

    clearUp();

    state_ = STATE_IDLE;
  }

  private void clearUp() {
    locationManager_.removeUpdates(this);

    clearNotifications();

    stopTimers();

    stopForeground(true);
  }

  private void startTimers() {
    bellTimer_ = new Timer();
    bellTimer_.schedule(new TimerTask() {
      @Override
      public void run() {
        handler_.post(ringBell_);
      }
    }, BELL_FIRST_INTERVAL * 60000, BELL_NEXT_INTERVAL * 60000);

    tickTimer_ = new Timer();
    tickTimer_.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        handler_.post(tick_);
      }
    }, 0, 1000);  // every second
  }

  private void stopTimers() {
    if (bellTimer_ != null) {
      bellTimer_.cancel();
      bellTimer_.purge();
      bellTimer_ = null;
    }
    if (tickTimer_ != null) {
      tickTimer_.cancel();
      tickTimer_.purge();
      tickTimer_ = null;
    }
  }

  // LocationListener implementation:
  @Override
  public void onLocationChanged(
      final Location loc) {
    updateTripStats(loc);
    trip_.addPointNow(loc);
    notifyUpdate();
  }

  private void updateTripStats(
      final Location newLocation) {
    final float spdConvert = 2.2369f;

    // Stats should only be updated if accuracy is decent
    if (newLocation.getAccuracy() > 20)
      return;

    // Speed data is sometimes awful, too:
    curSpeedMph_ = newLocation.getSpeed() * spdConvert;
  }

  @Override
  public void onProviderDisabled(String arg0) {
  }

  @Override
  public void onProviderEnabled(String arg0) {
  }

  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
  }
  // END LocationListener implementation:

  private NotificationManager nm() {
    return (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
  }

  private Notification createNotification(final String tickerText, final int flags) {
    final Intent notificationIntent = new Intent(this, activityClass_);
    final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    Notification notification = CycleStreetsNotifications.INSTANCE.getBuilder(this, CHANNEL_TRACK_ID)
            .setSmallIcon(R.drawable.icon25)
            .setTicker(tickerText)
            .setWhen(java.lang.System.currentTimeMillis())
            .setContentTitle("Cycle Hackney - Recording")
            .setContentText("Tap to see your ongoing trip")
            .setContentIntent(contentIntent)
            .build();
    notification.flags = flags;
    return notification;
  }

  private void showNotification(
      final String tickerText,
      final int flags) {
    final Notification notification = createNotification(tickerText, flags);
    nm().notify(NOTIFICATION_ID, notification);
  }

  private void remindUser() {
    soundpool_.play(bikebell_, 1.0f, 1.0f, 1, 0, 1.0f);

    int minutes = (int) (trip_.secondsElapsed() / 60);
    String tickerText = String.format("Still recording (%d min)", minutes);

    showNotification(tickerText, Notification.FLAG_ONGOING_EVENT);
  }

  private void clearNotifications() {
    nm().cancel(NOTIFICATION_ID);
  }

  private boolean hasRiderStopped() {
    if (trip_.secondsElapsed() < BAIL_TIME)
      return false;
    if (trip_.lastPointElapsed() > BAIL_TIME)
      return true;
    if (!trip_.dataAvailable())
      return false;

    final List<CyclePoint> points = trip_.journey();
    final CyclePoint end = points.get(points.size()-1);
    for (int i = points.size()-1; i != 0; --i) {
      final CyclePoint cur = points.get(i);

      if (end.distanceTo(cur) > 100)
        return false;

      if ((end.time - cur.time) > BAIL_TIME)
        break;
    }

    return true;
  }

  private void notifyUpdate() {
    if (trackListener_ == null)
      return;

    trackListener_.updateStatus(curSpeedMph_, trip_);

    if (hasRiderStopped())
      trackListener_.riderHasStopped(trip_);
  }
}
