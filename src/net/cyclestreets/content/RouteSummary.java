package net.cyclestreets.content;

public class RouteSummary 
{
	private int id_;
	private String title_;
	private String plan_;
	private int distance_;
	
	RouteSummary(final int id, 
				 final String title,
				 final String plan,
				 final int distance)
	{
		id_ = id;
		title_ = title;
		plan_ = plan;
		distance_ = distance;
	} // RouteSummary
	
	public int id() { return id_; }
	public String title() { return title_; }
	public String plan() { return plan_; }
	public int distance() { return distance_; }
} // class RouteSummary
