package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class PhotomapCategories {
	@Element(required=false)
	long validuntil;
	
	@ElementList(required=false)
	public List<Category> categories = new ArrayList<Category>();
	
	@ElementList(required=false)
	public List<MetaCategory> metacategories = new ArrayList<MetaCategory>();

	@Override
	public String toString() {
		return "PhotomapCategories [categories=" + categories
				+ ", metacategories=" + metacategories + ", validuntil="
				+ validuntil + "]";
	}
}
