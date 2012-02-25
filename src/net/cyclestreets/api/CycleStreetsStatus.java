package net.cyclestreets.api;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import net.cyclestreets.util.Base64;
import net.cyclestreets.util.Bitmaps;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;

public class CycleStreetsStatus
{
	private String distanceAmount_;
	private String distanceUnits_;
	private List<String> countries_;

	private CycleStreetsStatus()
	{
	}
	
	
	public String getDistanceAmount() 
	{
		return distanceAmount_;
	}
	
	
	public String getDistanceUnits()
	{
		return distanceUnits_;
	}
	
	
	public List<String> getCountries()
	{
		return countries_;
	}
	
	
	
	
	static public Factory<CycleStreetsStatus> factory() { 
		return new CycleStreetsStatusFactory();
	}	// Factory

	

	
	
	
	static private class CycleStreetsStatusFactory extends Factory<CycleStreetsStatus>
	{
		private CycleStreetsStatus status_;
		
		protected CycleStreetsStatus get() {
			return status_;
		}
		
		protected ContentHandler contentHandler() {
			status_ = new CycleStreetsStatus();
		      
		    final RootElement root = new RootElement("poitypes");
		      
		    return root.getContentHandler();
		}
	}	// CycleStreetsStatusFactory
	
	
	
	
	
	
	
	
}
