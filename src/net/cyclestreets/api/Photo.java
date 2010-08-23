package net.cyclestreets.api;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Photo {
	@Attribute(required=false)
	public int id, bearing, feature, rating, gridx, gridy;

	@Attribute(required=false)
	public float latitude, longitude;

	@Attribute(required=false)
	public String proximity, caption, privacy, type, url, imageUrl, thumbnailUrl, thumbnailSizes;
}
