package net.cyclestreets.util;

import java.util.Map;

import net.cyclestreets.view.R;
import net.cyclestreets.util.MapFactory;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public final class TurnIcons
{
  public final static class Mapping
  {
    private final Map<String, Drawable> mapping_;
    
    private Mapping(final Map<String, Drawable> mapping) 
    {
      mapping_ = mapping;
    } // Mapping
    
    public Drawable icon(final String turn) 
    {
      final Drawable i = mapping_.get(turn.toLowerCase());
      return i != null ? i : mapping_.get("default");
    } // icon
  } // class Mapping
  
  static public Mapping LoadMapping(final Context context)
  {
    return new Mapping(loadIconMappings(context));
  } // LoadMapping
  
  static private Map<String, Drawable> loadIconMappings(final Context context)
  {
    final Resources res = context.getResources();
    
    return MapFactory.map("straight on", res.getDrawable(R.drawable.straight_on))
                     .map("bear left", res.getDrawable(R.drawable.bear_left))
                     .map("turn left", res.getDrawable(R.drawable.turn_left))
                     .map("sharp left", res.getDrawable(R.drawable.sharp_left))
                     .map("bear right", res.getDrawable(R.drawable.bear_right))
                     .map("turn right", res.getDrawable(R.drawable.turn_right))
                     .map("sharp right", res.getDrawable(R.drawable.sharp_right))
                     .map("double-back", res.getDrawable(R.drawable.double_back))
                     .map("join roundabout", res.getDrawable(R.drawable.roundabout))
                     .map("first exit", res.getDrawable(R.drawable.first_exit))
                     .map("second exit", res.getDrawable(R.drawable.second_exit))
                     .map("third exit", res.getDrawable(R.drawable.third_exit))
                     .map("waymark", res.getDrawable(R.drawable.waymark))
                     .map("default", res.getDrawable(R.drawable.ic_launcher));
  } // loadIconMappings
} // TurnIcons
