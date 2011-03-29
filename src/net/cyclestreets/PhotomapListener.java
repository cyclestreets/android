package net.cyclestreets;

import java.util.ArrayList;
import java.util.List;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Photo;

import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.events.MapAdapter;

import android.content.Context;
import android.os.AsyncTask;

public class PhotomapListener extends MapAdapter 
{
	private MapView map_;
	private int zoomLevel_;
	private List<PhotoItem> photoList_;
	private PhotoMarkers photoMarkers_;

	public PhotomapListener(final Context ctx, final MapView map, final List<PhotoItem> photoList) 
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
	
	private void setPhotos(final List<PhotoItem> items)
	{
		for(final PhotoItem item : items)
			if(!photoList_.contains(item))
				photoList_.add(item);
		if(photoList_.size() > 500)  // arbitrary figure
			photoList_ = new ArrayList<PhotoItem>(photoList_.subList(100, 500));
		map_.postInvalidate();
	} // setPhotos

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
			
			final List<PhotoItem> items = new ArrayList<PhotoItem>();
			for (final Photo photo: photos) 
			{
				final PhotoItem item = new PhotoItem(photo, listener_.photoMarkers_);
				items.add(item);
			} // for ...
			
			listener_.setPhotos(items);
		} // onPostExecute
	} // GetPhotosTask
} // class PhotomapListener
