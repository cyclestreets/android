package net.cyclestreets.track;

public interface TrackListener {
  void updateStatus(float currentMph, TripData tripData);
  void riderHasStopped();

  void completed();
  void abandoned();
}
