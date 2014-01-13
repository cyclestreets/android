package net.cyclestreets.views.overlay;

import android.content.SharedPreferences;

public interface PauseResumeListener
{
  public void onResume(SharedPreferences prefs);
  public void onPause(SharedPreferences.Editor prefs);
} // interface PauseResumeListener
