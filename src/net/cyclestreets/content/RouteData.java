package net.cyclestreets.content;

import org.osmdroid.util.GeoPoint;

public class RouteData 
{
	final String xml_;
	final GeoPoint start_;
	final GeoPoint finish_;
	
	public RouteData(final String xml, 
					 final GeoPoint start,
					 final GeoPoint finish)
	{
		xml_ = xml;
		start_ = start;
		finish_ = finish;
	} // RouteData
	
	public String xml() { return xml_; }
	public GeoPoint start() { return start_; }
	public GeoPoint finish() { return finish_; }
} // class RouteData
