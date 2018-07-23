package net.cyclestreets.util;

import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil;

import java.lang.reflect.Field;

public class IconicsInflater {

  public static void inflate(MenuInflater inflater, int menuId, Menu menu) {
    IconicsInflater.inflate(inflater, menuId, menu, true);
  }

  // Derive Context from the inflater, and then delegate to the Iconics inflater.
  public static void inflate(MenuInflater inflater, int menuId, Menu menu, boolean checkSubMenus) {
    Context context = getContext(inflater);

    if (context != null) {
      IconicsMenuInflaterUtil.inflate(inflater, getContext(inflater), menuId, menu, checkSubMenus);
    } else {
      // In the worst case (e.g. on Google implementation change), we fall back to the default
      // inflater; we'll lose the icons but won't fall over.
      inflater.inflate(menuId, menu);
    }
  }

  // Use reflection to derive the Context from a MenuInflater.
  //
  // In some fragment transitions, the menu inflation is performed before the fragment's context
  // is initialised, so we can't just do a `getContext()`; the internal `mContext` field is used
  // in this way by the native inflater.inflate(), so we should be safe.
  private static Context getContext(MenuInflater inflater) {
    try {
      Field f = inflater.getClass().getDeclaredField("mContext");
      f.setAccessible(true);
      return (Context) f.get(inflater);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      // In the worst case
      Log.e(IconicsInflater.class.getSimpleName(), "Failed to find mContext on MenuInflater");
      return null;
    }
  }
}
