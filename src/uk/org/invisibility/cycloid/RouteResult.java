package uk.org.invisibility.cycloid;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RouteResult implements Parcelable
{
	private String start;
	private String finish;
	private int length;
	private int time;
	private String error;
	private ArrayList<GeoPoint> coords;
	
	private final int version = 0x100;
	
	public RouteResult()
	{
		coords = new ArrayList<GeoPoint>();
	}

	public RouteResult(Parcel in)
	{
		coords = new ArrayList<GeoPoint>();
		readFromParcel(in);
	}
	
	public RouteResult setStart(String s) { start = s; return this; }
	public RouteResult setFinish(String s) { finish = s; return this; }
	public RouteResult setLength(int i) { length = i; return this; }
	public RouteResult setTime(int i) { time = i; return this; }
    public RouteResult setError(String s)
    {
    	error = s;
    	Log.e("RouteResult", s);
    	return this;
    }
    
    public void addCoord(GeoPoint p) { coords.add(p); }

    public String getStart() { return start; }
	public String getFinish() { return finish; }
	public int getLength() { return length; }
	public int getTime() { return time; }
    public String getError() { return error; }
    public boolean isValid() { return error == null || error.length() == 0; }
    public Iterable<GeoPoint> getCoords() { return coords; }

	@Override
	public int describeContents()
	{		
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(version);
		dest.writeString(start);
		dest.writeString(finish);
		dest.writeInt(length);
		dest.writeInt(time);
		dest.writeString(error);
		int n = coords.size();
		dest.writeInt(n);
		for (int i = 0; i < n; i++)
		{
			dest.writeInt(coords.get(i).getLatitudeE6());
			dest.writeInt(coords.get(i).getLongitudeE6());
		}	
	}
    
	public void readFromParcel(Parcel in)
	{
		if (in.readInt() != version)
			return;
		start = in.readString();
		finish = in.readString();
		length = in.readInt();
		time = in.readInt();
		error = in.readString();
		int n = in.readInt();
		for (int i = 0; i < n; i++)
		{
			addCoord(new GeoPoint(in.readInt(), in.readInt()));
		}		
	}
	
    @SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public RouteResult createFromParcel(Parcel in)
        {
            return new RouteResult(in);
        }

        public RouteResult[] newArray(int size)
        {
            return new RouteResult[size];
        }
    };	
}
