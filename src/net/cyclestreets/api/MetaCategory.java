package net.cyclestreets.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class MetaCategory implements ICategory
{
	@Element(required=false)
	public String tag, name, description;

	@Element(required=false)
	public int ordering;

	@Override
	public String getName() { return name; }
	@Override 
	public String getTag() { return tag; }
	@Override
	public String getDescription() { return description; }

	@Override
	public String toString() {
		return tag;
	}
}
