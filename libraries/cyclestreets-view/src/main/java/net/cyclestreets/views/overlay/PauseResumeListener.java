package net.cyclestreets.views.overlay;

import android.content.SharedPreferences;

public interface PauseResumeListener {
  void onResume(SharedPreferences prefs);
  void onPause(SharedPreferences.Editor prefs);
} // interface PauseResumeListener
