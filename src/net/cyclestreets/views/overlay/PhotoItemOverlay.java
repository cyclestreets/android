package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import net.cyclestreets.DisplayPhotoActivity;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.PhotoMarkers;

public class PhotoItemOverlay extends CycleStreetsItemOverlay<PhotoItemOverlay.PhotoItem>
{
	static public class PhotoItem extends OverlayItem 
	{
		private final Photo photo_;
		private final PhotoMarkers photoMarkers;
		
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
	 
  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////
	static private class PhotoItemListener implements OnItemGestureListener<PhotoItemOverlay.PhotoItem>
	{
	  private final Context context_;
	  
	  public PhotoItemListener(final Context context) 
	  {
	    context_ = context;
	  } // PhotoItemListener

	  public boolean onItemLongPress(int i, final PhotoItemOverlay.PhotoItem item) 
	  {
	    showPhoto(item);
	    return true;
	  } // onItemLongPress
    
	  public boolean onItemSingleTapUp(int i, final PhotoItemOverlay.PhotoItem item) 
	  {
	    showPhoto(item);
	    return true;
	  } // onItemSingleTapUp
  
	  private void showPhoto(final PhotoItemOverlay.PhotoItem item)
	  {
	    final Intent intent = new Intent(context_, DisplayPhotoActivity.class);
	    intent.setData(Uri.parse(item.photo().thumbnailUrl));
	    intent.putExtra("caption", item.photo().caption);
	    context_.startActivity(intent);
	  } // showPhoto
	} // PhotoItemListener

	/////////////////////////////////////////////////////
	private final PhotoMarkers photoMarkers_;
	
	public PhotoItemOverlay(final Context context,
	                        final MapView mapView)
	{
		super(context, 
			    mapView, 
			    new PhotoItemListener(context));
		
		photoMarkers_ = new PhotoMarkers(context.getResources());
	} // PhotoItemOverlay
  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////
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

		GetPhotosTask.fetch(this, clat, clon, zoom, n, s, e, w);
	} // refreshPhotos
	
	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////
	static private class GetPhotosTask extends AsyncTask<Object,Void,List<Photo>> 
	{
		static void fetch(final PhotoItemOverlay overlay, 
						          final Object... params)
		{
			new GetPhotosTask(overlay).execute(params);
		} // fetch
		
		//////////////////////////////////////////////////////
		private final PhotoItemOverlay overlay_;
		
		private  GetPhotosTask(final PhotoItemOverlay overlay)
		{
			overlay_ = overlay;
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

			try {
				return ApiClient.getPhotos(clat, clon, zoom, n, s, e, w);
			}
			catch (final Exception ex) {
				// never mind, eh?
			}
			return null;
		} // doInBackground
		
		@Override
		protected void onPostExecute(final List<Photo> photos) 
		{
			final List<PhotoItemOverlay.PhotoItem> items = new ArrayList<PhotoItemOverlay.PhotoItem>();
			
			if(photos != null)
				for (final Photo photo: photos) 
					items.add(new PhotoItemOverlay.PhotoItem(photo, overlay_.photoMarkers_));
			
			overlay_.setItems(items);
		} // onPostExecute
	} // GetPhotosTask
} // class PhotoItemOverlay
