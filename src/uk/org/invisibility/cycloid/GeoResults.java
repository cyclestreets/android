package uk.org.invisibility.cycloid;

import java.util.ArrayList;
import org.osmdroid.util.GeoPoint;

public class GeoResults
{
	ArrayList<GeoPlace> places;
	String error;

	public GeoResults() 
	{
		this.places = new ArrayList<GeoPlace>();
	}
	public GeoResults setError(String s) { error = s; return this;}
	public String getError() { return error; }
	public boolean isValid() { return error == null; }

	public void addResult(GeoPoint point, String name, String near)
	{
		places.add(new GeoPlace(point, name, near));
	}
	
	Iterable<GeoPlace> getPlaces() { return places; }
}
