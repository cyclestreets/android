package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.api.IProjection;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import net.cyclestreets.R;
import net.cyclestreets.api.POI;
import net.cyclestreets.api.POICategories;
import net.cyclestreets.api.POICategory;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.util.Draw;
import net.cyclestreets.util.GeoHelper;
import net.cyclestreets.views.CycleMapView;

public class POIOverlay extends LiveItemOverlay<POIOverlay.POIItem>
                        implements MapListener, 
                                   DynamicMenuListener, 
                                   PauseResumeListener, 
                                   UndoAction
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
  private Context context_;
  private final List<POICategory> activeCategories_;
  private POIItem active_;
  private final Point curScreenCoords_ = new Point();
  private final Point touchScreenPoint_ = new Point();
  private IGeoPoint lastFix_;
  private Rect bubble_;
  private OverlayHelper overlays_;
  private boolean chooserShowing_;
  
  public POIOverlay(final Context context,
                    final CycleMapView mapView)
  {
    super(context, mapView, null, false);

    context_ = context;
    activeCategories_ = new ArrayList<POICategory>();
    overlays_ = new OverlayHelper(mapView);
    chooserShowing_ = false;
  } // POIOverlay
	
  private POICategories allCategories()
  {
    return POICategories.get();
  } // allCategories
	
  private TapToRouteOverlay routeOverlay()
  {
    return overlays_.get(TapToRouteOverlay.class);
  } // routeOverlay
  
  private ControllerOverlay controller()
  {
    return overlays_.controller();
  } // controller
  
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
    
    final boolean firstTime = !POICategories.loaded(); 
    
    try {
      reloadActiveCategories(prefs);
    } // try
    catch(Exception e) {
      // very occasionally this throws a NullException, although it's not something
      // I've been able to replicate :(  
      // Let's just carry on
      activeCategories_.clear();
    } // catch

    if(firstTime)
    {
      items().clear();
      clearLastFix();
      active_ = null;
      refreshItems();
    } // if ... 
  } // onResume
	
  private void reloadActiveCategories(final SharedPreferences prefs)
  {
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
  } // reloadActiveCategories
	
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
  
  private boolean tappedInBubble(final MotionEvent event)
  {
    final Projection pj = mapView().getProjection();
    final int eventX = (int) event.getX();
    final int eventY = (int) event.getY();

    pj.fromMapPixels(eventX, eventY, touchScreenPoint_);

    if(!bubble_.contains(touchScreenPoint_.x, touchScreenPoint_.y))
      return false;
    
    return routeMarkerAtItem(active_);
  } // tappedInBubble
  
  @Override
  protected boolean onItemSingleTap(final int index, final POIItem item, final MapView mapView) 
  {
    if(active_ == item)
      hideBubble();
    else
      showBubble(item);
    redraw();
    
    return true;
  } // onItemSingleTap
	
  private void showBubble(final POIItem item)
  {
    hideBubble();
    active_ = item;
    controller().pushUndo(this);
  } // showBubble
	
  private void hideBubble()
  {
    active_ = null;
    controller().flushUndo(this);
  } // hideBubble
  
  @Override
  protected boolean onItemDoubleTap(final int index, final POIItem item, final MapView mapView) 
  {
    return routeMarkerAtItem(item);
  } // onItemDoubleTap
  
  private boolean routeMarkerAtItem(final POIItem item)
  {
    hideBubble();
    
    final TapToRouteOverlay o = routeOverlay();
    if(o == null)
      return false;
    
    o.setNextMarker(item.getPoint());
    
    return true;
  } // routeMarkerAtItem

  /////////////////////////////////////////////////////
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
  {
    if(activeCategories_.isEmpty())
      return;
	  
    super.draw(canvas, mapView, shadow);
    
    if(active_ == null)
      return;
    
    final String bubble = active_.getTitle() +
                          (active_.getSnippet().length() > 0 ? "\n" + active_.getSnippet() : "") +
                          (active_.getUrl().length() > 0 ? "\n" + active_.getUrl() : "");

    final IProjection pj = mapView.getProjection();
    pj.toPixels(active_.getPoint(), curScreenCoords_);
    
    bubble_ = Draw.drawBubble(canvas, textBrush(), offset(), cornerRadius(), curScreenCoords_, bubble);
  } // draw

  public void clear()
  {
    activeCategories_.clear();
    items().clear();
    active_ = null;
    redraw();
  } // clear
  
  private void updateCategories(final List<POICategory> newCategories)
  {
    final List<POICategory> removed = notIn(activeCategories_, newCategories);
    final List<POICategory> added = notIn(newCategories, activeCategories_);

    if(removed.size() != 0)
    {
      for(final POICategory r : removed)
        hide(r);
      redraw();
    } // if ...
        
    if(added.size() != 0)
    {
      for(final POICategory a : added)
        activeCategories_.add(a);
      clearLastFix();
      refreshItems();
    } // if ...
  } // updateCategories
  
  private void hide(final POICategory cat)
  {
    if(!activeCategories_.contains(cat))
      return;
    activeCategories_.remove(cat);
    
    for(int i = items().size() - 1; i >= 0; --i)
      if(cat.equals(items().get(i).category()))
        items().remove(i);
    
    if((active_ != null) && (cat.equals(active_.category())))
      active_ = null;
  } // hide
   
  private List<POICategory> notIn(final List<POICategory> c1, 
                                  final List<POICategory> c2)
  {
    final List<POICategory> n = new ArrayList<POICategory>();
    
    for(final POICategory c : c1)
      if(!c2.contains(c))
        n.add(c);
        
    return n;
  } // notIn
  
  public boolean showing(final POICategory cat)
  {
    return activeCategories_.contains(cat);
  } // showing

  protected boolean fetchItemsInBackground(final IGeoPoint mapCentre,
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
    menu.add(0, R.string.ic_menu_poi, Menu.NONE, R.string.ic_menu_poi).setIcon(R.drawable.ic_menu_poi);
    
    return true;
  } // onCreateOptionsMenu
  
  public boolean onPrepareOptionsMenu(final Menu menu)
  {
    return true;
  } // onPrepareOptionsMenu
  
  public boolean onMenuItemSelected(final int featureId, final MenuItem item)
  {
    if(item.getItemId() != R.string.ic_menu_poi)
      return false;
    
    if(chooserShowing_ == true)
      return true;
    
    chooserShowing_  = true;
    final POICategoryAdapter poiAdapter = new POICategoryAdapter(context_,
                                                                 allCategories(), 
                                                                 activeCategories_);

    // it can take a while to show the dialog
    Dialog.listViewDialog(context_, 
                          poiAdapter, 
                          new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                int which) {
                              chooserShowing_ = false;
                              updateCategories(poiAdapter.chosenCategories());
                            }
                          },
                          new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                int which) {
                              chooserShowing_ = false;
                            }
                          });
    return true;
  } // onMenuItemSelected
  
  @Override
  public void onBackPressed()
  {
    hideBubble();
    redraw();
  } // onBackPressed
  
  /////////////////////////////////////////////////////
  static private class GetPOIsTask extends AsyncTask<Object,Void,List<POI>> 
  {
    static void fetch(final POIOverlay overlay, 
                      final IGeoPoint centre,
                      final int radius)
    {
      new GetPOIsTask(overlay).execute(centre, radius);
    } // fetch
		
    //////////////////////////////////////////////////////
    private final POIOverlay overlay_;
    private final List<POICategory> activeCategories_;
		
    private GetPOIsTask(final POIOverlay overlay)
    {
      overlay_ = overlay;
      // take snapshot of categories to avoid later contention
      activeCategories_ = new ArrayList<POICategory>(overlay.activeCategories_);
    } // GetPhotosTask
		
    protected List<POI> doInBackground(Object... params) 
    {
      final IGeoPoint centre = (IGeoPoint)params[0];
      final int radius = (Integer)params[1];

      final List<POI> pois = new ArrayList<POI>();
      
      for(final POICategory cat : activeCategories_)
        try {
          pois.addAll(cat.pois(centre, radius));
        } // try
        catch (final Exception ex) {
          // never mind, eh?
        } // catch
      return pois;
    } // doInBackground
		
    @Override
    protected void onPostExecute(final List<POI> pois) 
    {
      final List<POIOverlay.POIItem> items = new ArrayList<POIOverlay.POIItem>();
			
      for (final POI poi : pois)
      {
        if(items.contains(poi))
          continue;
        items.add(new POIOverlay.POIItem(poi));
      } // for ...
			
      overlay_.setItems(items);
    } // onPostExecute
  } // GetPhotosTask
	
  //////////////////////////////////
  static class POICategoryAdapter extends BaseAdapter 
  {
    private final LayoutInflater inflater_;
    private POICategories cats_;
    private POICategories massiveIconCats_;
    private List<POICategory> selected_;
        
    POICategoryAdapter(final Context context, 
                       final POICategories allCategories,
                       final List<POICategory> initialCategories)
    {
      inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      cats_ = allCategories;
      massiveIconCats_ = POICategories.load(64);
      selected_ = new ArrayList<POICategory>();
      selected_.addAll(initialCategories);
    } // POICategoryAdaptor
    
    public List<POICategory> chosenCategories()
    {
      return selected_;
    } // chosenCategories
    
    @Override
    public int getCount() { return cats_.count(); }
    
    @Override
    public Object getItem(int position) { return cats_.get(position); }

    @Override
    public long getItemId(int position) { return position; } 
    
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) 
    {
      final POICategory cat = cats_.get(position);
      final View v = inflater_.inflate(R.layout.poicategories_item, parent, false);

      final TextView n = (TextView)v.findViewById(R.id.name);
      n.setText(cat.name());

      final ImageView iv = (ImageView)v.findViewById(R.id.icon);
      iv.setImageDrawable(massiveIconCats_.get(cat.name()).icon());
      
      final CheckBox chk = (CheckBox)v.findViewById(R.id.checkbox);
      chk.setChecked(isSelected(cat));

      n.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          chk.setChecked(!chk.isChecked());
        } // onClick      
      });
      iv.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          chk.setChecked(!chk.isChecked());
        } // onClick      
      });
      
      chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked)
        {
          if(isChecked)
            selected_.add(cat);
          else
            selected_.remove(cat);
        } // onCheckedChanged
      });
      
      return v;
    } // getView
    
    private boolean isSelected(POICategory cat)
    {
      for(POICategory c : selected_)
        if(cat.name().equals(c.name()))
          return true;
      return false;
    } // isSelected
  } // class RouteSummaryAdapter
} // class PhotoItemOverlay
