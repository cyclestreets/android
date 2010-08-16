package net.cyclestreets.api;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Journey {
	@ElementList(inline=true)
	public List<Marker> markers;
}
