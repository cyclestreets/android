package net.cyclestreets.track;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

class Controller
    implements TrackerControl,
               ServiceConnection {
  public static TrackerControl create(final Activity context, final TrackListener listener) {
    Controller control = new Controller(context, listener);

    Intent rService = new Intent(context, RecordingService.class);
    context.bindService(rService, control, Context.BIND_AUTO_CREATE);

    return control;
  } // create

  private final Activity context_;
  private final TrackListener listener_;
  private IRecordService rs_;
  private boolean shouldStart_;
  private boolean unbound_;

  private Controller(
      final Activity context,
      final TrackListener listener) {
    context_ = context;
    listener_ = listener;
  } // Controller

  @Override
  public void onServiceConnected(
      final ComponentName name,
      final IBinder service) {
    rs_ = (IRecordService)service;
    rs_.setListener(listener_);
    rs_.setNotificationActivity((Class<Activity>)context_.getClass());

    if (shouldStart_ == true)
      rs_.startRecording();
  } // onServiceConnected

  @Override
  public void onServiceDisconnected(ComponentName name) {}

  @Override
  public void start() {
    if (rs_ == null)
      shouldStart_ = true;
    else
      rs_.startRecording();
  } // start

  @Override
  public void stop() {
    if (unbound_)
      return;

    unbound_ = true;

    final TripData trip = rs_.stopRecording();
    context_.unbindService(this);

    if (listener_ != null) {
      if (trip.dataAvailable())
        listener_.completed(trip);
      else
        listener_.abandoned(trip);
    }
  } // stop
} // Controller
