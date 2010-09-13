package net.cyclestreets.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class MetaCategory {
	@Element(required=false)
	public String tag, name, description;

	@Element(required=false)
	public int ordering;

	@Override
	public String toString() {
		return tag;
	}
}
