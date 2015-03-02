package net.cyclestreets.track;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

public class Tracker {
  public static void checkStatus(final Context context, final TrackerStatusListener callback) {
    // check to see if already recording here
    Intent rService = new Intent(context, RecordingService.class);
    ServiceConnection sc = new ServiceConnection() {
      public void onServiceDisconnected(ComponentName name) {}
      public void onServiceConnected(ComponentName name, IBinder service) {
        IRecordService rs = (IRecordService)service;
        int state = rs.getState();
        if (state == RecordingService.STATE_RECORDING) {
          callback.recordingActive();
        } else {
          int unfinishedTrip = DbAdapter.unfinishedTrip(context);
          if (unfinishedTrip != -1) {
            callback.unsavedTrip();
          }
        } // if ...

        context.unbindService(this); // race?  this says we no longer care
      }
    };
    // This needs to block until the onServiceConnected (above) completes.
    // Thus, we can check the recording status before continuing on.
    context.bindService(rService, sc, Context.BIND_AUTO_CREATE);
  } // checkStatus

  public static int uploadLeftOverTrips(final Context context) {
    final List<TripData> trips = DbAdapter.unUploadedTrips(context);
    if (trips.size() == 0)
      return 0;

    TripDataUploader.upload(context, trips);

    return trips.size();
  } // uploadLeftOverTrips


  private Tracker() { }
} // class Tracker
