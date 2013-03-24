package net.cyclestreets.content;

import net.cyclestreets.routing.Waypoints;

public class RouteData 
{
	final String name_;
	final String xml_;
	final Waypoints points_;
	
	public RouteData(final String xml, 
        					 final Waypoints points,
        					 final String name)
	{
		xml_ = xml;
		points_ = points;
		name_ = name;
	} // RouteData
	
	public String name() { return name_; }
	public String xml() { return xml_; }
	public Waypoints points() { return points_; }
} // class RouteData
