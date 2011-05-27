package net.cyclestreets.api;

import org.osmdroid.util.GeoPoint;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="result", strict=false)
public class GeoPlace
{
	public GeoPlace() { }
	public GeoPlace(final int latE6, final int longE6, final String name, final String near)
	{
		this.latitude = latE6/1E6;
		this.longitude = longE6/1E6;
		this.name = name;
		this.near = near;
	} // GeoPlace
	public GeoPlace(final GeoPoint point, final String name, final String near)
	{
		this(point.getLatitudeE6(),
			 point.getLongitudeE6(),
			 name, 
			 near);
	} // GeoPlace
	
	@Element(required=false)
	public String type, name, near;

	@Element(required=false)
	public int id;
	
	@Element(required=false)
	public double longitude, latitude;
	
	@Override
	public String toString()
	{
		String result = name;
		if (near != null && near.length() > 0)
		{
			if (name.length() > 0)
				result = name + ", ";
			result += near;
		}
		return result;
	}

	public GeoPoint coord() { return new GeoPoint(latitude, longitude); }
}
