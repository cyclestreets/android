package net.cyclestreets.track;

public interface TrackListener {
  void updateStatus(float spdCurrent, float spdMax);
  void updateTimer(long elapsedMS);
  void riderHasStopped();
}
