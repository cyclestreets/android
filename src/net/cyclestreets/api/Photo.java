package net.cyclestreets.api;

import net.cyclestreets.CycleStreetsUtils;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Photo {
	@Attribute(required=false)
	public int id, bearing, feature, rating, gridx, gridy;

	@Attribute(required=false)
	public double latitude, longitude;

	@Attribute(required=false)
	public String proximity, caption, privacy, type, url, imageUrl, thumbnailUrl, thumbnailSizes;

	public String toString() {
		return id + ":" + CycleStreetsUtils.truncate(caption);
	}
}
