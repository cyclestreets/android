package net.cyclestreets.track;

public interface TrackListener {
  void updateStatus(float currentMph, TripData tripData);
  void riderHasStopped(TripData tripData);

  void completed(TripData tripData);
  void abandoned(TripData tripData);
}
