package net.cyclestreets.api;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;

@Root(strict=false)
public class Journey implements Parcelable {
	@ElementList(inline=true, entry="marker", required=false)
	public List<Segment> segments;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub		
	}
}
