package net.cyclestreets.content;

public class RouteSummary 
{
	private int localId_;
	private int itinerary_;
	private String title_;
	private String plan_;
	private int distance_;
	
	RouteSummary(final int localId,
				       final int itinerary, 
				       final String title,
				       final String plan,
				       final int distance)
	{
		localId_ = localId;
		itinerary_ = itinerary;
		title_ = title;
		plan_ = plan;
		distance_ = distance;
	} // RouteSummary
	
	public int localId() { return localId_; }
	public int itinerary() { return itinerary_; }
	public String title() { return title_; }
	public String plan() { return plan_; }
	public int distance() { return distance_; }
} // class RouteSummary
