package net.cyclestreets.util;

import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;

public class MenuHelper
{
  public static MenuItem createMenuItem(final Menu menu,
                                        final int itemId,
                                        final int order,
                                        final Drawable icon) {
    final MenuItem item = menu.findItem(itemId);
    if (item != null)
      return item;
    final MenuItem newItem = menu.add(0, itemId, order, itemId);
    if (icon != null)
      newItem.setIcon(icon);
    return newItem;
  }

  public static MenuItem createMenuItem(final Menu menu,
                                        final int itemId,
                                        final int order,
                                        final int iconId) {
    final MenuItem item = menu.findItem(itemId);
    if (item != null)
      return item;
    final MenuItem newItem = menu.add(0, itemId, order, itemId);
    if (iconId != 0)
      newItem.setIcon(iconId);
    return newItem;
  }

  public static MenuItem createMenuItem(final Menu menu, final int itemId) {
    final MenuItem item = menu.findItem(itemId);
    if (item != null)
      return item;
    return menu.add(0, itemId, Menu.NONE, itemId);
  }

  public static MenuItem enableMenuItem(final Menu menu, final int itemId, final boolean enabled) {
    final MenuItem mi = menu.findItem(itemId);
    if (mi != null) {
      mi.setVisible(true);
      mi.setEnabled(enabled);
    }
    return mi;
  }

  public static MenuItem showMenuItem(final Menu menu, final int itemId, final boolean show) {
    final MenuItem mi = menu.findItem(itemId);
    if (mi != null)
      mi.setVisible(show);
    return mi;
  }
}
