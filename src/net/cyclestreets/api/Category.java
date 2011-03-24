package net.cyclestreets.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Category implements ICategory
{
	@Element(required=false)
	public String tag, name, plural, nameShowing, description, invitation, icon;

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
