package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.events.MapListener;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import net.cyclestreets.R;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.POICategory;

public class POIOverlay extends CycleStreetsItemOverlay<POIOverlay.POIItem>
                        implements MapListener
{
  static private Drawable defaultMarker_;

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
		  return defaultMarker_;
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
  static private class POIListener implements OnItemGestureListener<POIItem>
  {
    private final Context context_;
    
    public POIListener(final Context context) 
    {
      context_ = context;
    } // PhotoItemListener

    public boolean onItemLongPress(int i, final POIItem item) 
    {
      //showPhoto(item);
      return true;
    } // onItemLongPress
    
    public boolean onItemSingleTapUp(int i, final POIItem item) 
    {
      //showPhoto(item);
      return true;
    } // onItemSingleTapUp
  } // PhotoItemListener

  /////////////////////////////////////////////////////
  private List<POICategory> activeCategories_;
  
	public POIOverlay(final Context context,
							      final MapView mapView)
	{
		super(context, 
			    mapView,
			    new POIListener(context));
		activeCategories_ = new ArrayList<POICategory>();
		
    defaultMarker_ = context.getResources().getDrawable(R.drawable.icon);
	} // POIOverlay

	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
  {
	  if(activeCategories_.isEmpty())
	    return;
    super.draw(canvas, mapView, shadow);
  } // draw
	
	public void show(final POICategory cat) 
	{ 
	  if(activeCategories_.contains(cat))
	    return;
	  activeCategories_.add(cat);
	  redraw();
	} // show
	
	public void hide(final POICategory cat)
	{
	  if(!activeCategories_.contains(cat))
	    return;
	  activeCategories_.remove(cat);
	  redraw();
	} // hide

  protected void fetchItemsInBackground(final GeoPoint mapCentre,
                                        final int zoom,
                                        final BoundingBoxE6 boundingBox)
	{
		GetPOIsTask.fetch(this, mapCentre, boundingBox);
	} // refreshPhotos
	
	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////
	static private class GetPOIsTask extends AsyncTask<Object,Void,List<POI>> 
	{
		static void fetch(final POIOverlay overlay, 
						          final GeoPoint centre,
						          final BoundingBoxE6 boundingBox)
		{
			new GetPOIsTask(overlay).execute(centre, boundingBox);
		} // fetch
		
		//////////////////////////////////////////////////////
		private final POIOverlay overlay_;
		
		private  GetPOIsTask(final POIOverlay overlay)
		{
			overlay_ = overlay;
		} // GetPhotosTask
		
		protected List<POI> doInBackground(Object... params) 
		{
		  final GeoPoint centre = (GeoPoint)params[0];
		  final BoundingBoxE6 boundingBox = (BoundingBoxE6)params[1];
		  
      final List<POI> pois = new ArrayList<POI>();

      for(final POICategory cat : overlay_.activeCategories_)
        try {
			    pois.addAll(cat.pois(centre, boundingBox));
        }
			  catch (final Exception ex) {
			    // never mind, eh?
			  }
      return pois;
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
