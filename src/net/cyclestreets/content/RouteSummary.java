package net.cyclestreets.content;

public class RouteSummary 
{
	private int id_;
	private String title_;
	
	RouteSummary(final int id, final String title)
	{
		id_ = id;
		title_ = title;
	} // RouteSummary
	
	public int id() { return id_; }
	public String title() { return title_; }
} // class RouteSummary
