package net.cyclestreets.track;

import android.app.Activity;

interface IRecordService {
  int getState();

  TripData startRecording();
  TripData stopRecording();

  void setListener(TrackListener ra);
  void setNotificationActivity(Class<Activity> activityClass);
}
