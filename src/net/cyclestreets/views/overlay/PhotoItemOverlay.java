package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.PhotoMarkers;
import net.cyclestreets.util.Brush;

public class PhotoItemOverlay extends ItemizedOverlay<PhotoItemOverlay.PhotoItem>
							  implements MapListener
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
	/////////////////////////////////////////////////////
	private final MapView mapView_;
	private final PhotoMarkers photoMarkers_;
	private int zoomLevel_;
	private boolean loading_;
	
	private final int offset_;
	private final float radius_;
	private final Paint textBrush_;
	
	static private final String LOADING = "Loading ...";
	
	public PhotoItemOverlay(final Context context,
							final MapView mapView,
							final OnItemGestureListener<PhotoItemOverlay.PhotoItem> listener)
	{
		super(context, 
			  new ArrayList<PhotoItemOverlay.PhotoItem>(), 
			  listener);
		
		mapView_ = mapView;
		zoomLevel_ = mapView_.getZoomLevel();
		photoMarkers_ = new PhotoMarkers(context.getResources());
		loading_ = false;
		
		offset_ = OverlayHelper.offset(context);
		radius_ = OverlayHelper.cornerRadius(context);
		textBrush_ = Brush.createTextBrush(offset_);

		mapView_.setMapListener(new DelayedMapListener(this));
	} // PhotoItemOverlay

	@Override
	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
	{
		super.draw(canvas, mapView, shadow);
		
		if(!loading_)
			return;
		
		final Rect bounds = new Rect();
		textBrush_.getTextBounds(LOADING, 0, LOADING.length(), bounds);

		int width = bounds.width() + (offset_ * 2);
		final Rect screen = canvas.getClipBounds();
        screen.left = screen.centerX() - (width/2); 
        screen.top += offset_* 2;
        screen.right = screen.left + width;
        screen.bottom = screen.top + bounds.height() + (offset_ * 2);
		
        if(!OverlayHelper.drawRoundRect(canvas, screen, radius_, Brush.Grey))
        	return;
        canvas.drawText(LOADING, screen.centerX(), screen.centerY() + bounds.bottom, textBrush_);
	} // drawButtons
	
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
			items().clear();
		zoomLevel_ = event.getZoomLevel();
		refreshPhotos();
		return true;
	} // onZoom

	private void refreshPhotos() 
	{
		final BoundingBoxE6 bounds = mapView_.getBoundingBox();
		double n = bounds.getLatNorthE6() / 1E6;
		double s = bounds.getLatSouthE6() / 1E6;
		double e = bounds.getLonEastE6() / 1E6;
		double w = bounds.getLonWestE6() / 1E6;
		
		int zoom = mapView_.getZoomLevel();
		final GeoPoint centre = mapView_.getMapCenter();
		double clat = (double)centre.getLatitudeE6() / 1E6;
		double clon = (double)centre.getLongitudeE6() / 1E6;
		GetPhotosTask.fetch(this, clat, clon, zoom, n, s, e, w);
		loading_ = true;
		mapView_.postInvalidate();
	} // refreshPhotos
	
	private void setPhotos(final List<PhotoItemOverlay.PhotoItem> items)
	{
		for(final PhotoItemOverlay.PhotoItem item : items)
			if(!items().contains(item))
				items().add(item);
		if(items().size() > 500)  // arbitrary figure
			items().remove(items().subList(0, 100));
		loading_ = false;
		mapView_.postInvalidate();
	} // setPhotos

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
			
			overlay_.setPhotos(items);
		} // onPostExecute
	} // GetPhotosTask
} // class PhotoItemOverlay
