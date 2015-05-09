package net.cyclestreets.track;

import android.app.Activity;

interface IRecordService {
	public int getState();

	public TripData startRecording();
	public TripData stopRecording();

	public void setListener(TrackListener ra);
  public void setNotificationActivity(Class<Activity> activityClass);
}
