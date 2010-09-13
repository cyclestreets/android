package net.cyclestreets.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Category {
	@Element(required=false)
	public String tag, name, plural, nameShowing, description, invitation, icon;

	@Element(required=false)
	public int ordering;

	@Override
	public String toString() {
		return tag;
	}
}
