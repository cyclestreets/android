package net.cyclestreets.views.overlay;

import android.content.SharedPreferences;

public interface PauseResumeListener
{
  public void onPause(SharedPreferences.Editor prefs);
  public void onResume(SharedPreferences prefs);
} // interface PauseResumeListener
