package net.cyclestreets.track;

public interface IRecordService {
	public int getState();

	public TripData startRecording();
	public void stopRecording();

	public void setListener(TrackListener ra);
}
