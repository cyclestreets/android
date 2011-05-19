package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapAdapter;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.PhotoMarkers;

public class PhotoItemOverlay extends ItemizedOverlay<PhotoItemOverlay.PhotoItem> 
{
	static public class PhotoItem extends OverlayItem 
	{
		private Photo photo_;
		private PhotoMarkers photoMarkers;
		
		public PhotoItem(final Photo photo, final PhotoMarkers photoMarkers) 
		{
			super(photo.id + "", photo.caption, new GeoPoint(photo.latitude, photo.longitude));
			photo_ = photo;
			this.photoMarkers = photoMarkers;
		} // PhotoItem

		public Photo photo() { return photo_; }
		
		// Markers
		@Override
		public Drawable getMarker(int stateBitset) 
		{ 
			return photoMarkers.getMarker(photo_.feature, stateBitset);	
		} // getMarker

		// Equality testing
		@Override
		public int hashCode() { return ((photo_ == null) ? 0 : photo_.id); }
		
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
			final PhotoItem other = (PhotoItem) obj;
			if (photo_ == null) 
				return (other.photo_ == null);

			return (photo_.id == other.photo_.id);
		} // equals

		@Override
		public String toString() 
		{
			return "PhotoItem [photo=" + photo_ + "]";
		} // toString	
	} // class PhotoItem

	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////
	public PhotoItemOverlay(final Context context,
							final MapView mapView,
							final OnItemGestureListener<PhotoItemOverlay.PhotoItem> listener)
	{
		super(context, 
			  new ArrayList<PhotoItemOverlay.PhotoItem>(), 
			  listener);
        
        mapView.setMapListener(new DelayedMapListener(new PhotomapListener(context, mapView, items())));
	} // PhotoItemOverlay
	
	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////
	static private class PhotomapListener extends MapAdapter 
	{
		final private MapView map_;
		private int zoomLevel_;
		private List<PhotoItemOverlay.PhotoItem> photoList_;
		final private PhotoMarkers photoMarkers_;

		public PhotomapListener(final Context ctx, 
								final MapView map, 
								final List<PhotoItemOverlay.PhotoItem> photoList) 
		{
			map_ = map;
			zoomLevel_ = map_.getZoomLevel();		
			photoList_ = photoList;
			photoMarkers_ = new PhotoMarkers(ctx.getResources());
		} // PhotomapListener
		
		@Override
		public boolean onScroll(final ScrollEvent event) 
		{
			refreshPhotos();
			return true;
		} // onScroll
		
		@Override
		public boolean onZoom(final ZoomEvent event) 
		{
			if(event.getZoomLevel() < zoomLevel_)
				photoList_.clear();
			zoomLevel_ = event.getZoomLevel();
			refreshPhotos();
			return true;
		} // onZoom

		protected void refreshPhotos() 
		{
			final BoundingBoxE6 bounds = map_.getBoundingBox();
			double n = bounds.getLatNorthE6() / 1E6;
			double s = bounds.getLatSouthE6() / 1E6;
			double e = bounds.getLonEastE6() / 1E6;
			double w = bounds.getLonWestE6() / 1E6;
			
			int zoom = map_.getZoomLevel();
			final GeoPoint centre = map_.getMapCenter();
			double clat = (double)centre.getLatitudeE6() / 1E6;
			double clon = (double)centre.getLongitudeE6() / 1E6;
			new GetPhotosTask(this).execute(clat, clon, zoom, n, s, e, w);		
		} // refreshPhotos
		
		private void setPhotos(final List<PhotoItemOverlay.PhotoItem> items)
		{
			for(final PhotoItemOverlay.PhotoItem item : items)
				if(!photoList_.contains(item))
					photoList_.add(item);
			if(photoList_.size() > 500)  // arbitrary figure
				photoList_ = new ArrayList<PhotoItemOverlay.PhotoItem>(photoList_.subList(100, 500));
			map_.postInvalidate();
		} // setPhotos
	} // class PhotomapListener
	
	static private class GetPhotosTask extends AsyncTask<Object,Void,List<Photo>> 
	{
		private final PhotomapListener listener_;
		
		public GetPhotosTask(final PhotomapListener listener)
		{
			listener_ = listener;
		} // GetPhotosTask
		
		protected List<Photo> doInBackground(Object... params) 
		{
			double clat = (Double) params[0];
			double clon = (Double) params[1];
			int zoom = (Integer) params[2];
			double n = (Double) params[3];
			double s = (Double) params[4];
			double e = (Double) params[5];
			double w = (Double) params[6];
			final List<Photo> photos;
			try {
				photos = ApiClient.getPhotos(clat, clon, zoom, n, s, e, w);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return photos;
		} // doInBackground
		
		@Override
		protected void onPostExecute(final List<Photo> photos) 
		{
			if(photos.isEmpty())
				return;
			
			final List<PhotoItemOverlay.PhotoItem> items = new ArrayList<PhotoItemOverlay.PhotoItem>();
			for (final Photo photo: photos) 
			{
				final PhotoItemOverlay.PhotoItem item = new PhotoItemOverlay.PhotoItem(photo, listener_.photoMarkers_);
				items.add(item);
			} // for ...
			
			listener_.setPhotos(items);
		} // onPostExecute
	} // GetPhotosTask
} // class PhotoItemOverlay
