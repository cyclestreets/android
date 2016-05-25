package net.cyclestreets.views.overlay;

import java.util.Map;
import java.util.HashMap;

import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.overlay.Overlay;

public class OverlayHelper
{
  @SuppressWarnings("unchecked")
  public static <T extends Overlay> T findOverlay(final CycleMapView view, final Class<T> type)
  {
    for(Overlay o : view.getOverlays())
      if(type.isInstance(o))
        return (T)o;
    return null;
  } // findOverlay
  
  public static ControllerOverlay findController(final CycleMapView view)
  {
    return findOverlay(view, ControllerOverlay.class);
  } // controller
  
  public OverlayHelper(final CycleMapView view)
  {
    view_ = view;
  } // OverlayHelper
  
  @SuppressWarnings("unchecked")
  public <T extends Overlay> T get(final Class<T> type) 
  {
    T o = (T)memo_.get(type);
    if(o != null)
      return o;
    
    o = findOverlay(view_, type);
    memo_.put(type, o);
    return o;
  } // get 
  
  public ControllerOverlay controller() { return get(ControllerOverlay.class); }
  
  private CycleMapView view_;
  @SuppressWarnings("rawtypes")
  private Map<Class, Overlay> memo_ = new HashMap<>();
} // OverlayHelper
