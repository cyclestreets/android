package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.events.MapListener;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import net.cyclestreets.api.POI;

public class POIOverlay extends CycleStreetsItemOverlay<POIOverlay.POIItem>
                        implements MapListener
{
	static public class POIItem extends OverlayItem 
	{
		private final POI poi_;
		
		public POIItem(final POI poi) 
		{
			super(poi.id() + "", poi.name(), poi.position());
			poi_ = poi;
		} // PhotoItem

		public POI poi() { return poi_; }
		
		// Markers
		@Override
		public Drawable getMarker(int stateBitset) 
		{ 
			// return photoMarkers.getMarker(1, stateBitset);	
		  return null;
		} // getMarker

		// Equality testing
		@Override
		public int hashCode() { return ((poi_ == null) ? 0 : poi_.id()); }
		
		/*
		 * PhotoItems are equal if underlying Photos have the same id
		 */
		@Override
		public boolean equals(final Object obj) 
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final POIItem other = (POIItem) obj;
			if (poi_ == null) 
				return (other.poi_ == null);

			return (poi_.id() == other.poi_.id());
		} // equals

		@Override
		public String toString() 
		{
			return "POIItem [poi=" + poi_ + "]";
		} // toString	
	} // class POIItem

	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////
	public POIOverlay(final Context context,
							      final MapView mapView,
							      final OnItemGestureListener<POIOverlay.POIItem> listener)
	{
		super(context, 
			    mapView,
			    listener);
	} // POIOverlay

  protected void fetchItemsInBackground(final GeoPoint mapCentre,
                                        final int zoom,
                                        final BoundingBoxE6 boundingBox)
	{
		double n = boundingBox.getLatNorthE6() / 1E6;
		double s = boundingBox.getLatSouthE6() / 1E6;
		double e = boundingBox.getLonEastE6() / 1E6;
		double w = boundingBox.getLonWestE6() / 1E6;
		
		double clat = (double)mapCentre.getLatitudeE6() / 1E6;
		double clon = (double)mapCentre.getLongitudeE6() / 1E6;

		GetPOIsTask.fetch(this, clat, clon, zoom, n, s, e, w);
	} // refreshPhotos
	
	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////
	static private class GetPOIsTask extends AsyncTask<Object,Void,List<POI>> 
	{
		static void fetch(final POIOverlay overlay, 
						          final Object... params)
		{
			new GetPOIsTask(overlay).execute(params);
		} // fetch
		
		//////////////////////////////////////////////////////
		private final POIOverlay overlay_;
		
		private  GetPOIsTask(final POIOverlay overlay)
		{
			overlay_ = overlay;
		} // GetPhotosTask
		
		protected List<POI> doInBackground(Object... params) 
		{
			double clat = (Double) params[0];
			double clon = (Double) params[1];
			int zoom = (Integer) params[2];
			double n = (Double) params[3];
			double s = (Double) params[4];
			double e = (Double) params[5];
			double w = (Double) params[6];

			try {
				//return ApiClient.getPhotos(clat, clon, zoom, n, s, e, w);
			}
			catch (final Exception ex) {
				// never mind, eh?
			}
			return null;
		} // doInBackground
		
		@Override
		protected void onPostExecute(final List<POI> pois) 
		{
			final List<POIOverlay.POIItem> items = new ArrayList<POIOverlay.POIItem>();
			
			if(pois != null)
				for (final POI poi : pois) 
					items.add(new POIOverlay.POIItem(poi));
			
			overlay_.setItems(items);
		} // onPostExecute
	} // GetPhotosTask
} // class PhotoItemOverlay
