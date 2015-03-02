package net.cyclestreets.track;

public interface IRecordService {
	public int getState();

	public TripData startRecording();
	public void cancelRecording();
	public long finishRecording();
	public void reset();

	public void setListener(TrackListener ra);
}
