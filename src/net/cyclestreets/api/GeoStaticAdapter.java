package net.cyclestreets.api;

import java.util.List;

import net.cyclestreets.api.GeoPlace;

import org.osmdroid.util.BoundingBoxE6;

import android.content.Context;
import android.os.AsyncTask;

public class GeoStaticAdapter extends GeoAdapter 
{
	public interface OnPopulatedListener
	{
		void onPopulated();
	} // interface OnPopulatedListener
	
	private final OnPopulatedListener listener_;

	public GeoStaticAdapter(final Context context,
							final String search,
						    final BoundingBoxE6 bounds,
						    final OnPopulatedListener listener)
	{
		super(context);
		
		listener_ = listener;
		
		asyncGeoCode(search, bounds);
	} // GeoAdapter
	
	private void populate(final List<GeoPlace> list)
	{
		addAll(list);
		
		if(listener_ != null)
			listener_.onPopulated();
	} // addAll
	
	private void asyncGeoCode(final String search,
							  final BoundingBoxE6 bounds)
	{
		final AsyncGeoCoder coder = new AsyncGeoCoder(this);
		coder.execute(search, bounds);
	} // asyncGeoCode
	
	static private class AsyncGeoCoder extends AsyncTask<Object, Void, List<GeoPlace>>
	{
		private GeoStaticAdapter owner_;
		
		public AsyncGeoCoder(final GeoStaticAdapter adapter)
		{
			owner_ = adapter;
		} // AsyncGeoCoder
		
		@Override
		protected List<GeoPlace> doInBackground(Object... params) 
		{
			final String search = (String)params[0];
			final BoundingBoxE6 box = (BoundingBoxE6)params[1];
			return owner_.geoCode(search, box);
		} // doInBackground
		
		@Override
		protected void onPostExecute(final List<GeoPlace> photos) 
		{
			owner_.populate(photos);
		} // onPostExecute
	} // AsyncGeoCoder
} // class GeoAdapter
