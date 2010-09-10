package net.cyclestreets.api;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Journey implements Parcelable {
	@ElementList(inline=true, entry="marker", required=false)
	public List<Segment> segments;
}
