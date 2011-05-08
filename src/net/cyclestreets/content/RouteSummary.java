package net.cyclestreets.content;

public class RouteSummary 
{
	private int itinerary_;
	private String title_;
	private String plan_;
	private int distance_;
	
	RouteSummary(final int itinerary, 
				 final String title,
				 final String plan,
				 final int distance)
	{
		itinerary_ = itinerary;
		title_ = title;
		plan_ = plan;
		distance_ = distance;
	} // RouteSummary
	
	public int itinerary() { return itinerary_; }
	public String title() { return title_; }
	public String plan() { return plan_; }
	public int distance() { return distance_; }
} // class RouteSummary
