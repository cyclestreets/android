package uk.org.invisibility.cycloid;

import org.osmdroid.util.GeoPoint;

public class GeoPlace
{
	public GeoPlace(GeoPoint coord, String name, String near)
	{
	  this.coord = coord;
	  this.name = name;
	  this.near = near;	  
	}
	public final GeoPoint coord;
	public final String name;
	public final String near;
	
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
}
