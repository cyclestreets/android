package net.cyclestreets.track;

interface IRecordService {
	public int getState();

	public TripData startRecording();
	public TripData stopRecording();

	public void setListener(TrackListener ra);
}
