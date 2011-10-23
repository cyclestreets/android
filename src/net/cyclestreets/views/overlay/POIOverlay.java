package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;

import net.cyclestreets.R;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.POICategories;
import net.cyclestreets.api.POICategory;
import net.cyclestreets.util.Draw;
import net.cyclestreets.util.GeoHelper;

public class POIOverlay extends LiveItemOverlay<POIOverlay.POIItem>
                        implements MapListener, 
                                   DynamicMenuListener, 
                                   PauseResumeListener
{
  static public class POIItem extends OverlayItem 
	{
		private final POI poi_;
		
		public POIItem(final POI poi) 
		{
			super(poi.id() + "", poi.name(), poi.notes(), poi.position());
			poi_ = poi;
			setMarker(poi_.icon());
			setMarkerHotspot(HotspotPlace.CENTER);
		} // PhotoItem

		public POI poi() { return poi_; }
		public String getUrl() { return poi_.url(); }
		public POICategory category() { return poi_.category(); }

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
  /////////////////////////////////////////////////////
  private POICategories allCats_;
  private final List<POICategory> activeCategories_;
  private POIItem active_;
  private final Point curScreenCoords_ = new Point();
  private final Point touchScreenPoint_ = new Point();
  private GeoPoint lastFix_;
  private Rect bubble_;
  
	public POIOverlay(final Context context,
							      final MapView mapView)
	{
		super(context, 
			    mapView,
			    null,
			    false);

		activeCategories_ = new ArrayList<POICategory>();
	} // POIOverlay
	
	private POICategories allCategories()
	{
	  // delay load
	  if(allCats_ == null)
	    allCats_ = POICategories.get();
	  return allCats_;
	} // allCategories
	
  /////////////////////////////////////////////////////
	public void onPause(final SharedPreferences.Editor prefs)
	{
	  prefs.putInt("category-count", activeCategories_.size());
	  for(int i = 0; i != activeCategories_.size(); ++i)
	    prefs.putString("category-" + i, activeCategories_.get(i).name());
	} // onPause
	
	public void onResume(final SharedPreferences prefs)
	{
	  activeCategories_.clear();
	  int count = prefs.getInt("category-count", 0);
	  for(int i = 0; i != count; ++i)
	  {
	    final String name = prefs.getString("category-" + i, "");
	    for(final POICategory cat : allCategories())
	      if(name.equals(cat.name()))
	      {
	        activeCategories_.add(cat);
	        break;
	      } // if...
	  } // for ...
	} // onResume
	
	///////////////////////////////////////////////////////
	@Override
	public boolean onZoom(final ZoomEvent event) 
	{
	  clearLastFix();
	  return super.onZoom(event);
	} // onZoom
	
  ///////////////////////////////////////////////////
  @Override
  public boolean onSingleTap(final MotionEvent event)
  {
    if((active_ != null) && (tappedInBubble(event)))
      return true;
      
    return super.onSingleTap(event);
  } // onSingleTap
  
  private TapToRouteOverlay routeOverlay()
  {
    for(Overlay o : mapView().getOverlays())
      if(o instanceof TapToRouteOverlay)
        return (TapToRouteOverlay)o;
    return null;
  } // routeOverlay
  
  private boolean tappedInBubble(final MotionEvent event)
  {
    final Projection pj = mapView().getProjection();
    final int eventX = (int) event.getX();
    final int eventY = (int) event.getY();

    pj.fromMapPixels(eventX, eventY, touchScreenPoint_);

    if(!bubble_.contains(touchScreenPoint_.x, touchScreenPoint_.y))
      return false;
    
    TapToRouteOverlay o = routeOverlay();
    if(o == null)
      return false;
    
    o.setNextMarker(active_.getPoint());
    
    return true;
  } // tappedInBubble
  
	@Override
  protected boolean onItemSingleTap(final int index, final POIItem item, final MapView mapView) 
  {
    if(active_ == item)
      active_ = null;
    else
      active_ = item;
    redraw();
    
    return true;
  } // onItemSingleTap
  
  @Override
  protected boolean onItemDoubleTap(final int index, final POIItem item, final MapView mapView) 
  {
    return false;
  } // onItemDoubleTap

  /////////////////////////////////////////////////////
	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
  {
	  if(activeCategories_.isEmpty())
	    return;
	  
    super.draw(canvas, mapView, shadow);
    
    if(active_ == null)
      return;
    
    final String bubble = active_.getTitle() +
                          (active_.getSnippet().length() > 0 ? "\n" + active_.getSnippet() : "") +
                          (active_.getUrl().length() > 0 ? "\n" + active_.getUrl() : "");

    final Projection pj = mapView.getProjection();
    pj.toMapPixels(active_.getPoint(), curScreenCoords_);
    
    bubble_ = Draw.drawBubble(canvas, textBrush(), offset(), cornerRadius(), curScreenCoords_, bubble);
  } // draw

	public void show(final POICategory cat) 
	{ 
	  if(activeCategories_.contains(cat))
	    return;
	  activeCategories_.add(cat);
	  clearLastFix();
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
	  
	  if((active_ != null) && (cat.equals(active_.category())))
	    active_ = null;
	  
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
    active_ = null;
    redraw();
  } // clear
	
	public boolean showing(final POICategory cat)
	{
	  return activeCategories_.contains(cat);
	} // showing

  protected boolean fetchItemsInBackground(final GeoPoint mapCentre,
                                           final int zoom,
                                           final BoundingBoxE6 boundingBox)
	{
    if(activeCategories_.isEmpty())
      return false;
    
    final double moved = lastFix_ != null ? GeoHelper.distanceBetween(mapCentre, lastFix_) : Double.MAX_VALUE;
    final double diagonalWidth = boundingBox.getDiagonalLengthInMeters() / 1000.0;
    
    // first time through width can be zero
    if(diagonalWidth == 0.0)
      return false;
    
    if(moved < (diagonalWidth/2))
      return false;
    
    lastFix_ = mapCentre;    
		GetPOIsTask.fetch(this, mapCentre, (int)(diagonalWidth * 3) + 1);
		return true;
	} // refreshItemsInBackground

  protected void clearLastFix()
  {
    lastFix_ = null;
  } // clearLastFix
  
	/////////////////////////////////////////////////////
  ////////////////////////////////////////////////
  public boolean onCreateOptionsMenu(final Menu menu)
  {
    final SubMenu poi = menu.addSubMenu(0, R.string.ic_menu_poi, Menu.NONE, R.string.ic_menu_poi).setIcon(R.drawable.ic_menu_poi);
    
    poi.add(R.string.ic_menu_poi, R.string.ic_menu_poi_clear_all, Menu.NONE, R.string.ic_menu_poi_clear_all);
    for(int index = 0; index != allCategories().count(); ++index)
    {
      final MenuItem c = poi.add(R.string.ic_menu_poi, index, Menu.NONE, allCategories().get(index).shortName());
      c.setCheckable(true);
    } // for ...
    
    return true;
  } // onCreateOptionsMenu
  
  public boolean onPrepareOptionsMenu(final Menu menu)
  {
    final SubMenu i = menu.findItem(R.string.ic_menu_poi).getSubMenu();

    for(int index = 0; index != allCategories().count(); ++index)
    {
      final MenuItem c = i.findItem(index);
      c.setChecked(showing(allCategories().get(index)));
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
      POICategory cat = allCategories().get(item.getItemId());
      toggle(cat);
    } // if ...
    
    return true;
  } // onMenuItemSelected
  
  /////////////////////////////////////////////////////
	static private class GetPOIsTask extends AsyncTask<Object,Void,List<POI>> 
	{
		static void fetch(final POIOverlay overlay, 
						          final GeoPoint centre,
						          final int radius)
		{
			new GetPOIsTask(overlay).execute(centre, radius);
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
	    final int radius = (Integer)params[1];

      final List<POI> pois = new ArrayList<POI>();

      for(final POICategory cat : overlay_.activeCategories_)
        try {
			    pois.addAll(cat.pois(centre, radius));
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
				{
				  if(items.contains(poi))
				    continue;
					items.add(new POIOverlay.POIItem(poi));
				} // for ...
			
			overlay_.setItems(items);
		} // onPostExecute
	} // GetPhotosTask
} // class PhotoItemOverlay
