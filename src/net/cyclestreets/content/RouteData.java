package net.cyclestreets.content;

import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.ArrayList;

public class RouteData 
{
	final String name_;
	final String xml_;
	final List<GeoPoint> points_;
	
	public RouteData(final String xml, 
        					 final List<GeoPoint> points,
        					 final String name)
	{
		xml_ = xml;
		points_ = points;
		name_ = name;
	} // RouteData
	
	public RouteData(final String xml,
	                 final GeoPoint[] points,
	                 final String name)
	{
	  xml_ = xml;
	  points_ = new ArrayList<GeoPoint>();
	  for(final GeoPoint p : points)
	    points_.add(p);
	  name_ = name;   
	} // RouteData
	
	public String name() { return name_; }
	public String xml() { return xml_; }
	public List<GeoPoint> points() { return points_; }
} // class RouteData
