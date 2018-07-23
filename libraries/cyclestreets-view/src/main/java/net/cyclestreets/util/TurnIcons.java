package net.cyclestreets.util;

import java.util.HashMap;
import java.util.Map;

import net.cyclestreets.view.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

public final class TurnIcons
{
  public final static class Mapping  {
    private final Map<String, Drawable> mapping_;

    private Mapping(final Map<String, Drawable> mapping) {
      mapping_ = mapping;
    }

    public Drawable icon(final String turn) {
      final Drawable i = mapping_.get(turn.toLowerCase());
      return i != null ? i : mapping_.get("default");
    }
  }

  public static Mapping LoadMapping(final Context context) {
    return new Mapping(loadIconMappings(context));
  }

  private static Map<String, Drawable> loadIconMappings(final Context context) {
    final Resources res = context.getResources();

    return new HashMap<String, Drawable>() {{
        put("straight on", getDrawable(res, R.drawable.straight_on));
        put("bear left", getDrawable(res, R.drawable.bear_left));
        put("turn left", getDrawable(res, R.drawable.turn_left));
        put("sharp left", getDrawable(res, R.drawable.sharp_left));
        put("bear right", getDrawable(res, R.drawable.bear_right));
        put("turn right", getDrawable(res, R.drawable.turn_right));
        put("sharp right", getDrawable(res, R.drawable.sharp_right));
        put("double-back", getDrawable(res, R.drawable.double_back));
        put("join roundabout", getDrawable(res, R.drawable.roundabout));
        put("first exit", getDrawable(res, R.drawable.first_exit));
        put("second exit", getDrawable(res, R.drawable.second_exit));
        put("third exit", getDrawable(res, R.drawable.third_exit));
        put("waymark", getDrawable(res, R.drawable.waymark));
        put("default", getDrawable(res, R.drawable.ic_launcher));
      }};
  }

  private static Drawable getDrawable(Resources res, int iconId) {
    return ResourcesCompat.getDrawable(res, iconId, null);
  }
}
