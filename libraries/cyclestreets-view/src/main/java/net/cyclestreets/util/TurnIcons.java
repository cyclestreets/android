package net.cyclestreets.util;

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

    return MapFactory.map("straight on", getDrawable(res, R.drawable.straight_on))
                     .map("bear left", getDrawable(res, R.drawable.bear_left))
                     .map("turn left", getDrawable(res, R.drawable.turn_left))
                     .map("sharp left", getDrawable(res, R.drawable.sharp_left))
                     .map("bear right", getDrawable(res, R.drawable.bear_right))
                     .map("turn right", getDrawable(res, R.drawable.turn_right))
                     .map("sharp right", getDrawable(res, R.drawable.sharp_right))
                     .map("double-back", getDrawable(res, R.drawable.double_back))
                     .map("join roundabout", getDrawable(res, R.drawable.roundabout))
                     .map("first exit", getDrawable(res, R.drawable.first_exit))
                     .map("second exit", getDrawable(res, R.drawable.second_exit))
                     .map("third exit", getDrawable(res, R.drawable.third_exit))
                     .map("waymark", getDrawable(res, R.drawable.waymark))
                     .map("default", getDrawable(res, R.drawable.ic_launcher));
  }

  private static Drawable getDrawable(Resources res, int iconId) {
    return ResourcesCompat.getDrawable(res, iconId, null);
  }
}
