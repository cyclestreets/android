package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class GeoPlaces
{
	@ElementList(name="results", entry="result", required=false)
	public List<GeoPlace> places = new ArrayList<GeoPlace>();
} // class GeoPlaces
