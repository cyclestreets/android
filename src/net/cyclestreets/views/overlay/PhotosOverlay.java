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

public class PhotosOverlay extends LiveItemOverlay<PhotosOverlay.PhotoItem>
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
			return photoMarkers.getMarker(photo_.feature);	
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
	private final Context context_;
	private final PhotoMarkers photoMarkers_;
	
	public PhotosOverlay(final Context context,
	                        final MapView mapView)
	{
		super(context, 
			    mapView, 
			    null,
			    true);
	
		context_ = context;
		photoMarkers_ = new PhotoMarkers(context.getResources());
	} // PhotoItemOverlay

	///////////////////////////////////////////////////
	@Override
  protected boolean onItemSingleTap(final int index, final PhotoItem item, final MapView mapView) 
  {
    showPhoto(item, mapView);
    return true;
  } // onItemSingleTap
  
  @Override
  protected boolean onItemDoubleTap(final int index, final PhotoItem item, final MapView mapView) 
  {
    showPhoto(item, mapView);
    return true;
  } // onItemDoubleTap

  private void showPhoto(final PhotoItem item, final MapView mapView)
  {
    final Intent intent = new Intent(context_, DisplayPhotoActivity.class);
    intent.setData(Uri.parse(item.photo().thumbnailUrl));
    intent.putExtra("caption", item.photo().caption);
    mapView.getContext().startActivity(intent);
  } // showPhoto

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
		static void fetch(final PhotosOverlay overlay, 
						          final Object... params)
		{
			new GetPhotosTask(overlay).execute(params);
		} // fetch
		
		//////////////////////////////////////////////////////
		private final PhotosOverlay overlay_;
		
		private  GetPhotosTask(final PhotosOverlay overlay)
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
			final List<PhotosOverlay.PhotoItem> items = new ArrayList<PhotosOverlay.PhotoItem>();
			
			if(photos != null)
				for (final Photo photo: photos) 
					items.add(new PhotosOverlay.PhotoItem(photo, overlay_.photoMarkers_));
			
			overlay_.setItems(items);
		} // onPostExecute
	} // GetPhotosTask
} // class PhotoItemOverlay
