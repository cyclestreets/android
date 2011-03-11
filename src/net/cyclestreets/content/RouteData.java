package net.cyclestreets.content;

import org.osmdroid.util.GeoPoint;

public class RouteData 
{
	final String xml_;
	final GeoPoint start_;
	final GeoPoint end_;
	
	public RouteData(final String xml, 
					 final GeoPoint start,
					 final GeoPoint end)
	{
		xml_ = xml;
		start_ = start;
		end_ = end;
	} // RouteData
	
	public String xml() { return xml_; }
	public GeoPoint start() { return start_; }
	public GeoPoint end() { return end_; }
} // class RouteData
