package net.cyclestreets.track;

public interface TrackListener {
  void updateStatus(float currentMph, TripData trip);
  void riderHasStopped(TripData trip);

  void completed(TripData trip);
  void abandoned(TripData trip);
}
