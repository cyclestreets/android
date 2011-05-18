package net.cyclestreets.api;

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

	@Override
	public int hashCode() {
		return id;
	}

	/*
	 * Photos are equal if they have the same id
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Photo other = (Photo) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id + ":" + caption;
	}
}
