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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import net.cyclestreets.R;
import net.cyclestreets.StoredRoutesActivity;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.POICategories;
import net.cyclestreets.api.POICategory;

public class POIOverlay extends CycleStreetsItemOverlay<POIOverlay.POIItem>
                        implements MapListener, DynamicMenuListener
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
		public POICategory category() { return poi_.category(); }
		
		// Markers
		@Override
		public Drawable getMarker(int stateBitset) 
		{ 
		  return poi_.icon();
		} // getMarker

		// Equality testing
		@Override
		public int hashCode() { return ((poi_ == null) ? 0 : poi_.id()); }
		
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
  private final Context context_;
  private final POICategories allCategories_;
  private final List<POICategory> activeCategories_;
  
	public POIOverlay(final Context context,
							      final MapView mapView)
	{
		super(context, 
			    mapView,
			    new POIListener(context));

		context_ = context;
		allCategories_ = POICategories.get();
		activeCategories_ = new ArrayList<POICategory>();
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
	  refreshItems();
	} // show
	
	public void hide(final POICategory cat)
	{
	  if(!activeCategories_.contains(cat))
	    return;
	  activeCategories_.remove(cat);
	  
	  for(int i = items().size() - 1; i >= 0; --i)
	    if(cat.equals(items().get(i).category()))
	      items().remove(i);
	  redraw();
	} // hide
	 
  public void toggle(final POICategory cat)
  {
    if(activeCategories_.contains(cat))
      hide(cat);
    else
      show(cat);
  } // toggle
  
  public void clear()
  {
    activeCategories_.clear();
    items().clear();
    redraw();
  } // clear
	
	public boolean showing(final POICategory cat)
	{
	  return activeCategories_.contains(cat);
	} // showing

  protected void fetchItemsInBackground(final GeoPoint mapCentre,
                                        final int zoom,
                                        final BoundingBoxE6 boundingBox)
	{
		GetPOIsTask.fetch(this, mapCentre, boundingBox);
	} // refreshItemsInBackground
	
	/////////////////////////////////////////////////////
  ////////////////////////////////////////////////
  public boolean onCreateOptionsMenu(final Menu menu)
  {
    final SubMenu poi = menu.addSubMenu(0, R.string.ic_menu_poi, Menu.NONE, R.string.ic_menu_poi).setIcon(R.drawable.ic_menu_poi);
    
    poi.add(R.string.ic_menu_poi, R.string.ic_menu_poi_clear_all, Menu.NONE, R.string.ic_menu_poi_clear_all);
    for(int index = 0; index != allCategories_.count(); ++index)
    {
      final MenuItem c = poi.add(R.string.ic_menu_poi, index, Menu.NONE, allCategories_.get(index).shortName());
      c.setCheckable(true);
    } // for ...
    
    return true;
  } // onCreateOptionsMenu
  
  public boolean onPrepareOptionsMenu(final Menu menu)
  {
    final SubMenu i = menu.findItem(R.string.ic_menu_poi).getSubMenu();

    for(int index = 0; index != allCategories_.count(); ++index)
    {
      final MenuItem c = i.findItem(index);
      c.setChecked(showing(allCategories_.get(index)));
    } // for ...

    return true;
  } // onPrepareOptionsMenu

  
  public boolean onMenuItemSelected(final int featureId, final MenuItem item)
  {
    if(item.getGroupId() != R.string.ic_menu_poi)
      return false;

    if(item.getItemId() == R.string.ic_menu_poi_clear_all)
      clear();
    else
    {
      POICategory cat = allCategories_.get(item.getItemId());
      toggle(cat);
    } // if ...
    
    return true;
  } // onMenuItemSelected
  
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
