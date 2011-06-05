package net.cyclestreets.contacts;

import java.util.Collections;
import java.util.List;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.util.Dialog;

import org.osmdroid.util.BoundingBoxE6;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class ContactLookup 
{
	static public List<GeoPlace> lookup(final Contact contact, 
										final BoundingBoxE6 bounds)
	{
		return lookup(contact, bounds, null);
	} // lookup
	
	static public List<GeoPlace> lookup(final Contact contact, 
										final BoundingBoxE6 bounds,
										final Context context)
	{
		return doLookup(contact, bounds, context);
	} // lookupContact
	
	static public List<GeoPlace> lookup(final String search, 
										final BoundingBoxE6 bounds,
										final Context context)
	{
		return doLookup(search, bounds, context);
	} // lookup

	@SuppressWarnings("unchecked")
	static private List<GeoPlace> doLookup(final Object search, 
										   final BoundingBoxE6 bounds,
										   final Context context)
	{
		try {
			final AsyncLookup l = new AsyncLookup(context);
			l.execute(search, bounds);
			return l.get();
		} // try
		catch(final Exception e) {
			return (List<GeoPlace>)Collections.EMPTY_LIST;
		} // catch
	} // doLookup

	static private class AsyncLookup extends AsyncTask<Object, Void, List<GeoPlace>>
	{
		final ProgressDialog progress_;
		
		public AsyncLookup(final Context context)
		{
			progress_ = (context != null) ?
							Dialog.createProgressDialog(context, "Searching for location") :
							null;
		} // AsyncLookup
		
		@Override
		protected void onPreExecute()
		{
			if(progress_ != null)
				progress_.show();
		} // onPreExecute
		
		@Override
		protected List<GeoPlace> doInBackground(Object... params) 
		{
			final BoundingBoxE6 bounds = (BoundingBoxE6)params[1];

			if(params[0] instanceof String)
				return doSearch((String)params[0], bounds);
			
			return doContactSearch((Contact)params[0], bounds);
		} // doInBackground

		@Override
		protected void onPostExecute(final List<GeoPlace> result)
		{
			if(progress_ != null)
				progress_.dismiss();
		} // onPostExecute
		
		private List<GeoPlace> doContactSearch(final Contact contact, 
											   final BoundingBoxE6 bounds)
		{
			List<GeoPlace> r = doSearch(contact.address(), bounds);
			if(!r.isEmpty())
				return r;
			
			r = doSearch(contact.postcode(), bounds);
			if(!r.isEmpty())
				return r;
			
			r = doSearch(contact.city(), bounds);
			return r;
		} // doContactSearch
		
		@SuppressWarnings("unchecked")
		private List<GeoPlace> doSearch(final String search, 
										final BoundingBoxE6 bounds)
		{
			try {
				return ApiClient.geoCoder(search, bounds).places;				
			}
			catch(Exception e) {
				return (List<GeoPlace>)Collections.EMPTY_LIST;
			} // catch
		} // doSearch
	} // AsyncGeoCoder

} // class ContactLookup
